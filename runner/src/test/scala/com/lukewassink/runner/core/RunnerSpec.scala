package com.lukewassink.runner.core

import com.lukewassink.runner.core.Runner
import com.lukewassink.simulation.behavior.SimpleSender
import com.lukewassink.simulation.core.MessageStage.{Pending, Scheduled}
import com.lukewassink.simulation.core.ResponseState.Request
import com.lukewassink.simulation.core.{
  Message,
  MessageContent,
  Node,
  NodeHeader
}
import com.lukewassink.simulation.test_utils.MessageSpecUtil.{
  draftedMessage,
  scheduledMessage
}
import com.lukewassink.simulation.test_utils.NetworkSpecUtil.testNetwork
import com.lukewassink.simulation.test_utils.NodeStateSpecUtil.testNodeState
import com.lukewassink.simulation.test_utils.{MessageMatchers, UnitSpec}

class RunnerSpec extends UnitSpec {
  describe("run") {
    val messageAToB = scheduledMessage(1, 1, 2, 3, 10, "AToB")
    val messageAToC = scheduledMessage(4, 1, 3, 5, 8, "AToC")
    val messageBToA = scheduledMessage(9, 2, 1, 4, 11, "BToA")

    val messageCToB = draftedMessage(2, "CToB")

    val nodeA = Node(
      List.empty,
      testNodeState(NodeHeader(1, 2), List.empty, List.empty)
    )
    val nodeB = Node(
      List.empty,
      testNodeState(NodeHeader(2, 5), List.empty, List.empty)
    )
    val nodeC = Node(
      List(SimpleSender(3, messageCToB)),
      testNodeState(NodeHeader(3, 10), List.empty, List.empty)
    )

    val network = testNetwork(
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
        no(states(2).messagesInTransit.messages) should have(
          stringContent("CToB")
        )

        exactly(1, states(3).messagesInTransit.messages) should have(
          stringContent("CToB")
        )
      }

      it("has the message in the queue before delivery") {
        exactly(1, states(12).messagesInTransit.messages) should have(
          stringContent("CToB")
        )
      }

      it("does not have the message in the queue on delivery") {
        no(states(13).messagesInTransit.messages) should have(
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
