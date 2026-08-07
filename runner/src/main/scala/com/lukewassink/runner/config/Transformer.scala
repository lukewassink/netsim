package com.lukewassink.runner.config

import com.lukewassink.runner.config.BehaviorNode.{
  SimpleResponderNode, SimpleSenderNode
}
import com.lukewassink.runner.core.{Simulation, SimulationMetadata}
import com.lukewassink.runner.util.Result
import com.lukewassink.simulation.behavior.{SimpleResponder, SimpleSender}
import com.lukewassink.simulation.core.MessageStage.Drafted
import com.lukewassink.simulation.core.ResponseState.Request
import com.lukewassink.simulation.core.{
  Message, MessageContent, MessageID, Network, Node, NodeBehavior, NodeHeader,
  NodeID, NodeState
}
import com.lukewassink.simulation.util.{Time, XORRandom}

// Context used by the transformers to build the Simulation.
case class TransformContext(nameToID: Map[String, NodeID], randomSeed: Long):
  // This method should never fail to find an ID for a name unless there is a bug in syntax tree validation
  // or in constructing the transform context, so throw an error if it does fail.
  def resolveID(name: String): NodeID = nameToID.get(name) match {
    case Some(nodeID) => nodeID
    case None         => throw IllegalStateException(
        s"Internal error: no node ID for node name $name. This indicates a bug in validation or transformation."
      )
  }

object TransformContext:
  def apply(simulation: SimulationNode): TransformContext = {
    val nameToID = simulation.network.nodes.map(_.name).zipWithIndex
      .map((name, id) => name -> NodeID(id)).toMap
    TransformContext(nameToID, simulation.randomSeed)
  }

// Transforms a syntax tree into an actual Simulation with a network.
// This includes assigning node IDs and resolving node name references.
object Transformer {
  def transform(result: Result[SimulationNode]): Result[Simulation] = result
    .map(transform)

  private def transform(simulationNode: SimulationNode): Simulation = {
    given TransformContext = TransformContext(simulationNode)

    Simulation(
      SimulationMetadata(simulationNode.name, simulationNode.randomSeed),
      transform(simulationNode.network)
    )
  }

  private def transform(using
      context: TransformContext
  )(networkNode: NetworkNode): Network = Network(
    Time(0),
    networkNode.nodes.map(transform),
    List.empty,
    XORRandom.fromSeed(context.randomSeed)
  )

  private def transform(using
      context: TransformContext
  )(nodeNode: NodeNode): Node = {
    val id    = context.resolveID(nodeNode.name)
    val state = NodeState(
      NodeHeader(id, MessageID(0)),
      List.empty,
      List.empty,
      XORRandom.fromSeed(context.randomSeed, id.id + 1)
    )
    Node(nodeNode.behaviors.map(transform), state)
  }

  private def transform(using
      context: TransformContext
  )(behaviorNode: BehaviorNode): NodeBehavior = behaviorNode match {
    case SimpleSenderNode(time, receiver, content) => SimpleSender(
        Time(time),
        Message[Drafted](
          Drafted(context.resolveID(receiver)),
          Request(),
          MessageContent(content)
        )
      )
    case SimpleResponderNode()                     => SimpleResponder()
  }
}
