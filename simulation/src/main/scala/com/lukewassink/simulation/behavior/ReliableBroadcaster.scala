package com.lukewassink.simulation.behavior

import com.lukewassink.simulation.behavior.ReliableBroadcaster.*
import com.lukewassink.simulation.core.NodeID.NodeID
import com.lukewassink.simulation.core.{ExecutionContext, NodeState}
import com.lukewassink.simulation.message.MessageID.MessageID
import com.lukewassink.simulation.message.{
  BroadcastProtocols, Content, DeliverySemantics, Message, MessageUniqueID
}
import com.lukewassink.simulation.message.MessageStage.{
  Drafted, Pending, Scheduled
}
import com.lukewassink.simulation.message.{Reliable, ReliableAcks}
import com.lukewassink.simulation.message.RecipientSpecification.Single
import com.lukewassink.simulation.message.ResponseState.Response
import com.lukewassink.simulation.util.{Duration, Time}

case class ReliableBroadcasterConfig(
    // How long to wait for an ack before retrying the message.
    incomingAckTimeout: Duration,
    // How many times to retry a message.
    maxRetries: Int,
    // How long to keep received messages for deduping.
    dedupeTimeout: Duration,
    // How long to wait to piggyback on an outgoing message before sending an ack.
    outgoingAckTimeout: Duration
)

case class SentUnAckedMessage(
    message: Message[Pending],
    deadline: Time,
    retry: Int
)

case class SeenMessageData(deadline: Time)

case class ReceivedMessageData(messageUniqueID: MessageUniqueID, deadline: Time)

case object ReliableBroadcaster {
  def empty(config: ReliableBroadcasterConfig): ReliableBroadcaster =
    new ReliableBroadcaster(config, List.empty, Map.empty, Map.empty)

  private def ack(id: MessageUniqueID): Message[Drafted] = Message[Drafted](
    Drafted(Single(id.senderID)),
    DeliverySemantics(Response(id.senderID, id.messageID)).withReliableAck(id),
    Content.empty
  )

  private def isReliable(message: Message[?]): Boolean = message
    .deliverySemantics.broadcastProtocols.contains[Reliable]

  private def resend(sentUnAckedMessage: SentUnAckedMessage): Message[Drafted] = {
    val semantics = sentUnAckedMessage.message.deliverySemantics
    // Resends can't be reliable. That triggers an infinite cascade of resends.
    val nonReliableSemantics = semantics
      .copy(broadcastProtocols = semantics.broadcastProtocols.without[Reliable])
    sentUnAckedMessage.message.copy(
      messageStage = Drafted(
        Single(sentUnAckedMessage.message.messageStage.receiverId)
      ),
      deliverySemantics = nonReliableSemantics
    )
  }
}

