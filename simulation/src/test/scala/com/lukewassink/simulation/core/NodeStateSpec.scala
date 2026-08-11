package com.lukewassink.simulation.core

import com.lukewassink.simulation.test_utils.MessageSpecUtil.{
  draftedMessage, pendingMessage
}
import com.lukewassink.simulation.test_utils.NodeStateSpecUtil.testNodeState
import com.lukewassink.simulation.test_utils.UnitSpec
import com.lukewassink.simulation.test_utils.ExecutionContextUtils.testContext

class NodeStateSpec extends UnitSpec {
  private val draftedMessage1   = draftedMessage(7, "One")
  private val draftedMessage2   = draftedMessage(8, "Two")
  private val sentMessage1      = pendingMessage(1, 3, 7, 4, "One")
  private val sentMessage2      = pendingMessage(2, 3, 8, 5, "Two")
  private val scheduledMessage1 = sentMessage1.schedule(5)
  private val scheduledMessage2 = sentMessage2.schedule(9)

  private val emptyState = testNodeState(
    NodeHeader(3, 1),
    List.empty,
    List.empty
  )

  describe("withOutgoingMessage") {
    it("adds the message to the list and sets node metadata correctly") {
      assert(emptyState.outgoingMessages.isEmpty)
      val state2 =
        emptyState.withOutgoingMessage(using testContext(4))(draftedMessage1)
      assert(state2.outgoingMessages === List(sentMessage1))
      val state3 =
        state2.withOutgoingMessage(using testContext(5))(draftedMessage2)
      state3.outgoingMessages should contain theSameElementsAs
        List(sentMessage1, sentMessage2)
    }
  }

  describe("clearOutgoingMessages") {
    it("does nothing if messages are already empty") {
      assert(emptyState.outgoingMessages.isEmpty)
      assert(emptyState.clearOutgoingMessages.outgoingMessages.isEmpty)
    }

    it("clears the outgoing messages") {
      val state = testNodeState(
        NodeHeader(0, 0),
        List(sentMessage1, sentMessage2),
        List.empty
      )
      state.outgoingMessages should contain theSameElementsAs
        List(sentMessage1, sentMessage2)
      state.clearOutgoingMessages.outgoingMessages shouldBe empty
    }
  }

  describe("withIncomingMessage") {
    it("adds the message to the inbox") {
      emptyState.incomingMessages shouldBe empty
      val state1 = emptyState.withIncomingMessage(scheduledMessage1)
      state1.incomingMessages should contain theSameElementsAs
        List(scheduledMessage1)

      val state2 = state1.withIncomingMessage(scheduledMessage2)
      state2.incomingMessages should contain theSameElementsAs
        List(scheduledMessage1, scheduledMessage2)
    }
  }

  describe("clearIncomingMessages") {
    it("does nothing if incoming messages are already empty") {
      assert(emptyState.incomingMessages.isEmpty)
      assert(emptyState.clearIncomingMessages.incomingMessages.isEmpty)
    }

    it("clears the incoming messages") {
      val state = testNodeState(
        NodeHeader(0, 0),
        List.empty,
        List(scheduledMessage1, scheduledMessage2)
      )
      state.incomingMessages should contain theSameElementsAs
        List(scheduledMessage1, scheduledMessage2)
      state.clearIncomingMessages.incomingMessages shouldBe empty
    }
  }
}
