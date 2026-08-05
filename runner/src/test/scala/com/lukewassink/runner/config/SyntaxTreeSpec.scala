package com.lukewassink.runner.config

import com.lukewassink.runner.config.BehaviorNode.{
  SimpleResponderNode,
  SimpleSenderNode
}
import com.lukewassink.simulation.test_utils.UnitSpec
import io.github.edadma.hocon.Hocon

class SyntaxTreeSpec extends UnitSpec {
  describe("fromConfig") {
    it("handles zero nodes") {
      val config = Hocon.parse("""
           name = "simulation-name"
           randomSeed = 10

           network {
             nodes = []
           }
        """.stripMargin)
      val tree = SyntaxTree.fromConfig(config)

      val expectedTree =
        SimulationNode("simulation-name", 10, NetworkNode(List.empty))
    }

    it("handles a node with zero behaviors") {
      val config = Hocon.parse("""
                 name = "simulation-name"
                 randomSeed = 10

                 network {
                   nodes = [{
                     name = "node-name"
                     behaviors = []
                   }]
                 }
              """.stripMargin)
      val tree = SyntaxTree.fromConfig(config)

      val expectedTree =
        SimulationNode(
          "simulation-name",
          10,
          NetworkNode(List(NodeNode("node-name", List.empty)))
        )
    }

    it("builds a network") {
      val config = Hocon.parse("""
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
                               time = 10
                               receiver = "node-name-2"
                               content = "Hi!"
                             }
                           ]
                         }]
                       }
                    """.stripMargin)
      val tree = SyntaxTree.fromConfig(config)

      val expectedTree =
        SimulationNode(
          "simulation-name",
          10,
          NetworkNode(
            List(
              NodeNode("node-name-1", List.empty),
              NodeNode("node-name-2", List(SimpleResponderNode())),
              NodeNode(
                "node-name-3",
                List(
                  SimpleResponderNode(),
                  SimpleResponderNode(),
                  SimpleSenderNode(10, "node-name-2", "Hi!")
                )
              )
            )
          )
        )
    }
  }
}
