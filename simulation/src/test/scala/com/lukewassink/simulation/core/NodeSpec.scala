package com.lukewassink.simulation.core

import com.lukewassink.simulation.core.{Node, NodeHeader, NodeState}
import com.lukewassink.simulation.message.Message
import com.lukewassink.simulation.test_utils.UnitSpec
import com.lukewassink.simulation.test_utils.BehaviorSpecUtil.{
  TestMessageBehavior, TestSelfUpdateBehavior
}
import com.lukewassink.simulation.test_utils.MessageSpecUtil.{
  draftedMessage, pendingMessage
}
import com.lukewassink.simulation.test_utils.NodeStateSpecUtil.testNodeState
import com.lukewassink.simulation.test_utils.ExecutionContextUtils.testContext

class NodeSpec extends UnitSpec {
  private val draftedMessage1   = draftedMessage(7, "One")
  private val draftedMessage2   = draftedMessage(8, "Two")
  private val sentMessage1      = pendingMessage(1, 3, 7, 4, "One")
  private val sentMessage2      = pendingMessage(2, 3, 8, 5, "Two")
  private val scheduledMessage1 = sentMessage1.schedule(5)
  private val scheduledMessage2 = sentMessage2.schedule(9)

  val emptyState: NodeState = testNodeState(
    NodeHeader(0, 0),
    List.empty,
    List.empty
  )

  val emptyNode                = Node(List.empty, emptyState)
  val nodeWithOutgoingMessages = Node(
    List.empty,
    testNodeState(NodeHeader(4, 0), List(sentMessage1, sentMessage2), List.empty)
  )

  describe("outgoingMessages") {
    it("returns empty when there are no outgoing messages") {
      emptyNode.outgoingMessages shouldBe empty
    }

    it("returns outgoing messages") {
      nodeWithOutgoingMessages.outgoingMessages should contain theSameElementsAs
        List(sentMessage1, sentMessage2)
    }
  }

  describe("withIncomingMessage") {
    it("adds an incoming message to the state") {
      emptyNode.sharedState.incomingMessages shouldBe empty
      emptyNode.withIncomingMessage(scheduledMessage1).sharedState
        .incomingMessages should contain theSameElementsAs
        List(scheduledMessage1)
    }
  }

  describe("preDeliveryAction") {
    it("clears sent messages from the last tick") {
      val node = Node(
        List.empty,
        testNodeState(
          NodeHeader(4, 0),
          List.empty,
          List(scheduledMessage1, scheduledMessage2)
        )
      )
      node.sharedState.incomingMessages should contain theSameElementsAs
        List(scheduledMessage1, scheduledMessage2)
      node.preDeliveryAction(using testContext(5)).sharedState
        .incomingMessages shouldBe empty
    }
  }

  describe("preAction") {
    it("triggers a behavior to update the shared state") {
      val node = Node(List(TestMessageBehavior(draftedMessage1)), emptyState)
      node.outgoingMessages shouldBe empty
      val nextMessages = node.preAction(using testContext(5)).outgoingMessages
      nextMessages should have size 1
      all(nextMessages) should have(stringContent("One"))
    }

    it("triggers a behavior to update the behavior's state") {
      val node = Node(List(TestSelfUpdateBehavior(0)), emptyState)
      node.behaviors.head match {
        case TestSelfUpdateBehavior(selfState) => selfState.should(equal(0))
      }
      node.preAction(using testContext(10)).behaviors.head match {
        case TestSelfUpdateBehavior(selfState) => selfState.should(equal(1))
      }
    }
  }

  describe("mainAction") {
    it("triggers a behavior to update the shared state") {
      val node = Node(List(TestMessageBehavior(draftedMessage1)), emptyState)
      node.outgoingMessages shouldBe empty
      val nextMessages = node.mainAction(using testContext(5)).outgoingMessages
      nextMessages should have size 1
      all(nextMessages) should have(stringContent("One"))
    }

    it("triggers a behavior to update the behavior's state") {
      val node = Node(List(TestSelfUpdateBehavior(0)), emptyState)
      node.behaviors.head match {
        case TestSelfUpdateBehavior(selfState) => selfState.should(equal(0))
      }
      node.mainAction(using testContext(10)).behaviors.head match {
        case TestSelfUpdateBehavior(selfState) => selfState.should(equal(1))
      }
    }
  }

  describe("postAction") {
    it("triggers a behavior to update the shared state") {
      val node = Node(List(TestMessageBehavior(draftedMessage1)), emptyState)
      node.outgoingMessages shouldBe empty
      val nextMessages = node.postAction(using testContext(5)).outgoingMessages
      nextMessages should have size 1
      all(nextMessages) should have(stringContent("One"))
    }

    it("triggers a behavior to update the behavior's state") {
      val node = Node(List(TestSelfUpdateBehavior(0)), emptyState)
      node.behaviors.head match {
        case TestSelfUpdateBehavior(selfState) => selfState.should(equal(0))
      }
      node.postAction(using testContext(10)).behaviors.head match {
        case TestSelfUpdateBehavior(selfState) => selfState.should(equal(1))
      }
    }
  }

  describe("sending multiple messages") {
    val node = Node(
      List(
        TestMessageBehavior(draftedMessage1),
        TestMessageBehavior(draftedMessage2)
      ),
      emptyState
    )
    node.outgoingMessages shouldBe empty
    val outgoingMessages =
      node.mainAction(using testContext(10)).outgoingMessages

    it("triggers multiple behaviors") {
      outgoingMessages should have size 2
      exactly(1, outgoingMessages) should have(stringContent("One"))
      exactly(1, outgoingMessages) should have(stringContent("Two"))
    }

    it("increments the message ID") {
      exactly(1, outgoingMessages) should have(messageID(0))
      exactly(1, outgoingMessages) should have(messageID(1))
    }
  }

  describe("id") {
    it("returns the node ID") {
      val node1 = Node(
        List.empty,
        testNodeState(NodeHeader(1, 0), List.empty, List.empty)
      )
      val node2 = Node(
        List.empty,
        testNodeState(NodeHeader(2, 0), List.empty, List.empty)
      )

      node1.id shouldEqual NodeID(1)
      node2.id shouldEqual NodeID(2)
    }
  }
}
