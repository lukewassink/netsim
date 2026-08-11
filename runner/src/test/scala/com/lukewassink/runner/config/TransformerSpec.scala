package com.lukewassink.runner.config

import com.lukewassink.runner.config.BehaviorNode.{
  SimpleResponderNode, SimpleSenderNode
}
import com.lukewassink.runner.core.{Simulation, SimulationMetadata}
import com.lukewassink.runner.util.Success
import com.lukewassink.simulation.behavior.{SimpleResponder, SimpleSender}
import com.lukewassink.simulation.core.MessageStage.Drafted
import com.lukewassink.simulation.core.ResponseState.Request
import com.lukewassink.simulation.core.{
  Message, MessageContent, Network, Node, NodeHeader, NodeState
}
import com.lukewassink.simulation.test_utils.UnitSpec
import com.lukewassink.simulation.util.XORRandom

class TransformerSpec extends UnitSpec {
  describe("TransformContext") {
    val tree = SimulationNode(
      "simulation-name",
      10,
      NetworkNode(List(
        NodeNode("node-name-1", List.empty),
        NodeNode("node-name-2", List.empty),
        NodeNode("node-name-3", List.empty)
      ))
    )

    val context = TransformContext(tree)

    describe("build context from a SimulationNode") {

      it("builds transformation context from a SimuldationNode") {

        assert(
          context === TransformContext(
            Map("node-name-1" -> 0, "node-name-2" -> 1, "node-name-3" -> 2),
            10
          )
        )
      }
    }

    describe("resolveID") {
      it("returns the node ID corresponding to a name") {
        assert(context.resolveID("node-name-1").id === 0)
      }

      it("throws an error if the node ID is not present") {
        assertThrows[IllegalStateException] {
          context.resolveID("missing-node-name")
        }
      }
    }
  }

  describe("transformer.transform") {
    it("handles empty nodes") {
      val tree = SimulationNode("simulation-name", 10, NetworkNode(List.empty))

      val simulation = Simulation(
        SimulationMetadata("simulation-name", 10),
        Network(0, List.empty, List.empty, XORRandom.fromSeed(10))
      )

      assert(Transformer.transform(tree) === Success(simulation))
    }

    it("handles empty behavior") {
      val tree = SimulationNode(
        "simulation-name",
        10,
        NetworkNode(List(NodeNode("node-name", List.empty)))
      )

      val simulation = Simulation(
        SimulationMetadata("simulation-name", 10),
        Network(
          0,
          List(Node(
            List.empty,
            NodeState(
              NodeHeader(0, 0),
              List.empty,
              List.empty,
              XORRandom.fromSeed(10, 1)
            )
          )),
          List.empty,
          XORRandom.fromSeed(10)
        )
      )

      assert(Transformer.transform(tree) === Success(simulation))
    }

    it("handles a complex simulation") {
      val tree = SimulationNode(
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
              SimpleSenderNode(15, "node-name-2", "Hi!")
            )
          )
        ))
      )

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

      assert(Transformer.transform(tree) === Success(simulation))
    }
  }
}
