package com.lukewassink.simulation.core

import com.lukewassink.simulation.core.{
  Message,
  MessageContent,
  MessageHeader,
  MessageQueue,
  Node,
  NodeHeader,
  NodeState
}
import com.lukewassink.simulation.test_utils.UnitSpec
import com.lukewassink.simulation.test_utils.BehaviorSpecUtil.{
  TestMessageBehavior,
  TestSelfUpdateBehavior
}
import com.lukewassink.simulation.test_utils.NodeSpecUtil.testNodeState

class NodeSpec extends UnitSpec {
  val message1 =
    Message(MessageHeader(0, 0, 7, 0, Some(9)), MessageContent("One"))
  val message2 =
    Message(MessageHeader(0, 0, 8, 0, Some(5)), MessageContent("Two"))

  val emptyState: NodeState =
    testNodeState(NodeHeader(0, 0), List.empty, List.empty)

  val emptyNode = Node(List.empty, emptyState)
  val nodeWithOutgoingMessages = Node(
    List.empty,
    testNodeState(NodeHeader(4, 0), List(message1, message2), List.empty)
  )

  describe("outgoingMessages") {
    it("returns empty when there are no outgoing messages") {
      emptyNode.outgoingMessages shouldBe empty
    }

    it("returns outgoing messages") {
      nodeWithOutgoingMessages.outgoingMessages should contain theSameElementsAs List(
        message1,
        message2
      )
    }
  }

  describe("withIncomingMessage") {
    it("adds an incoming message to the state") {
      emptyNode.sharedState.incomingMessages shouldBe empty
      emptyNode
        .withIncomingMessage(message1)
        .sharedState
        .incomingMessages should contain theSameElementsAs List(message1)
    }
  }

  describe("preDeliveryAction") {
    it("clears sent messages from the last tick") {
      val node = Node(
        List.empty,
        testNodeState(NodeHeader(4, 0), List.empty, List(message1, message2))
      )
      node.sharedState.incomingMessages should contain theSameElementsAs List(
        message1,
        message2
      )
      node
        .preDeliveryAction(5)
        .sharedState
        .incomingMessages shouldBe empty
    }
  }

  describe("postDeliveryAction") {
    it("clears outgoing messages") {
      nodeWithOutgoingMessages
        .postDeliveryAction(1)
        .outgoingMessages shouldBe empty
    }

    it("triggers a behavior to update the shared state") {
      val node = Node(
        List(TestMessageBehavior(message1)),
        emptyState
      )
      node.outgoingMessages shouldBe empty
      val nextMessages = node.postDeliveryAction(10).outgoingMessages
      nextMessages should have size 1
      all(nextMessages) should matchPattern {
        case Message(_, MessageContent("One")) =>
      }
    }

    it("triggers a behavior to update the behavior's state") {
      val node = Node(
        List(TestSelfUpdateBehavior(0)),
        emptyState
      )
      node.behaviors.head match {
        case TestSelfUpdateBehavior(selfState) =>
          selfState.should(equal(0))
      }
      node.postDeliveryAction(10).behaviors.head match {
        case TestSelfUpdateBehavior(selfState) =>
          selfState.should(equal(1))
      }
    }

    describe("sending multiple messages") {
      val node = Node(
        List(
          TestMessageBehavior(message1),
          TestMessageBehavior(message2)
        ),
        emptyState
      )
      node.outgoingMessages shouldBe empty
      val outgoingMessages = node
        .postDeliveryAction(10)
        .outgoingMessages

      it("triggers multiple behaviors") {
        outgoingMessages should have size 2
        exactly(1, outgoingMessages) should matchPattern {
          case Message(_, MessageContent("One")) =>
        }
        exactly(1, outgoingMessages) should matchPattern {
          case Message(_, MessageContent("Two")) =>
        }
      }

      it("increments the message ID") {
        exactly(1, outgoingMessages) should matchPattern {
          case Message(MessageHeader(MessageID(0), _, _, _, _), _) =>
        }
        exactly(1, outgoingMessages) should matchPattern {
          case Message(MessageHeader(MessageID(1), _, _, _, _), _) =>
        }
      }
    }
  }
}
