package core

import test_utils.{BehaviorSpecUtil, UnitSpec}

class NodeSpec extends UnitSpec {
  val emptyContent = MessageContent("")
  val message1 = Message(MessageHeader(0, 1, 2, 1, 9), emptyContent)
  val message2 = Message(MessageHeader(12, 1, 4, 1, 4), emptyContent)
  val emptyState = NodeState(List.empty)

  describe("NodeState") {
    describe("withOutgoingMessage") {
      it("adds the message to the list") {
        assert(emptyState.outgoingMessages.isEmpty)
        val state2 = emptyState.withOutgoingMessage(message1)
        assert(state2.outgoingMessages === List(message1))
        val state3 = state2.withOutgoingMessage(message2)
        state3.outgoingMessages should contain theSameElementsAs List(
          message1,
          message2
        )
      }
    }

    describe("clearOutgoingMessages") {
      it("does nothing if messages are already empty") {
        assert(emptyState.outgoingMessages.isEmpty)
        assert(emptyState.clearOutgoingMessages.outgoingMessages.isEmpty)
      }

      it("clears only the outgoing messages") {
        val state = NodeState(List(message1, message2))
        state.outgoingMessages should contain theSameElementsAs List(
          message1,
          message2
        )
        state.clearOutgoingMessages.outgoingMessages shouldBe empty
      }
    }
  }

  describe("Node") {
    val emptyQueue = MessageQueue.empty
    val emptyState = NodeState(List.empty)

    val emptyNode = Node(List.empty, emptyState, emptyQueue)
    val nodeWithIncomingMessages = Node(
      List.empty,
      NodeState(List(message1, message2)),
      emptyQueue
    )

    describe("outgoingMessages") {
      it("returns empty when there are no outgoing messages") {
        emptyNode.outgoingMessages shouldBe empty
      }

      it("returns outgoing messages") {
        nodeWithIncomingMessages.outgoingMessages should contain theSameElementsAs List(
          message1,
          message2
        )
      }
    }

    describe("withIncomingMessage") {
      it("adds an incoming message to the queue") {
        emptyNode.incomingMessages.currentMessages(10) shouldBe empty
        emptyNode
          .withIncomingMessage(message1)
          .incomingMessages
          .currentMessages(10) should contain theSameElementsAs List(message1)
      }
    }

    describe("nextNode") {
      it("clears outgoing messages") {
        nodeWithIncomingMessages.nextNode(1).outgoingMessages shouldBe empty
      }

      it("triggers a behavior to update the state") {
        val node = Node(
          List(BehaviorSpecUtil.TestBehavior(message1)),
          emptyState,
          emptyQueue
        )
        node.outgoingMessages shouldBe empty
        node
          .nextNode(10)
          .outgoingMessages should contain theSameElementsAs List(message1)
      }

      it("triggers multiple behaviors") {
        val node = Node(
          List(
            BehaviorSpecUtil.TestBehavior(message1),
            BehaviorSpecUtil.TestBehavior(message2)
          ),
          emptyState,
          emptyQueue
        )
        node.outgoingMessages shouldBe empty
        node
          .nextNode(10)
          .outgoingMessages should contain theSameElementsAs List(
          message1,
          message2
        )
      }

      it("clears delivered messages") {
        val node = Node(
          List.empty,
          NodeState(List.empty),
          MessageQueue(message1, message2)
        )
        node.incomingMessages.currentMessages(
          10
        ) should contain theSameElementsAs List(message1, message2)
        node
          .nextNode(5)
          .incomingMessages
          .currentMessages(10) should contain theSameElementsAs List(message1)
        node.nextNode(10).incomingMessages.currentMessages(10) shouldBe empty
      }
    }
  }
}
