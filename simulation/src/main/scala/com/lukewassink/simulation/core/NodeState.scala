package com.lukewassink.simulation.core

import com.lukewassink.simulation.message.MessageID.MessageID
import com.lukewassink.simulation.message.MessageStage.*
import com.lukewassink.simulation.core.NodeID.NodeID
import com.lukewassink.simulation.message.Message
import com.lukewassink.simulation.message.RecipientSpecification.Single

object NodeID:
  opaque type NodeID = Int

  def apply(value: Int): NodeID = value

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
  )(draft: Message[Drafted]): NodeState = {
    // When this can return more than one message, consider refactoring to handle
    // incrementing message IDs more gracefully.
    //
    // Also, consider extracting a helper function RecipientSpecification -> List[NodeID]
    // and moving it to another file.
    val pendingMessages =
      draft.messageStage.recipientSpecification match {
        case Single(id) =>
          Vector(draft.send(header.nextMessageId, header.id, id, ctx.time))
      }

    copy(
      outgoingMessages = pendingMessages.toList ::: outgoingMessages,
      header = header
        .copy(nextMessageId = pendingMessages.last.messageStage.messageId.next)
    )
  }

  def withIncomingMessage(message: Message[Scheduled]): NodeState = copy(
    incomingMessages = message :: incomingMessages
  )
}
