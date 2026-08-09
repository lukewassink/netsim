package com.lukewassink.runner.core

import com.lukewassink.runner.config.Transformer
import com.lukewassink.runner.core.Runner
import com.lukewassink.runner.util.Success
import com.lukewassink.simulation.behavior.{SimpleResponder, SimpleSender}
import com.lukewassink.simulation.core.MessageStage.{Drafted, Pending, Scheduled}
import com.lukewassink.simulation.core.ResponseState.Request
import com.lukewassink.simulation.core.{
  Message, MessageContent, Network, Node, NodeHeader, NodeState
}
import com.lukewassink.simulation.test_utils.MessageSpecUtil.{
  draftedMessage, scheduledMessage
}
import com.lukewassink.simulation.test_utils.NetworkSpecUtil.testNetwork
import com.lukewassink.simulation.test_utils.NodeStateSpecUtil.testNodeState
import com.lukewassink.simulation.test_utils.{MessageMatchers, UnitSpec}
import com.lukewassink.simulation.util.XORRandom
import io.github.edadma.hocon.Hocon

class RunnerSpec extends UnitSpec {
  describe("run (from config)") {
    it("generates a simulation from a config") {
      val config =
        """
           name = "simulation-name"
           randomSeed = 10
           
           network {
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
           }
        """.stripMargin

      val result = Runner.run(config)

      val simulation = Simulation(
        SimulationMetadata("simulation-name", 10),
        Network(
          0,
          List(
            Node(
              List.empty,
              NodeState(
                NodeHeader(0, 0),
                List.empty,
                List.empty,
                XORRandom.fromSeed(10, 1)
              )
            ),
            Node(
              List(SimpleResponder()),
              NodeState(
                NodeHeader(1, 0),
                List.empty,
                List.empty,
                XORRandom.fromSeed(10, 2)
              )
            ),
            Node(
              List(
                SimpleResponder(),
                SimpleResponder(),
                SimpleSender(
                  15,
                  Message[Drafted](Drafted(1), Request(), MessageContent("Hi!"))
                )
              ),
              NodeState(
                NodeHeader(2, 0),
                List.empty,
                List.empty,
                XORRandom.fromSeed(10, 3)
              )
            )
          ),
          List.empty,
          XORRandom.fromSeed(10)
        )
      )

      inside(result) { case Success(s) => s === simulation }
    }
  }
}
