package com.lukewassink.simulation.core

import com.lukewassink.simulation.core.MessageStage.*
import com.lukewassink.simulation.util.{Random, Time}

// A node ID. It should uniquely identify the node.
case class NodeID(id: Int)

// Metadata for a node.
case class NodeHeader(id: NodeID, nextMessageId: MessageID)

// The shared internal state of the node. It contains incoming messages and any shared node history or data required by
// the behaviors. Individual behaviors can also store their own state.
case class NodeState(
    header: NodeHeader,
    outgoingMessages: List[Message[Pending]],
    incomingMessages: List[Message[Scheduled]]
) {
  def clearOutgoingMessages: NodeState = copy(outgoingMessages = List.empty)

  def clearIncomingMessages: NodeState = copy(incomingMessages = List.empty)

  // Sets the message ID, sender ID, and send time for outgoing messages and adds it to the list.
  def withOutgoingMessage(using
      ctx: ExecutionContext
  )(message: Message[Drafted]): NodeState = {
    val messageToSend = message.send(header.nextMessageId, header.id, ctx.time)

    copy(
      outgoingMessages = messageToSend :: outgoingMessages,
      header = header.copy(nextMessageId = header.nextMessageId.next)
    )
  }

  def withIncomingMessage(message: Message[Scheduled]): NodeState = copy(
    incomingMessages = message :: incomingMessages
  )
}
