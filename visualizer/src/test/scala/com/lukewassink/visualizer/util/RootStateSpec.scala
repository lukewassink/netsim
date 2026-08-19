package com.lukewassink.visualizer.util

import com.lukewassink.runner.core.Runner
import com.lukewassink.runner.util.Success
import com.lukewassink.simulation.behavior.SimpleSender
import com.lukewassink.simulation.core.MessageStage.Drafted
import com.lukewassink.simulation.core.ResponseState.Request
import com.lukewassink.simulation.core.{
  Message, MessageContent, Network, Node, NodeHeader, NodeState
}
import com.lukewassink.visualizer.test_util.UnitSpec
import com.raquo.laminar.api.L.{*, given}
import com.raquo.airstream.ownership.{Owner, Subscription}

class TestableOwner extends Owner {

  def _testSubscriptions: List[Subscription] = subscriptions.asScalaJs.toList

  override def killSubscriptions(): Unit = super.killSubscriptions()
}

class RootStateSpec extends UnitSpec {
  given TestableOwner()

  describe("currentNetworkState") {
    it("contains the current state of the network") {
      val config =
        """
        name = "simulation-name"
        randomSeed = 10
        
        nodes = [{
           name = "node-1"
           behaviors = []
         }
         {
           name = "node-2"
           behaviors = [{
               type = "simple-sender"
               time = 2
               receiver = "node-1"
               content = "Hi!"
             }
           ]
        }]
        """.stripMargin

      val state = RootState(config)

      val network = Network(
        0,
        List(
          Node(List.empty, NodeState(NodeHeader(0, 0), List.empty, List.empty)),
          Node(
            List(SimpleSender(
              2,
              Message[Drafted](Drafted(0), Request(), MessageContent("Hi!"))
            )),
            NodeState(NodeHeader(1, 0), List.empty, List.empty)
          )
        ),
        List.empty
      )

      val expectedState: NetworkState = (network, List.empty)

      assert(state.currentNetworkState.observe.now() === expectedState)
    }

    it("includes synthetic history") {}
  }
}
