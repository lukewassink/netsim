package com.lukewassink.runner.config

import com.lukewassink.runner.config.BehaviorNode.{
  SimpleResponderNode, SimpleSenderNode
}
import com.lukewassink.runner.config.{
  LogNormalDistributionNode, NormalDistributionNode, UniformDistributionNode
}
import com.lukewassink.runner.config.InterceptorNode.{
  MessageDropInterceptorNode, RandomLatencyInterceptorNode
}
import com.lukewassink.runner.core.{Simulation, SimulationMetadata}
import com.lukewassink.runner.util.Success
import com.lukewassink.simulation.behavior.{SimpleResponder, SimpleSender}
import com.lukewassink.simulation.message.MessageStage.Drafted
import com.lukewassink.simulation.message.ResponseState.Request
import com.lukewassink.simulation.core.NodeID.NodeID
import com.lukewassink.simulation.core.{
  DeliveryQueue, ExecutionContext, Network, Node, NodeHeader, NodeID, NodeState
}
import com.lukewassink.simulation.interceptor.{
  MessageDropInterceptor, RandomLatencyInterceptor
}
import com.lukewassink.simulation.message.RecipientSpecification.Single
import com.lukewassink.simulation.message.{DeliverySemantics, Message, Content}
import com.lukewassink.simulation.test_utils.UnitSpec
import com.lukewassink.simulation.util.{
  BooleanDistribution, LogNormalDistribution, NormalDistribution,
  UniformDistribution
}

class TransformerSpec extends UnitSpec {
  describe("TransformContext") {
    val tree = SimulationNode(
      "simulation-name",
      10,
      1,
      List.empty,
      List(
        NodeNode("node-name-1", List.empty),
        NodeNode("node-name-2", List.empty),
        NodeNode("node-name-3", List.empty)
      )
    )

    val context = TransformContext(tree)

    describe("build context from a SimulationNode") {

      it("builds transformation context from a SimulationNode") {

        assert(
          context === TransformContext(
            1,
            Map[String, NodeID](
              "node-name-1" -> 0,
              "node-name-2" -> 1,
              "node-name-3" -> 2
            )
          )
        )
      }
    }

    describe("resolveID") {
      it("returns the node ID corresponding to a name") {
        assert(context.resolveID("node-name-1") === NodeID(0))
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
      val tree = SimulationNode("simulation-name", 10, 1, List.empty, List.empty)

      val simulation = Simulation(
        SimulationMetadata("simulation-name", 10),
        Network(ExecutionContext(0, 1, 10), List.empty, List.empty)
      )

      assert(Transformer.transform(tree) === Success(simulation))
    }

    it("handles empty behavior") {
      val tree = SimulationNode(
        "simulation-name",
        10,
        1,
        List.empty,
        List(NodeNode("node-name", List.empty))
      )

      val simulation = Simulation(
        SimulationMetadata("simulation-name", 10),
        Network(
          ExecutionContext(0, 1, 10),
          List(
            Node(List.empty, NodeState(NodeHeader(0, 0), List.empty, List.empty))
          ),
          List.empty
        )
      )

      assert(Transformer.transform(tree) === Success(simulation))
    }

    it("handles a complex simulation") {
      val tree = SimulationNode(
        "simulation-name",
        10,
        6,
        List.empty,
        List(
          NodeNode("node-name-1", List.empty),
          NodeNode("node-name-2", List(SimpleResponderNode())),
          NodeNode(
            "node-name-3",
            List(
              SimpleResponderNode(),
              SimpleResponderNode(),
              SimpleSenderNode(15, "node-name-2", "Hi!", false)
            )
          )
        )
      )

      val simulation = Simulation(
        SimulationMetadata("simulation-name", 10),
        Network(
          ExecutionContext(0, 6, 10),
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

      assert(Transformer.transform(tree) === Success(simulation))
    }

    it("handles interceptors") {
      val tree = SimulationNode(
        "simulation-name",
        10,
        1,
        List(
          MessageDropInterceptorNode(BooleanDistributionNode(0.63)),
          RandomLatencyInterceptorNode(UniformDistributionNode(6, 12.1)),
          RandomLatencyInterceptorNode(NormalDistributionNode(2.3, 4)),
          RandomLatencyInterceptorNode(LogNormalDistributionNode(6, 7))
        ),
        List.empty
      )

      val simulation = Simulation(
        SimulationMetadata("simulation-name", 10),
        Network(
          ExecutionContext(0, 1, 10),
          List.empty,
          DeliveryQueue(
            List(
              MessageDropInterceptor(BooleanDistribution(0.63)),
              RandomLatencyInterceptor(UniformDistribution(6.0, 12.1)),
              RandomLatencyInterceptor(NormalDistribution(2.3, 4.0)),
              RandomLatencyInterceptor(LogNormalDistribution(6.0, 7.0))
            ),
            List.empty
          )
        )
      )

      assert(Transformer.transform(tree) === Success(simulation))
    }
  }
}
