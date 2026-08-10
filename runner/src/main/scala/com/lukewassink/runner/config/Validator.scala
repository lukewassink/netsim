package com.lukewassink.runner.config

import com.lukewassink.runner.config.BehaviorLocation.referenceLocations
import com.lukewassink.runner.util.{Failure, Result, Success}

// Validates global constraints on a syntax tree.
object Validator {
  def validate(simulationNode: SimulationNode): Result[SimulationNode] =
    val errors = runAllValidators(simulationNode)
    if errors.isEmpty then Success(simulationNode)
    else Failure[SimulationNode](errors)

  private def validationContext(node: SimulationNode): ValidationContext = {
    val indicesByName =
      node.network.nodes.map(_.name).zipWithIndex
        .groupMap((name, _) => name)((name, idx) => idx)
    ValidationContext(indicesByName)
  }

  private def runAllValidators(
      node: SimulationNode
  ): List[ConfigValidationException] = {
    val context = validationContext(node)
    validators.foldLeft(List.empty)((errors, validator) =>
      errors ++ validator.run(node, context)
    )
  }

  private val validators: List[Validator] = List(
    DuplicateNodeNamesValidator,
    MissingReferenceNameValidator
  )
}

case class ValidationContext(nodeIndicesByName: Map[String, List[Int]])

trait Validator:
  def run(
      node: SimulationNode,
      context: ValidationContext
  ): List[ConfigValidationException]

case object DuplicateNodeNamesValidator extends Validator:
  def run(
      node: SimulationNode,
      context: ValidationContext
  ): List[ConfigValidationException] =
    context.nodeIndicesByName.filter((_, indices) => indices.length > 1)
      .map(DuplicateNodeNamesException(_, _)).toList

case object MissingReferenceNameValidator extends Validator:
  def run(
      node: SimulationNode,
      context: ValidationContext
  ): List[ConfigValidationException] = {
    val nodeNames = context.nodeIndicesByName.keySet
    referenceLocations(node).filterNot(rl => nodeNames.contains(rl.nodeName))
      .map(MissingReferenceNameException(_))
  }
