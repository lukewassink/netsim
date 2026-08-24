package com.lukewassink.simulation.behavior

import com.lukewassink.simulation.core.{ExecutionContext, NodeState}
import com.lukewassink.simulation.message.{
  Content, DeliverySemantics, Message, MessageUniqueID, Protocol
}
import com.lukewassink.simulation.message.MessageStage.{
  Drafted, Pending, Scheduled
}
import com.lukewassink.simulation.message.Protocol.{Reliable, ReliableAck}
import com.lukewassink.simulation.message.RecipientSpecification.Single
import com.lukewassink.simulation.message.ResponseState.Response
import com.lukewassink.simulation.util.{Duration, Time}

case class ReliableBroadcasterConfig(
    // Wait this long before re-sending an un-acked message.
    ackTimeout: Duration,
    // How many times to resend a message.
    maxRetries: Int,
    // Store seen messages to dedupe for this long.
    dedupeTimeout: Duration
)

case class UnAckedMessage(message: Message[Pending], deadline: Time, retry: Int)

case class SeenMessageData(deadline: Time)

case class ReliableBroadcaster(
    config: ReliableBroadcasterConfig,
    unAckedMessages: List[UnAckedMessage],
    seenMessages: Map[MessageUniqueID, SeenMessageData]
) extends Behavior {

  private def ack(message: Message[Scheduled]): Message[Drafted] =
    Message[Drafted](
      message.respondTo,
      DeliverySemantics(message.responseStateTo, Protocol.empty),
      Content.empty
    )

  private def isReliable(message: Message[?]): Boolean = message
    .deliverySemantics.broadcastProtocols.contains[Reliable]

  private def resend(expectedResponse: UnAckedMessage): Message[Drafted] =
    expectedResponse.message.copy(messageStage =
      Drafted(Single(expectedResponse.message.messageStage.receiverId))
    )

  // 1. Ack incoming reliable messages.
  // 2. Process incoming acks.
  // 3. Filter duplicate reliable messages.
  // 4. Log incoming reliable messages.
  // 5. Remove seen messages past their deadline.
  override def preAction(using
      ctx: ExecutionContext
  )(state: NodeState): UpdatedState = {
    val acksToSend = state.incomingMessages.filter(isReliable).map(ack)

    val ackedMessageIDs = state.incomingMessages
      .map(_.deliverySemantics.broadcastProtocols.get[ReliableAck])
      .map(_.map(_.id))
    val remainingUnackedMessages = unAckedMessages
      .filter(m => ackedMessageIDs.contains(m.message.uniqueID))

    val dedupedIncomingMessages = state.incomingMessages
      .filter(m => seenMessages.contains(m.uniqueID))

    val updatedSeenMessages = state.incomingMessages
      .foldLeft(seenMessages)((sm, m) =>
        sm.updated(m.uniqueID, SeenMessageData(ctx.time + config.ackTimeout))
      ).filter(_._2.deadline <= ctx.time)

    val updatedState = acksToSend
      .foldLeft(state)((s, m) => s.withOutgoingMessage(m))
      .copy(incomingMessages = dedupedIncomingMessages)

    val updatedSelf = this.copy(
      unAckedMessages = remainingUnackedMessages,
      seenMessages = updatedSeenMessages
    )

    UpdatedState(updatedSelf, updatedState)
  }

  // 1. Resend un-acked messages.
  // 2. Update deadlines and retry counts and remove messages that exceed their retry limit.
  override def mainAction(using
      ctx: ExecutionContext
  )(state: NodeState): UpdatedState =
    val updatedState =
      unAckedMessages.filter(_.deadline <= ctx.time).map(resend)
        .foldLeft(state)((s, m) => s.withOutgoingMessage(m))
    val updatedUnAckedMessages = unAckedMessages.collect {
      case e: UnAckedMessage if e.deadline < ctx.time       => e
      case e: UnAckedMessage if e.retry < config.maxRetries =>
        e.copy(deadline = ctx.time + config.ackTimeout, retry = e.retry + 1)
    }
    UpdatedState(
      this.copy(unAckedMessages = updatedUnAckedMessages),
      updatedState
    )

  // Log outgoing messages that ask for reliable broadcast.
  override def postAction(using
      ctx: ExecutionContext
  )(sharedState: NodeState): UpdatedState =
    val newExpectedResponses = sharedState.outgoingMessages.filter(isReliable)
      .map(m => UnAckedMessage(m, ctx.time + config.ackTimeout, 0))
    UpdatedState(
      this.copy(unAckedMessages = unAckedMessages ::: newExpectedResponses),
      sharedState
    )
}
