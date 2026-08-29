package com.lukewassink.runner.config

import com.lukewassink.runner.config.BehaviorNode.*
import com.lukewassink.runner.config.InterceptorNode.*
import com.lukewassink.runner.config.SyntaxNodeDefaults.{
  defaultNodeNodeConfig, defaultReliableBroadcasterNodeConfig,
  defaultSimpleSenderNodeConfig, defaultSimulationNodeConfig
}
import com.lukewassink.runner.util.{Failure, Result, Success}
import io.github.edadma.hocon.{
  Config, ConfigObject, ConfigValue, Decoder, Hocon, HoconException
}

object SyntaxTree {
  def fromConfig(config: Config): Result[SimulationNode] =
    try
      Success(config.withFallback(defaultSimulationNodeConfig).as[SimulationNode])
    catch case e: (HoconException | ConfigException) => Failure(e)

  // Extract the object at path.
  // Error if there is no object at path.
  private def parseObject(
      configValue: ConfigValue,
      path: String,
      pluralizedNodeType: String // Appears in error message.
  ): Config =
    configValue match {
      case o: ConfigObject => Config(o)
      case other           =>
        throw HoconException(
          s"$pluralizedNodeType must be objects, but configuration value at $path was not an object"
        )
    }

  given Decoder[NodeNode] =
    (value, path) =>
      parseObject(value, path, "Nodes").withFallback(defaultNodeNodeConfig)
        .as[NodeNode]

  given Decoder[BehaviorNode] =
    (value, path) =>
      val config = parseObject(value, path, "Behaviors")
      config.getString("type") match {
        case "simple-sender" =>
          config.withFallback(defaultSimpleSenderNodeConfig).as[SimpleSenderNode]
        case "simple-responder"     => config.as[SimpleResponderNode]
        case "reliable-broadcaster" =>
          config.withFallback(defaultReliableBroadcasterNodeConfig)
            .as[ReliableBroadcasterNode]
        case t => throw MissingBehaviorTypeException(t)
      }

  given Decoder[InterceptorNode] =
    (value, path) =>
      val config = parseObject(value, path, "Interceptors")
      config.getString("type") match {
        case "message-drop"   => config.as[MessageDropInterceptorNode]
        case "random-latency" => config.as[RandomLatencyInterceptorNode]
        case t                => throw MissingInterceptorTypeException(t)
      }

  trait DistributionDecoder[A] extends Decoder[DistributionNode[A]]

  given [A: DistributionDecoder]: Decoder[DistributionNode[A]] =
    (value, path) => summon[DistributionDecoder[A]].decode(value, path)

  given DistributionDecoder[Double] =
    (value, path) =>
      val config = parseObject(value, path, "Distributions")
      config.getString("type") match {
        case "uniform"    => config.as[UniformDistributionNode]
        case "normal"     => config.as[NormalDistributionNode]
        case "log-normal" => config.as[LogNormalDistributionNode]
        case t            => throw MissingDistributionTypeException(t, "Double")
      }

  given DistributionDecoder[Boolean] =
    (value, path) =>
      val config = parseObject(value, path, "Distributions")
      config.getString("type") match {
        case "boolean" => config.as[BooleanDistributionNode]
        case t         => throw MissingDistributionTypeException(t, "Boolean")
      }
}

// All syntax tree nodes should extend this trait.
trait SyntaxTreeNode

final case class SimulationNode(
    name: String,
    randomSeed: Long,
    ticksPerMillisecond: Double,
    interceptors: List[InterceptorNode],
    nodes: List[NodeNode]
) extends SyntaxTreeNode

enum InterceptorNode extends SyntaxTreeNode:
  case MessageDropInterceptorNode(distribution: DistributionNode[Boolean])
  case RandomLatencyInterceptorNode(distribution: DistributionNode[Double])

sealed trait DistributionNode[T] extends SyntaxTreeNode

case class UniformDistributionNode(min: Double, max: Double)
    extends DistributionNode[Double]
case class NormalDistributionNode(mean: Double, stDev: Double)
    extends DistributionNode[Double]
case class LogNormalDistributionNode(logMean: Double, logStdDev: Double)
    extends DistributionNode[Double]
case class BooleanDistributionNode(probability: Double)
    extends DistributionNode[Boolean]

case class NodeNode(name: String, behaviors: List[BehaviorNode])
    extends SyntaxTreeNode

enum BehaviorNode extends SyntaxTreeNode:
  case SimpleSenderNode(
      time: Double,
      receiver: String,
      content: String,
      isReliable: Boolean
  )
  case SimpleResponderNode()
  case ReliableBroadcasterNode(
      incomingAckTimeout: Double,
      maxRetries: Int,
      dedupeTimeout: Double,
      outgoingAckTimeout: Double
  )
