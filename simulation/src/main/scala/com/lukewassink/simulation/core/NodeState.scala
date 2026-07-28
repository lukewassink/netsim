package com.lukewassink.simulation.core

import com.lukewassink.simulation.util.{Random, Time}

// A node ID. It should uniquely identify the node.
case class NodeID(id: Int)

// Metadata for a node.
case class NodeHeader(id: NodeID, nextMessageId: Int)

// The shared internal state of the node. It contains incoming messages and any shared node history or data required by
// the behaviors. Individual behaviors can also store their own state.
case class NodeState(
    header: NodeHeader,
    outgoingMessages: List[Message],
    incomingMessages: List[Message],
    random: Random
) {
  def clearOutgoingMessages: NodeState =
    copy(outgoingMessages = List.empty)

  def clearIncomingMessages: NodeState =
    copy(incomingMessages = List.empty)

  // Sets the message ID, sender ID, and send time for outgoing messages and adds it to the list.
  def withOutgoingMessage(time: Time, message: Message): NodeState = {
    val messageWithNodeMetadata = message.copy(header =
      message.header.copy(
        id = MessageID(header.nextMessageId),
        senderId = header.id,
        sendTime = time
      )
    )

    copy(
      outgoingMessages = messageWithNodeMetadata :: outgoingMessages,
      header = header.copy(nextMessageId = header.nextMessageId + 1)
    )
  }

  def withIncomingMessage(message: Message): NodeState =
    copy(incomingMessages = message :: incomingMessages)
}
