package com.lukewassink.simulation.behavior

import com.lukewassink.simulation.core.{ExecutionContext, NodeHeader, NodeState}
import com.lukewassink.simulation.message.{
  BroadcastProtocols, Content, DeliverySemantics, Message, MessageUniqueID
}
import com.lukewassink.simulation.message.MessageStage.{
  Drafted, Pending, Scheduled
}
import com.lukewassink.simulation.message.{Reliable, ReliableAcks}
import com.lukewassink.simulation.message.RecipientSpecification.Single
import com.lukewassink.simulation.message.ResponseState.{Request, Response}
import com.lukewassink.simulation.test_utils.UnitSpec

class ReliableBroadcasterSpec extends UnitSpec {
  private val config      = ReliableBroadcasterConfig(100, 3, 1000, 0)
  private val broadcaster = ReliableBroadcaster.empty(config)
  private val state       = NodeState(NodeHeader(0, 0), List.empty, List.empty)

  given ExecutionContext = ExecutionContext(16, 1, 1)

  describe("message dedupes") {
    val incomingMessage = Message[Scheduled](
      Scheduled(1, 2, 0, 5, 15),
      DeliverySemantics.empty,
      Content.empty
    )

    it("filters duplicate messages") {
      val UpdatedState(withSeenMessages, nextState) = broadcaster
        .preAction(state.withIncomingMessage(incomingMessage))
      nextState.incomingMessages should contain theSameElementsAs
        List(incomingMessage)

      val UpdatedState(_, dedupedState) = withSeenMessages
        .preAction(state.withIncomingMessage(incomingMessage))
      dedupedState.incomingMessages shouldBe empty
    }

    it("forgets seen messages after the timeout") {
      val UpdatedState(withSeenMessages, _) = broadcaster
        .preAction(state.withIncomingMessage(incomingMessage))

      val UpdatedState(behaviorAfterTimeout, _) =
        withSeenMessages.preAction(using ExecutionContext(2000, 1, 1))(state)

      val UpdatedState(_, stateAfterTimeout) =
        behaviorAfterTimeout.preAction(using
          // 2000 is after the timeout, so it shouldn't filter the duplicate message.
          ExecutionContext(2000, 1, 1)
        )(state.withIncomingMessage(incomingMessage))
      stateAfterTimeout.incomingMessages should contain theSameElementsAs
        List(incomingMessage)
    }
  }

  describe("message retries") {
    val outgoingMessage1 = Message[Drafted](
      Drafted(Single(2)),
      DeliverySemantics(Request(), BroadcastProtocols(Reliable())),
      Content.empty
    )
    val outgoingMessage2 = Message[Drafted](
      Drafted(Single(2)),
      DeliverySemantics(Request(), BroadcastProtocols(Reliable())),
      Content.empty
    )
    val message1Ack = Message[Scheduled](
      Scheduled(3, 2, 0, 20, 35),
      DeliverySemantics(
        Response(0, 0),
        BroadcastProtocols(ReliableAcks(List(MessageUniqueID(0, 0))))
      ),
      Content.empty
    )

    it("it retries un-acknowledged messages") {
      val UpdatedState(withReliables, _) = broadcaster.postAction(
        state.withOutgoingMessage(outgoingMessage1)
          .withOutgoingMessage(outgoingMessage2)
      )

      // Acknowledge message 1 but not message 2.
      val UpdatedState(withAcks, _) =
        withReliables.preAction(using
          ExecutionContext(35, 1, 1)
        )(state.withIncomingMessage(message1Ack))

      val UpdatedState(_, withRetries) =
        withAcks.mainAction(using ExecutionContext(200, 1, 1))(state)
      exactly(1, withRetries.outgoingMessages) should have(messageUniqueID(0, 0))
    }

    it("it gives up after reaching the max retries") {
      val UpdatedState(withReliables, _) = broadcaster
        .postAction(state.withOutgoingMessage(outgoingMessage1))

      val UpdatedState(afterRetries1, withRetries1) =
        withReliables.mainAction(using ExecutionContext(200, 1, 1))(state)
      exactly(1, withRetries1.outgoingMessages) should have(messageUniqueID(0, 0))

      val UpdatedState(afterRetries2, withRetries2) =
        afterRetries1.mainAction(using ExecutionContext(400, 1, 1))(state)
      exactly(1, withRetries2.outgoingMessages) should have(messageUniqueID(0, 0))

      val UpdatedState(afterRetries3, withRetries3) =
        afterRetries2.mainAction(using ExecutionContext(600, 1, 1))(state)
      exactly(1, withRetries3.outgoingMessages) should have(messageUniqueID(0, 0))

      val UpdatedState(_, withMaxRetries) =
        afterRetries3.mainAction(using ExecutionContext(800, 1, 1))(state)
      withMaxRetries.outgoingMessages shouldBe empty
    }

    it("acknowledges received reliable messages") {
      val incomingMessage = Message[Scheduled](
        Scheduled(1, 2, 0, 5, 15),
        DeliverySemantics(Request(), BroadcastProtocols(Reliable())),
        Content.empty
      )
      val ack = Message[Pending](
        Pending(0, 0, 2, 20),
        DeliverySemantics(
          Response(2, 1),
          BroadcastProtocols(ReliableAcks(List(MessageUniqueID(2, 1))))
        ),
        Content.empty
      )

      given ExecutionContext = ExecutionContext(20, 1, 1)

      val UpdatedState(_, withAck) = broadcaster
        .preAction(state.withIncomingMessage(incomingMessage)).selfState
        .mainAction(state)

      withAck.outgoingMessages should contain theSameElementsAs List(ack)
    }
  }
}
