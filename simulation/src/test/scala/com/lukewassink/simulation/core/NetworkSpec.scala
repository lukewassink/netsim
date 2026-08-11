package com.lukewassink.simulation.core

import com.lukewassink.simulation.core.{
  DeliveryQueue, Message, MessageContent, Network, Node, NodeHeader
}
import com.lukewassink.simulation.test_utils.BehaviorSpecUtil.{
  TestMessageBehavior, TestSelfUpdateBehavior
}
import com.lukewassink.simulation.test_utils.NetworkSpecUtil.testNetwork
import com.lukewassink.simulation.test_utils.NodeStateSpecUtil.testNodeState
import com.lukewassink.simulation.test_utils.RandomSpecUtil.InertRandom
import com.lukewassink.simulation.test_utils.{BehaviorSpecUtil, UnitSpec}
import com.lukewassink.simulation.util.Time
import com.lukewassink.simulation.test_utils.MessageSpecUtil.{
  draftedMessage, pendingMessage
}

class NetworkSpec extends UnitSpec {
  describe("Network") {
    val emptyNetwork = testNetwork(1, List.empty, List.empty)

    val emptyContent         = MessageContent("")
    val pendingMessageAToB   = pendingMessage(1, 1, 2, 3, "")
    val pendingMessageAToC   = pendingMessage(4, 1, 3, 5, "")
    val pendingMessageBToA   = pendingMessage(9, 2, 1, 4, "")
    val scheduledMessageAToB = pendingMessageAToB.schedule(10)
    val scheduledMessageAToC = pendingMessageAToC.schedule(10)
    val scheduledMessageBToA = pendingMessageBToA.schedule(10)

    val nodeA = Node(
      List.empty,
      testNodeState(
        NodeHeader(1, 0),
        List(pendingMessageAToB, pendingMessageAToC),
        List.empty
      )
    )
    val nodeB = Node(
      List.empty,
      testNodeState(NodeHeader(2, 0), List(pendingMessageBToA), List.empty)
    )
    val nodeC = Node(
      List(BehaviorSpecUtil.TestSelfUpdateBehavior(0)),
      testNodeState(NodeHeader(3, 0), List.empty, List.empty)
    )
    val network = testNetwork(
      1,
      List(nodeA, nodeB, nodeC),
      List(scheduledMessageAToB, scheduledMessageAToC, scheduledMessageBToA)
    )
    val nextNetwork = network.next

    describe("NextState") {
      it("ticks the time forward") {
        assert(nextNetwork.time.time - network.time.time === 1)
      }

      it("delivers current messages") {
        val readyToSend = testNetwork(
          9,
          List(nodeA, nodeB, nodeC),
          List(scheduledMessageAToB, scheduledMessageAToC, scheduledMessageBToA)
        )
        val withSentMessages = readyToSend.next

        readyToSend.nodes(1).sharedState.incomingMessages shouldBe empty
        readyToSend.nodes(2).sharedState.incomingMessages shouldBe empty
        readyToSend.nodes(3).sharedState.incomingMessages shouldBe empty
        withSentMessages.nodes(1).sharedState.incomingMessages should
          contain theSameElementsAs List(scheduledMessageBToA)
        withSentMessages.nodes(2).sharedState.incomingMessages should
          contain theSameElementsAs List(scheduledMessageAToB)
        withSentMessages.nodes(3).sharedState.incomingMessages should
          contain theSameElementsAs List(scheduledMessageAToC)
      }

      it("triggers node behavior") {
        network.nodes(3).behaviors.head match {
          case TestSelfUpdateBehavior(selfState) => selfState.should(equal(0))
        }
        nextNetwork.nodes(3).behaviors.head match {
          case TestSelfUpdateBehavior(selfState) => selfState.should(equal(1))
        }
      }

      it("collects new messages and sets the delivery time") {
        val message = draftedMessage(1, "")
        val node    = Node(
          List(TestMessageBehavior(message)),
          testNodeState(NodeHeader(1, 0), List.empty, List.empty)
        )
        val network = testNetwork(0, List(node), List.empty)

        network.messagesInTransit.messages shouldBe empty
        val nextNetwork = network.next
        nextNetwork.messagesInTransit.messages should have size 1
        nextNetwork.messagesInTransit.messages.head.messageStage
          .deliveryTime should equal(Time(11))
      }
    }

    describe("List constructor") {
      it("handles an empty list") {
        Network(0, List.empty, List.empty, InertRandom()) should
          equal(Network(0, Map.empty, DeliveryQueue.empty, InertRandom(), 1))
      }

      it("handles a list of nodes") {
        Network(0, List(nodeA, nodeB, nodeC), List.empty, InertRandom())
          .nodes should contain theSameElementsAs
          Map(NodeID(1) -> nodeA, NodeID(2) -> nodeB, NodeID(3) -> nodeC)
      }

      it("handles a list of messages") {
        Network(
          0,
          List.empty,
          List(scheduledMessageAToB, scheduledMessageAToC, scheduledMessageBToA),
          InertRandom()
        ).messagesInTransit.messages should contain theSameElementsAs
          List(scheduledMessageAToB, scheduledMessageAToC, scheduledMessageBToA)
      }
    }
  }
}
