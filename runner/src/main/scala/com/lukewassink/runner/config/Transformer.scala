package com.lukewassink.runner.config

import com.lukewassink.runner.config.BehaviorNode.{
  SimpleResponderNode, SimpleSenderNode
}
import com.lukewassink.runner.config.{
  BooleanDistributionNode, DistributionNode, LogNormalDistributionNode,
  NormalDistributionNode, UniformDistributionNode
}
import com.lukewassink.runner.config.InterceptorNode.{
  MessageDropInterceptorNode, RandomLatencyInterceptorNode
}
import com.lukewassink.runner.core.{Simulation, SimulationMetadata}
import com.lukewassink.runner.util.{Failure, Result, Success}
import com.lukewassink.simulation.behavior.{
  Behavior, SimpleResponder, SimpleSender
}
import com.lukewassink.simulation.core.MessageStage.Drafted
import com.lukewassink.simulation.core.NodeID.NodeID
import com.lukewassink.simulation.core.{
  DeliveryQueue, ExecutionContext, Message, MessageContent, MessageID, Network,
  Node, NodeHeader, NodeID, NodeState
}
import com.lukewassink.simulation.core.ResponseState.Request
import com.lukewassink.simulation.interceptor.{
  MessageDropInterceptor, MessageInterceptor, RandomLatencyInterceptor
}
import com.lukewassink.simulation.util.{
  BooleanDistribution, Chance, Distribution, LogNormalDistribution,
  NormalDistribution, Time, UniformDistribution
}

// Refactor to use Transformer[A, B] type classes.
// Give [A <: SyntaxTreeNode] an extension method A.to[B](using ctx: TransformContext) that uses Transformer[A, B]

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

trait Transformer[A, B]:
  def transform(using syntaxTreeNode: A, ctx: TransformContext): B

// Sadly, we need this because of type erasure.
trait DistributionTransformer[T]
    extends Transformer[DistributionNode[T], Distribution[T]]

// Transforms a syntax tree into an actual Simulation with a network.
// This includes assigning node IDs and resolving node name references.
object Transformer {
  extension [A <: SyntaxTreeNode](syntaxTreeNode: A)
    def to[B](using
        transformer: Transformer[A, B],
        transformContext: TransformContext
    ): B = {
      given A = syntaxTreeNode
      transformer.transform
    }

  def transform(simulationNode: SimulationNode): Result[Simulation] =
    try {
      given TransformContext = TransformContext(simulationNode)
      Success(simulationNode.to[Simulation])
    } catch { case e: IllegalStateException => Failure(e) }

  given Transformer[SimulationNode, Simulation] with
    def transform(using
        simulationNode: SimulationNode,
        ctx: TransformContext
    ): Simulation = Simulation(
      SimulationMetadata(simulationNode.name, simulationNode.randomSeed),
      Network(
        ExecutionContext(
          Time(0),
          simulationNode.ticksPerMillisecond,
          simulationNode.randomSeed
        ),
        simulationNode.nodes.map(_.to[Node]),
        DeliveryQueue(
          simulationNode.interceptors.map(_.to[MessageInterceptor]),
          List.empty
        )
      )
    )

  given Transformer[InterceptorNode, MessageInterceptor] with
    def transform(using
        interceptorNode: InterceptorNode,
        ctx: TransformContext
    ): MessageInterceptor =
      interceptorNode match {
        case MessageDropInterceptorNode(d) =>
          MessageDropInterceptor(d.to[Distribution[Boolean]])
        case RandomLatencyInterceptorNode(d) =>
          RandomLatencyInterceptor(d.to[Distribution[Double]])
      }

  given [A: DistributionTransformer]
      : Transformer[DistributionNode[A], Distribution[A]] with
    def transform(using
        distributionNode: DistributionNode[A],
        ctx: TransformContext
    ): Distribution[A] = summon[DistributionTransformer[A]].transform

  given DistributionTransformer[Double] with
    def transform(using
        distributionNode: DistributionNode[Double],
        ctx: TransformContext
    ): Distribution[Double] =
      distributionNode match {
        case UniformDistributionNode(min, max) => UniformDistribution(min, max)
        case NormalDistributionNode(mean, stDev) =>
          NormalDistribution(mean, stDev)
        case LogNormalDistributionNode(logMean, logStdDev) =>
          LogNormalDistribution(logMean, logStdDev)
      }

  given DistributionTransformer[Boolean] with
    def transform(using
        distributionNode: DistributionNode[Boolean],
        ctx: TransformContext
    ): Distribution[Boolean] =
      distributionNode match {
        case BooleanDistributionNode(probability) =>
          BooleanDistribution(probability)
      }

  given Transformer[NodeNode, Node] with
    def transform(using nodeNode: NodeNode, ctx: TransformContext): Node = {
      val id = ctx.resolveID(nodeNode.name)
      val state = NodeState(NodeHeader(id, MessageID(0)), List.empty, List.empty)
      Node(nodeNode.behaviors.map(_.to[Behavior]), state)
    }

  given Transformer[BehaviorNode, Behavior] with
    def transform(using
        behaviorNode: BehaviorNode,
        ctx: TransformContext
    ): Behavior =
      behaviorNode match {
        case SimpleSenderNode(time, receiver, content) =>
          SimpleSender(
            Time(time),
            Message[Drafted](
              Drafted(ctx.resolveID(receiver)),
              Request(),
              MessageContent(content)
            )
          )
        case SimpleResponderNode() => SimpleResponder()
      }
}
