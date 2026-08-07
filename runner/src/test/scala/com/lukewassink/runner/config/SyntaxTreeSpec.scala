package com.lukewassink.runner.config

import com.lukewassink.runner.config.BehaviorNode.{
  SimpleResponderNode, SimpleSenderNode
}
import com.lukewassink.simulation.test_utils.UnitSpec
import io.github.edadma.hocon.{Hocon, MissingPathException}
import com.lukewassink.runner.util.{Success, Failure}

class SyntaxTreeSpec extends UnitSpec {
  describe("fromConfig") {
    it("handles zero nodes") {
      val config = Hocon.parse(
        """
           name = "simulation-name"
           randomSeed = 10

           network {
             nodes = []
           }
        """.stripMargin
      )
      val result = SyntaxTree.fromConfig(config)

      val expectedTree =
        SimulationNode("simulation-name", 10, NetworkNode(List.empty))

      inside(result) { case Success(tree) => tree should equal(expectedTree) }
    }

    it("handles a node with zero behaviors") {
      val config = Hocon.parse(
        """
                 name = "simulation-name"
                 randomSeed = 10

                 network {
                   nodes = [{
                     name = "node-name"
                     behaviors = []
                   }]
                 }
              """.stripMargin
      )
      val result = SyntaxTree.fromConfig(config)

      val expectedTree = SimulationNode(
        "simulation-name",
        10,
        NetworkNode(List(NodeNode("node-name", List.empty)))
      )

      inside(result) { case Success(tree) => tree should equal(expectedTree) }
    }

    it("builds a network") {
      val config = Hocon.parse(
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
                               time = 10
                               receiver = "node-name-2"
                               content = "Hi!"
                             }
                           ]
                         }]
                       }
                    """.stripMargin
      )
      val result = SyntaxTree.fromConfig(config)

      val expectedTree = SimulationNode(
        "simulation-name",
        10,
        NetworkNode(List(
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
        ))
      )

      inside(result) { case Success(tree) => tree should equal(expectedTree) }
    }

    it("returns a parse error as a Failure") {
      val config = Hocon.parse(
        """
                 nam = "simulation-name" // "nam" should be "name", so the path "name" is missing
                 randomSeed = 10

                 network {
                   nodes = []
                 }
              """.stripMargin
      )
      val result = SyntaxTree.fromConfig(config)

      val expectedTree =
        SimulationNode("simulation-name", 10, NetworkNode(List.empty))

      inside(result) { case Failure(List(e)) =>
        e shouldBe a[MissingPathException]
      }
    }
  }
}
