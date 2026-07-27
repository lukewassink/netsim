package com.lukewassink.runner

import com.lukewassink.runner.Runner
import com.lukewassink.simulation.behavior.SimpleSender
import com.lukewassink.simulation.core.{Message, MessageContent, MessageHeader, Node, NodeHeader}
import com.lukewassink.simulation.test_utils.NetworkStateSpecUtil.testNetworkState
import com.lukewassink.simulation.test_utils.NodeSpecUtil.testNodeState
import com.lukewassink.simulation.test_utils.{MessageMatchers, UnitSpec}

class RunnerSpec extends UnitSpec {
  describe("run") {
    val messageAToB =
      Message(MessageHeader(1, 1, 2, 3, Some(10)), MessageContent("AToB"))
    val messageAToC =
      Message(MessageHeader(4, 1, 3, 5, Some(8)), MessageContent("AToC"))
    val messageBToA =
      Message(MessageHeader(9, 2, 1, 4, Some(11)), MessageContent("BToA"))
    val messageCToB =
      Message(MessageHeader(0, 0, 2, 0, None), MessageContent("CToB"))

    val nodeA = Node(
      List.empty,
      testNodeState(
        NodeHeader(1, 2),
        List.empty,
        List.empty
      )
    )
    val nodeB = Node(
      List.empty,
      testNodeState(NodeHeader(2, 5), List.empty, List.empty)
    )
    val nodeC = Node(
      List(SimpleSender(3, messageCToB)),
      testNodeState(NodeHeader(3, 10), List.empty, List.empty)
    )

    val network = testNetworkState(
      0,
      List(nodeA, nodeB, nodeC),
      List(messageAToB, messageAToC, messageBToA)
    )

    val states = Runner.run(network).take(15).toVector

    it("starts in the initial state") {
      states(0) should equal(network)
    }

    it("sends queued messages at their delivery times") {
      states(8)
        .nodes(3)
        .sharedState
        .incomingMessages should contain theSameElementsAs List(messageAToC)

      states(10)
        .nodes(2)
        .sharedState
        .incomingMessages should contain theSameElementsAs List(messageAToB)

      states(11)
        .nodes(1)
        .sharedState
        .incomingMessages should contain theSameElementsAs List(messageBToA)
    }

    describe("the detailed trajectory of a message sending behavior") {
      it("adds an outgoing message to the queue") {
        no(states(2).messagesInTransit.allMessages) should have(
          stringContent("CToB")
        )

        exactly(1, states(3).messagesInTransit.allMessages) should have(
          stringContent("CToB")
        )
      }

      it("has the message in the queue before delivery") {
        exactly(1, states(12).messagesInTransit.allMessages) should have(
          stringContent("CToB")
        )
      }

      it("does not have the message in the queue on delivery") {
        no(states(13).messagesInTransit.allMessages) should have(
          stringContent("CToB")
        )
      }

      it("delivers the added message") {
        no(states(12).nodes(2).sharedState.incomingMessages) should have(
          stringContent("CToB")
        )

        exactly(
          1,
          states(13).nodes(2).sharedState.incomingMessages
        ) should have(
          stringContent("CToB")
        )

        no(states(14).nodes(2).sharedState.incomingMessages) should have(
          stringContent("CToB")
        )
      }
    }
  }
}
