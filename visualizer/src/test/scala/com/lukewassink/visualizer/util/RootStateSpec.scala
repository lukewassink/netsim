package com.lukewassink.visualizer.util

import com.lukewassink.simulation.behavior.SimpleSender
import com.lukewassink.simulation.message.MessageStage.{Drafted, Scheduled}
import com.lukewassink.simulation.core.{
  DeliveryQueue, ExecutionContext, Network, Node, NodeHeader, NodeState
}
import com.lukewassink.simulation.interceptor.MessageDropInterceptor
import com.lukewassink.simulation.message.RecipientSpecification.Single
import com.lukewassink.simulation.message.{DeliverySemantics, Message, Content}
import com.lukewassink.simulation.util.BooleanDistribution
import com.lukewassink.visualizer.processing.SyntheticHistoryElement.DroppedMessageElement
import com.lukewassink.visualizer.test_util.UnitSpec
import com.raquo.laminar.api.L.{*, given}

class RootStateSpec extends UnitSpec {
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
              Message(Drafted(Single(0)), DeliverySemantics.empty, Content("Hi!"))
            )),
            NodeState(NodeHeader(1, 0), List.empty, List.empty)
          )
        ),
        List.empty
      )

      val expectedState: NetworkState = (network, List.empty)

      assert(state.currentNetworkState.observe.now() === expectedState)
    }

    it("includes synthetic history") {
      val config =
        """
          name = "simulation-name"
          randomSeed = 10

          interceptors = [{
              type = message-drop
              distribution {
                type = boolean
                probability = 1
              }
            }]

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
      for _ <- 1 to 5 do state.playbackState.increment()

      val network = Network(
        ExecutionContext(5, 1, 10),
        List(
          Node(List.empty, NodeState(NodeHeader(0, 0), List.empty, List.empty)),
          Node(
            List(SimpleSender(
              2,
              Message(Drafted(Single(0)), DeliverySemantics.empty, Content("Hi!"))
            )),
            NodeState(NodeHeader(1, 1), List.empty, List.empty)
          )
        ),
        DeliveryQueue(
          List(MessageDropInterceptor(BooleanDistribution(1))),
          List.empty
        )
      )

      val expectedState: NetworkState = (
        network,
        List(DroppedMessageElement(Message(
          Scheduled(0, 1, 0, 2, 12),
          DeliverySemantics.empty,
          Content("Hi!")
        )))
      )

      assert(expectedState === state.currentNetworkState.observe.now())
    }
  }
}
