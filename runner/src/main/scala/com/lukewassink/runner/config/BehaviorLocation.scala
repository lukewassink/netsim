package com.lukewassink.runner.config

import com.lukewassink.runner.config.BehaviorNode.{
  SimpleResponderNode, SimpleSenderNode
}

// Used in validation to track the location of a BehaviorNode in the syntax tree
// so it can be printed in error messages.
case class BehaviorLocation(
    nodeName: String,
    nodeIndex: Int,
    behavior: String,
    behaviorIndex: Int
):
  def prettyPrint: String =
    s"node: $nodeName (index $nodeIndex), in behavior: $behavior (index $behaviorIndex)"

case class ReferenceLocation(nodeName: String, location: BehaviorLocation)

object BehaviorLocation:
  // Returns a list of node names referenced in behaviors along with their locations.
  def referenceLocations(
      simulationNode: SimulationNode
  ): List[ReferenceLocation] =
    simulationNode.network.nodes.zipWithIndex
      .foldLeft(List.empty)((list, nodeWithIndex) =>
        list ::: referenceLocations(nodeWithIndex._1, nodeWithIndex._2)
      )

  private def referenceLocations(
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

  private def references(behavior: BehaviorNode): List[String] =
    behavior match {
      case SimpleSenderNode(_, nodeName, _) => List(nodeName)
      case SimpleResponderNode()            => List.empty
    }
