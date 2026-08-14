package com.lukewassink.runner.config

import com.lukewassink.runner.config.BehaviorLocation.referenceLocations
import com.lukewassink.runner.config.DistributionNode.UniformDistributionNode
import com.lukewassink.runner.config.InterceptorNode.{
  MessageDropInterceptorNode, RandomLatencyInterceptorNode
}
import com.lukewassink.runner.util.{Failure, Result, Success}

// Validates global constraints on a syntax tree.
object Validator {
  def validate(simulationNode: SimulationNode): Result[SimulationNode] =
    val errors = runAllValidators(simulationNode)
    if errors.isEmpty then Success(simulationNode)
    else Failure[SimulationNode](errors)

  private def validationContext(node: SimulationNode): ValidationContext = {
    val indicesByName =
      node.nodes.map(_.name).zipWithIndex
        .groupMap((name, _) => name)((name, idx) => idx)
    ValidationContext(indicesByName)
  }

  private def runAllValidators(node: SimulationNode): List[ConfigException] = {
    val context = validationContext(node)
    validators.foldLeft(List.empty)((errors, validator) =>
      errors ++ validator.run(node, context)
    )
  }

  private val validators: List[Validator] = List(
    DuplicateNodeNamesValidator,
    MissingReferenceNameValidator,
    NegativeTicksPerMillisecondValidator,
    ChancesValidator,
    NegativeRangesValidator
  )
}

case class ValidationContext(nodeIndicesByName: Map[String, List[Int]])

trait Validator:
  def run(
      simulationNode: SimulationNode,
      context: ValidationContext
  ): List[ConfigException]

case object DuplicateNodeNamesValidator extends Validator:
  def run(
      simulationNode: SimulationNode,
      context: ValidationContext
  ): List[ConfigException] =
    context.nodeIndicesByName.filter((_, indices) => indices.length > 1)
      .map(DuplicateNodeNamesException(_, _)).toList

case object MissingReferenceNameValidator extends Validator:
  def run(
      simulationNode: SimulationNode,
      context: ValidationContext
  ): List[ConfigException] = {
    val nodeNames = context.nodeIndicesByName.keySet
    referenceLocations(simulationNode)
      .filterNot(rl => nodeNames.contains(rl.nodeName))
      .map(MissingReferenceNameException(_))
  }

case object NegativeTicksPerMillisecondValidator extends Validator:
  def run(
      simulationNode: SimulationNode,
      context: ValidationContext
  ): List[ConfigException] = {
    val tpm = simulationNode.ticksPerMillisecond
    if tpm > 0 then List.empty
    else
      List(IllegalConfigValueException(
        s"Field ticksPerMillisecond must be > 0, but it is set to $tpm."
      ))
  }

case object ChancesValidator extends Validator:
  def run(
      simulationNode: SimulationNode,
      context: ValidationContext
  ): List[ConfigException] = simulationNode.interceptors.zipWithIndex.collect {
    case (MessageDropInterceptorNode(chance), idx)
        if chance <= 0 || chance >= 1 =>
      IllegalConfigValueException(s"Chances must be in (0, 1), but the interceptor at index $idx had chance $chance.")
  }

case object NegativeRangesValidator extends Validator:
  private def validateUniformDistribution(
      uniformDistributionNode: UniformDistributionNode,
      idx: Int
  ): Option[ConfigException] = {
    val UniformDistributionNode(min, max) = uniformDistributionNode
    if min < max then None
    else
      Some(IllegalConfigValueException(s"A uniform distribution requires max > min, but the interceptor at index $idx has min = $min, max = $max."))
  }

  def run(
      simulationNode: SimulationNode,
      context: ValidationContext
  ): List[ConfigException] =
    simulationNode.interceptors.zipWithIndex.collect {
      case (
            RandomLatencyInterceptorNode(distribution: UniformDistributionNode),
            idx
          ) =>
        validateUniformDistribution(distribution, idx)
    }.flatten
