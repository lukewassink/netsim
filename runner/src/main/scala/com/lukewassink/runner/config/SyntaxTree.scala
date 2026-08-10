package com.lukewassink.runner.config

import com.lukewassink.runner.config.BehaviorNode.{
  SimpleResponderNode, SimpleSenderNode
}
import com.lukewassink.runner.util.{Failure, Result, Success}
import io.github.edadma.hocon.{Config, ConfigObject, Decoder, HoconException}

object SyntaxTree {
  def fromConfig(config: Config): Result[SimulationNode] =
    try Success(config.as[SimulationNode])
    catch case e: (HoconException | IllegalStateException) => Failure(e)

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
        case t                  =>
          throw IllegalStateException(
            s"Behavior type $t is not a valid behavior type"
          )
      }
    }
}

final case class SimulationNode(
    name: String,
    randomSeed: Long,
    network: NetworkNode
)

final case class NetworkNode(nodes: List[NodeNode])

case class NodeNode(name: String, behaviors: List[BehaviorNode])

enum BehaviorNode:
  case SimpleSenderNode(time: Double, receiver: String, content: String)
  case SimpleResponderNode()
