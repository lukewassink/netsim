package core

import test_utils.BehaviorSpecUtil.{TestMessageBehavior, TestSelfUpdateBehavior}
import test_utils.UnitSpec
import test_utils.NodeSpecUtil.testNodeState

class NodeSpec extends UnitSpec {
  val message1 =
    Message(MessageHeader(0, 0, 7, 0, Some(9)), MessageContent("One"))
  val message2 =
    Message(MessageHeader(0, 0, 8, 0, Some(5)), MessageContent("Two"))
  val emptyState: NodeState =
    testNodeState(NodeHeader(3, 1), List.empty, List.empty)

  describe("NodeState") {
    describe("withOutgoingMessage") {
      it("adds the message to the list and sets node metadata correctly") {
        val messageWithMetadata1 =
          Message(MessageHeader(1, 3, 7, 4, Some(9)), MessageContent("One"))
        val messageWithMetadata2 =
          Message(MessageHeader(2, 3, 8, 5, Some(5)), MessageContent("Two"))

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

      it("clears the outgoing messages") {
        val state =
          testNodeState(NodeHeader(0, 0), List(message1, message2), List.empty)
        state.outgoingMessages should contain theSameElementsAs List(
          message1,
          message2
        )
        state.clearOutgoingMessages.outgoingMessages shouldBe empty
      }
    }

    describe("withIncomingMessage") {
      it("adds the message to the inbox") {
        emptyState.incomingMessages shouldBe empty
        val state1 = emptyState.withIncomingMessage(message1)
        state1.incomingMessages should contain theSameElementsAs List(message1)
        val state2 = state1.withIncomingMessage(message2)
        state2.incomingMessages should contain theSameElementsAs List(
          message1,
          message2
        )
      }
    }

    describe("clearIncomingMessages") {
      it("does nothing if incoming messages are already empty") {
        assert(emptyState.incomingMessages.isEmpty)
        assert(emptyState.clearIncomingMessages.incomingMessages.isEmpty)
      }

      it("clears the incoming messages") {
        val state =
          testNodeState(NodeHeader(0, 0), List.empty, List(message1, message2))
        state.incomingMessages should contain theSameElementsAs List(
          message1,
          message2
        )
        state.clearIncomingMessages.incomingMessages shouldBe empty
      }
    }
  }

  describe("Node") {
    val emptyQueue = MessageQueue.empty
    val emptyState = testNodeState(NodeHeader(0, 0), List.empty, List.empty)

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
            case Message(MessageHeader(0, _, _, _, _), _) =>
          }
          exactly(1, outgoingMessages) should matchPattern {
            case Message(MessageHeader(1, _, _, _, _), _) =>
          }
        }
      }
    }
  }
}
