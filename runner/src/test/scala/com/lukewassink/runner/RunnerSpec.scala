package com.lukewassink.runner

import com.lukewassink.runner.Runner
import com.lukewassink.simulation.behavior.SimpleSender
import com.lukewassink.simulation.core.{Node, NodeHeader}
import com.lukewassink.simulation.test_utils.NetworkSpecUtil.testNetwork
import com.lukewassink.simulation.test_utils.NodeStateSpecUtil.testNodeState
import com.lukewassink.simulation.test_utils.UnitSpec
import com.lukewassink.simulation.test_utils.MessageSpecUtil.{
  draftedMessage,
  scheduledMessage
}

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

    val states = Runner.run(network).take(20).toVector

    it("starts in the initial state") {
      states(0) should equal(network)
    }

    it("sends queued messages at their delivery times") {
      states(9)
        .nodes(3)
        .sharedState
        .incomingMessages should contain theSameElementsAs List(messageAToC)

      states(11)
        .nodes(2)
        .sharedState
        .incomingMessages should contain theSameElementsAs List(messageAToB)

      states(12)
        .nodes(1)
        .sharedState
        .incomingMessages should contain theSameElementsAs List(messageBToA)
    }

    describe("the detailed trajectory of a message sending behavior") {
      it("adds an outgoing message to the queue") {
        no(states(3).messagesInTransit.messages) should have(
          stringContent("CToB")
        )

        exactly(1, states(4).messagesInTransit.messages) should have(
          stringContent("CToB")
        )
      }

      it("has the message in the queue before delivery") {
        exactly(1, states(12).messagesInTransit.messages) should have(
          stringContent("CToB")
        )
      }

      it("does not have the message in the queue on delivery") {
        no(states(14).messagesInTransit.messages) should have(
          stringContent("CToB")
        )
      }

      it("delivers the added message") {
        no(states(13).nodes(2).sharedState.incomingMessages) should have(
          stringContent("CToB")
        )

        exactly(
          1,
          states(14).nodes(2).sharedState.incomingMessages
        ) should have(
          stringContent("CToB")
        )

        no(states(15).nodes(2).sharedState.incomingMessages) should have(
          stringContent("CToB")
        )
      }
    }
  }
}
