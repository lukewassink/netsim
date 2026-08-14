package com.lukewassink.runner.config

import com.lukewassink.runner.config.BehaviorNode.*
import com.lukewassink.runner.config.InterceptorNode.*
import com.lukewassink.runner.config.DistributionNode.*
import com.lukewassink.runner.config.SyntaxNodeDefaults.defaultSimulationNode
import com.lukewassink.runner.util.{Failure, Result, Success}
import io.github.edadma.hocon.{
  Config, ConfigObject, Decoder, Hocon, HoconException
}

object SyntaxTree {
  def fromConfig(config: Config): Result[SimulationNode] =
    try
      Success(
        config.withFallback(Hocon.parse(defaultSimulationNode))
          .as[SimulationNode]
      )
    catch case e: (HoconException | ConfigException) => Failure(e)

  given Decoder[BehaviorNode] =
    (value, path) => {
      val config =
        value match {
          case o: ConfigObject => Config(o)
          case other           =>
            throw HoconException(
              s"Behaviors must be objects, but configuration value at $path was not an object"
            )
        }

      config.getString("type") match {
        case "simple-sender"    => config.as[SimpleSenderNode]
        case "simple-responder" => config.as[SimpleResponderNode]
        case t                  => throw MissingBehaviorTypeException(t)
      }
    }

  given Decoder[InterceptorNode] =
    (value, path) => {
      val config =
        value match {
          case o: ConfigObject => Config(o)
          case other           =>
            throw HoconException(
              s"Interceptors must be objects, but configuration value at $path was not an object"
            )
        }

      config.getString("type") match {
        case "message-drop"   => config.as[MessageDropInterceptorNode]
        case "random-latency" => config.as[RandomLatencyInterceptorNode]
        case t                => throw MissingInterceptorTypeException(t)
      }
    }

  given Decoder[DistributionNode] =
    (value, path) => {
      val config =
        value match {
          case o: ConfigObject => Config(o)
          case other           =>
            throw HoconException(
              s"Distributions must be objects, but configuration value at $path was not an object"
            )
        }

      config.getString("type") match {
        case "uniform"    => config.as[UniformDistributionNode]
        case "normal"     => config.as[NormalDistributionNode]
        case "log-normal" => config.as[LogNormalDistributionNode]
        case t            => throw MissingBehaviorTypeException(t)
      }
    }
}

final case class SimulationNode(
    name: String,
    randomSeed: Long,
    ticksPerMillisecond: Double,
    interceptors: List[InterceptorNode],
    network: NetworkNode
)

enum InterceptorNode:
  case MessageDropInterceptorNode(chance: Double)
  case RandomLatencyInterceptorNode(distribution: DistributionNode)

enum DistributionNode:
  case UniformDistributionNode(min: Double, max: Double)
  case NormalDistributionNode(mean: Double, stDev: Double)
  case LogNormalDistributionNode(logMean: Double, logStdDev: Double)

final case class NetworkNode(nodes: List[NodeNode])

case class NodeNode(name: String, behaviors: List[BehaviorNode])

enum BehaviorNode:
  case SimpleSenderNode(time: Double, receiver: String, content: String)
  case SimpleResponderNode()
