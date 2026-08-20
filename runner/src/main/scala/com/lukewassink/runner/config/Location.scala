package com.lukewassink.runner.config

import com.lukewassink.runner.config.BehaviorNode.{
  SimpleResponderNode, SimpleSenderNode
}
import com.lukewassink.runner.config.InterceptorNode.{
  MessageDropInterceptorNode, RandomLatencyInterceptorNode
}

// This could probably use some refactoring as the project grows.

// Used in validation to track the location of nodes in the syntax tree.
sealed trait Location:
  // Print the location of the node for use in error messages.
  def prettyPrint: String

case class BehaviorLocation(
    nodeName: String,
    nodeIndex: Int,
    behavior: String,
    behaviorIndex: Int
) extends Location:
  def prettyPrint: String =
    s"node: $nodeName (index $nodeIndex), in behavior: $behavior (index $behaviorIndex)"

object BehaviorLocation {
  // Returns a list of node names referenced in behaviors along with their locations.
  def referenceLocations(
      simulationNode: SimulationNode
  ): List[ReferenceLocation] =
    simulationNode.nodes.zipWithIndex
      .foldLeft(List.empty)((list, nodeWithIndex) =>
        list ::: nodeReferenceLocations(nodeWithIndex._1, nodeWithIndex._2)
      )

  private def nodeReferenceLocations(
      node: NodeNode,
      nodeIndex: Int
  ): List[ReferenceLocation] =
    node.behaviors.zipWithIndex
      .foldLeft(List.empty) { (list, behaviorWithIndex) =>
        val (behavior, index) = behaviorWithIndex
        val location          = BehaviorLocation(
          node.name,
          nodeIndex,
          behavior.getClass.getSimpleName,
          index
        )
        list ::: references(behavior).map(ReferenceLocation(_, location))
      }
}

private def references(behavior: BehaviorNode): List[String] =
  behavior match {
    case SimpleSenderNode(_, nodeName, _) => List(nodeName)
    case SimpleResponderNode()            => List.empty
  }

// The location of a reference to another Node.
case class ReferenceLocation(nodeName: String, location: Location)

object ReferenceLocation:
  // If other syntax tree node types contain references, we can add to the list here.
  def locations(simulationNode: SimulationNode): List[ReferenceLocation] =
    BehaviorLocation.referenceLocations(simulationNode)

case class InterceptorLocation(index: Int) extends Location:
  override def prettyPrint: String = s"interceptor with index $index"

case class DistributionLocation(
    distributionNode: DistributionNode[?],
    location: Location
)

object DistributionLocation {
  def locations(simulationNode: SimulationNode): List[DistributionLocation] =
    simulationNode.interceptors.zipWithIndex.flatMap((interceptor, idx) =>
      interceptorDistributions(interceptor)
        .map(d => DistributionLocation(d, InterceptorLocation(idx)))
    )

  private def interceptorDistributions(
      interceptorNode: InterceptorNode
  ): List[DistributionNode[?]] =
    interceptorNode match {
      case MessageDropInterceptorNode(d)   => List(d)
      case RandomLatencyInterceptorNode(d) => List(d)
    }
}
