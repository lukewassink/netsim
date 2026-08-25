package com.lukewassink.runner.core

import com.lukewassink.runner.config.{
  DuplicateNodeNamesException, MissingBehaviorTypeException
}
import com.lukewassink.runner.core.Runner
import com.lukewassink.runner.util.{Failure, Success}
import com.lukewassink.simulation.behavior.{
  ReliableBroadcaster, ReliableBroadcasterConfig, SimpleResponder, SimpleSender
}
import com.lukewassink.simulation.message.MessageStage.Drafted
import com.lukewassink.simulation.message.ResponseState.Request
import com.lukewassink.simulation.core.{Network, Node, NodeHeader, NodeState}
import com.lukewassink.simulation.message.RecipientSpecification.Single
import com.lukewassink.simulation.message.{Content, DeliverySemantics, Message}
import com.lukewassink.simulation.test_utils.UnitSpec
import io.github.edadma.hocon.HoconException

class RunnerSpec extends UnitSpec {
  describe("run (from config)") {
    it("generates a simulation from a config") {
      val config =
        """
           name = "simulation-name"
           randomSeed = 10

           nodes = [{
               name = "node-name-1"
               behaviors = []
             }
             {
               name = "node-name-2"
               behaviors = [{type = "simple-responder"}]
             }
             {
               name = "node-name-3"
               behaviors = [
                 {type = "simple-responder"}
                 {type = "simple-responder"}
                 {
                   type = "simple-sender"
                   time = 15
                   receiver = "node-name-2"
                   content = "Hi!"
                 }
               ]
            }]
        """.stripMargin

      val result = Runner.run(config)

      val simulation = Simulation(
        SimulationMetadata("simulation-name", 10),
        Network(
          0,
          List(
            Node(List.empty, NodeState(NodeHeader(0, 0), List.empty, List.empty)),
            Node(
              List(SimpleResponder()),
              NodeState(NodeHeader(1, 0), List.empty, List.empty)
            ),
            Node(
              List(
                SimpleResponder(),
                SimpleResponder(),
                SimpleSender(
                  15,
                  Message[Drafted](
                    Drafted(Single(1)),
                    DeliverySemantics.empty,
                    Content("Hi!")
                  )
                )
              ),
              NodeState(NodeHeader(2, 0), List.empty, List.empty)
            )
          ),
          List.empty
        )
      )

      inside(result) { case Success(s) => s === simulation }
    }

    it("transforms a reliable broadcaster") {
      val config =
        """
             name = "simulation-name"
             randomSeed = 10

             nodes = [{
                   name = "node-1"
                   behaviors = [{
                       type = reliable-broadcaster
                       ackTimeout = 100
                       maxRetries = 3
                       dedupeTimeout = 1000
                     }]
                 }]
        """.stripMargin

      val result = Runner.run(config)

      val simulation = Simulation(
        SimulationMetadata("simulation-name", 10),
        Network(
          0,
          List(Node(
            List(
              ReliableBroadcaster.empty(ReliableBroadcasterConfig(100, 3, 1000))
            ),
            NodeState(NodeHeader(0, 0), List.empty, List.empty)
          )),
          List.empty
        )
      )

      inside(result) { case Success(s) => s === simulation }
    }
  }

  it("returns a Failure if there are HOCON parsing errors") {
    val config: String =
      """
         name = "simulation-name"
         randomSeed = 10

         nodes = [{
             name = "node-name-1"
             behaviors = []
           }]x // 'x' here is a syntax error
      """.stripMargin

    val result = Runner.run(config)
    inside(result) { case Failure(List(e)) => e shouldBe a[HoconException] }
  }

  it("returns a Failure if there is a missing behavior type") {
    val config: String =
      """
             name = "simulation-name"
             randomSeed = 10

             nodes = [{
                 name = "node-name-1"
                 behaviors = [{type = "missing-type"}]
               }]
          """.stripMargin

    val result = Runner.run(config)
    inside(result) { case Failure(List(e)) =>
      e shouldBe a[MissingBehaviorTypeException]
    }
  }

  it("returns a Failure if there are validation errors") {
    val config: String =
      """
             name = "simulation-name"
             randomSeed = 10

             nodes = [{
                 name = "node-name-1"
                 behaviors = []
               }
               {
                 name = "node-name-1"
                 behaviors = []
               }]
          """.stripMargin

    val result = Runner.run(config)
    inside(result) { case Failure(List(e)) =>
      e shouldBe a[DuplicateNodeNamesException]
    }
  }
}