case class ReliableBroadcaster(
    config: ReliableBroadcasterConfig,
    sentUnAckedMessages: List[SentUnAckedMessage],
    seenMessages: Map[MessageUniqueID, SeenMessageData],
    receivedUnAckedMessages: Map[NodeID, List[ReceivedMessageData]]
) extends Behavior {
  private def withMessageToAck(using
      ctx: ExecutionContext
  )(message: Message[Scheduled]): ReliableBroadcaster = {
    val messageData = ReceivedMessageData(
      message.uniqueID,
      ctx.time + config.outgoingAckTimeout
    )
    this.copy(receivedUnAckedMessages =
      receivedUnAckedMessages.updatedWith(message.messageStage.senderId)(l =>
        Some(messageData :: l.getOrElse(List.empty))
      )
    )
  }

  // 1. Log incoming messages to ack.
  // 2. Process incoming acks.
  // 3. Filter duplicate messages.
  // 4. Log incoming messages.
  // 5. Remove seen messages past their deadline.
  override def preAction(using
      ctx: ExecutionContext
  )(state: NodeState): UpdatedState = {

    val updatedSelf =
      state.incomingMessages.filter(isReliable)
        .foldLeft(this)((s, message) => s.withMessageToAck(message))

    val ackedMessageIDs =
      state.incomingMessages
        .map(_.deliverySemantics.broadcastProtocols.get[ReliableAcks])
        .flatMap(_.map(_.ids)).flatten.toSet
    val remainingUnackedMessages = sentUnAckedMessages
      .filterNot(m => ackedMessageIDs.contains(m.message.uniqueID))

    val dedupedIncomingMessages = state.incomingMessages
      .filterNot(m => seenMessages.contains(m.uniqueID))

    val updatedSeenMessages = state.incomingMessages
      .foldLeft(seenMessages)((sm, m) =>
        sm.updated(
          m.uniqueID,
          SeenMessageData(ctx.time + config.incomingAckTimeout)
        )
      ).filter(_._2.deadline > ctx.time)

    UpdatedState(
      updatedSelf.copy(
        sentUnAckedMessages = remainingUnackedMessages,
        seenMessages = updatedSeenMessages
      ),
      state.copy(incomingMessages = dedupedIncomingMessages)
    )
  }

  // 1. Resend un-acked messages.
  // 2. Update deadlines and retry counts and remove messages that exceed their retry limit.
  // 3. Send acks that have reached their deadlines.
  override def mainAction(using
      ctx: ExecutionContext
  )(state: NodeState): UpdatedState = {
    val acksToSend = receivedUnAckedMessages.values.flatten
      .filter(_.deadline <= ctx.time).map(r => ack(r.messageUniqueID))

    val messagesToRetry = sentUnAckedMessages.filter(_.deadline <= ctx.time)
      .map(resend)

    val updatedNodeState = state.withOutgoingMessages(messagesToRetry)
      .withOutgoingMessages(acksToSend)

    val updatedSentUnAckedMessages = sentUnAckedMessages.collect {
      case u: SentUnAckedMessage if u.deadline > ctx.time           => u
      case u: SentUnAckedMessage if u.retry + 1 < config.maxRetries =>
        u.copy(
          deadline = ctx.time + config.incomingAckTimeout,
          retry = u.retry + 1
        )
    }

    UpdatedState(
      this.copy(
        sentUnAckedMessages = updatedSentUnAckedMessages,
        receivedUnAckedMessages = receivedUnAckedMessages
          .map((id, l) => id -> l.filter(_.deadline > ctx.time))
          .filterNot((_, l) => l.isEmpty)
      ),
      updatedNodeState
    )
  }

  private def withOutgoingAcks(using
      ctx: ExecutionContext
  )(message: Message[Pending]): Message[Pending] = {
    val idsToAck = receivedUnAckedMessages
      .getOrElse(message.messageStage.receiverId, List.empty)
      .map(_.messageUniqueID)
    message.copy(deliverySemantics =
      idsToAck
        .foldLeft(message.deliverySemantics)((d, id) => d.withReliableAck(id))
    )
  }

  // 1. Log outgoing messages that ask for reliable broadcast.
  // 2. Piggyback acks on outgoing messages.
  override def postAction(using
      ctx: ExecutionContext
  )(sharedState: NodeState): UpdatedState = {
    val newExpectedResponses = sharedState.outgoingMessages.filter(isReliable)
      .map(m => SentUnAckedMessage(m, ctx.time + config.incomingAckTimeout, 0))

    val updatedMessagesAndAcks =
      sharedState.outgoingMessages.foldLeft((
        messages = List.empty[Message[Pending]],
        acksToSend = receivedUnAckedMessages
      ))((messagesAndAcks, message) =>
        (
          withOutgoingAcks(message) :: messagesAndAcks.messages,
          messagesAndAcks.acksToSend
            .filterNot((nodeID, _) => message.messageStage.receiverId == nodeID)
        )
      )

    UpdatedState(
      this.copy(
        sentUnAckedMessages = sentUnAckedMessages ::: newExpectedResponses,
        receivedUnAckedMessages = updatedMessagesAndAcks.acksToSend
      ),
      sharedState.copy(outgoingMessages = updatedMessagesAndAcks.messages)
    )
  }
}
