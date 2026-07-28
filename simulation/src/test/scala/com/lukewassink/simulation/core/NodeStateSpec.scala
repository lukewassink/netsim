package com.lukewassink.simulation.core

import com.lukewassink.simulation.test_utils.NodeSpecUtil.testNodeState
import com.lukewassink.simulation.test_utils.UnitSpec

class NodeStateSpec extends UnitSpec {
  val message1 =
    Message(MessageHeader(0, 0, 7, 0, Some(9)), MessageContent("One"))
  val message2 =
    Message(MessageHeader(0, 0, 8, 0, Some(5)), MessageContent("Two"))
  val emptyState: NodeState =
    testNodeState(NodeHeader(3, 1), List.empty, List.empty)

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
