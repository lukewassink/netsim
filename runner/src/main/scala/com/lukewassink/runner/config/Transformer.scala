package com.lukewassink.runner.config

import com.lukewassink.runner.config.BehaviorNode.{
  SimpleResponderNode, SimpleSenderNode
}
import com.lukewassink.runner.config.DistributionNode.{
  LogNormalDistributionNode, NormalDistributionNode, UniformDistributionNode
}
import com.lukewassink.runner.config.InterceptorNode.{
  MessageDropInterceptorNode, RandomLatencyInterceptorNode
}
import com.lukewassink.runner.core.{Simulation, SimulationMetadata}
import com.lukewassink.runner.util.{Failure, Result, Success}
import com.lukewassink.simulation.behavior.{SimpleResponder, SimpleSender}
import com.lukewassink.simulation.core.MessageStage.Drafted
import com.lukewassink.simulation.core.NodeID.NodeID
import com.lukewassink.simulation.core.{
  DeliveryQueue, ExecutionContext, Message, MessageContent, MessageID, Network,
  Node, NodeBehavior, NodeHeader, NodeID, NodeState
}
import com.lukewassink.simulation.core.ResponseState.Request
import com.lukewassink.simulation.interceptor.{
  MessageDropInterceptor, MessageInterceptor, RandomLatencyInterceptor
}
import com.lukewassink.simulation.util.{
  Chance, Distribution, LogNormalDistribution, NormalDistribution, Time,
  UniformDistribution
}

// Context used by the transformers to build the Simulation.
case class TransformContext(nameToID: Map[String, NodeID]):
  // This method should never fail to find an ID for a name unless there is a bug in syntax tree validation
  // or in constructing the transform context, so throw an error if it does fail.
  def resolveID(name: String): NodeID =
    nameToID.get(name) match {
      case Some(nodeID) => nodeID
      case None         =>
        throw IllegalStateException(
          s"Internal error: no node ID for node name $name. This indicates a bug in validation or transformation."
        )
    }

object TransformContext:
  def apply(simulation: SimulationNode): TransformContext = {
    val nameToID =
      simulation.nodes.map(_.name).zipWithIndex
        .map((name, id) => name -> NodeID(id)).toMap
    TransformContext(nameToID)
  }

// Transforms a syntax tree into an actual Simulation with a network.
// This includes assigning node IDs and resolving node name references.
object Transformer {
  def transform(simulationNode: SimulationNode): Result[Simulation] =
    try {
      given TransformContext = TransformContext(simulationNode)

      Success(Simulation(
        SimulationMetadata(simulationNode.name, simulationNode.randomSeed),
        Network(
          ExecutionContext(
            Time(0),
            simulationNode.ticksPerMillisecond,
            simulationNode.randomSeed
          ),
          simulationNode.nodes.map(transformNode),
          DeliveryQueue(
            simulationNode.interceptors.map(transformInterceptor),
            List.empty
          )
        )
      ))
    } catch { case e: IllegalStateException => Failure(e) }

  private def transformInterceptor(using
      context: TransformContext
  )(interceptorNode: InterceptorNode): MessageInterceptor =
    interceptorNode match {
      case MessageDropInterceptorNode(chance) =>
        MessageDropInterceptor(Chance(chance))
      case RandomLatencyInterceptorNode(distribution) =>
        RandomLatencyInterceptor(transformDistribution(distribution))
    }

  private def transformDistribution(using
      context: TransformContext
  )(distributionNode: DistributionNode): Distribution =
    distributionNode match {
      case UniformDistributionNode(min, max) => UniformDistribution(min, max)
      case NormalDistributionNode(mean, stDev) => NormalDistribution(mean, stDev)
      case LogNormalDistributionNode(logMean, logStdDev) =>
        LogNormalDistribution(logMean, logStdDev)
    }

  private def transformNode(using
      context: TransformContext
  )(nodeNode: NodeNode): Node = {
    val id    = context.resolveID(nodeNode.name)
    val state = NodeState(NodeHeader(id, MessageID(0)), List.empty, List.empty)
    Node(nodeNode.behaviors.map(transformBehavior), state)
  }

  private def transformBehavior(using
      context: TransformContext
  )(behaviorNode: BehaviorNode): NodeBehavior =
    behaviorNode match {
      case SimpleSenderNode(time, receiver, content) =>
        SimpleSender(
          Time(time),
          Message[Drafted](
            Drafted(context.resolveID(receiver)),
            Request(),
            MessageContent(content)
          )
        )
      case SimpleResponderNode() => SimpleResponder()
    }
}
