package core

import test_utils.{BehaviorSpecUtil, UnitSpec}

class NodeSpec extends UnitSpec {
  val message1 = Message(MessageHeader(0, 0, 7, 0, 9), MessageContent("One"))
  val message2 = Message(MessageHeader(0, 0, 8, 0, 5), MessageContent("Two"))
  val emptyState = NodeState(NodeHeader(3, 1), List.empty)

  describe("NodeState") {
    describe("withOutgoingMessage") {
      it("adds the message to the list and sets node metadata correctly") {
        val messageWithMetadata1 =
          Message(MessageHeader(1, 3, 7, 4, 9), MessageContent("One"))
        val messageWithMetadata2 =
          Message(MessageHeader(2, 3, 8, 5, 5), MessageContent("Two"))

        assert(emptyState.outgoingMessages.isEmpty)
        val state2 = emptyState.withOutgoingMessage(4, message1)
        assert(state2.outgoingMessages === List(messageWithMetadata1))
        val state3 = state2.withOutgoingMessage(5, message2)
        state3.outgoingMessages should contain theSameElementsAs List(
          messageWithMetadata1,
          messageWithMetadata2
        )
      }
    }

    describe("clearOutgoingMessages") {
      it("does nothing if messages are already empty") {
        assert(emptyState.outgoingMessages.isEmpty)
        assert(emptyState.clearOutgoingMessages.outgoingMessages.isEmpty)
      }

      it("clears only the outgoing messages") {
        val state = NodeState(NodeHeader(0, 0), List(message1, message2))
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
    val emptyState = NodeState(NodeHeader(0, 0), List.empty)

    val emptyNode = Node(List.empty, emptyState, emptyQueue)
    val nodeWithIncomingMessages = Node(
      List.empty,
      NodeState(NodeHeader(4, 0), List(message1, message2)),
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
        val nextMessages = node.nextNode(10).outgoingMessages
        nextMessages should have size 1
        all(nextMessages) should matchPattern {
          case Message(_, MessageContent("One")) =>
        }
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
        val outgoingMessages = node
          .nextNode(10)
          .outgoingMessages
        outgoingMessages should have size 2
        exactly(1, outgoingMessages) should matchPattern {
          case Message(_, MessageContent("One")) =>
        }
        exactly(1, outgoingMessages) should matchPattern {
          case Message(_, MessageContent("Two")) =>
        }
      }

      it("clears delivered messages") {
        val node = Node(
          List.empty,
          NodeState(NodeHeader(4, 0), List.empty),
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
