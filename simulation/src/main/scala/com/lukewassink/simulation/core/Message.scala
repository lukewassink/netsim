package com.lukewassink.simulation.core

import com.lukewassink.simulation.util.Time

// NOTE: a message ID uniquely identifies a message withing a node.
// The pair (message ID, node ID) is required to uniquely identify the message within the network.
case class MessageID(id: Int)

case class MessageHeader(
    id: MessageID,
    senderId: NodeID,
    receiverId: NodeID,
    sendTime: Time,
    deliveryTime: Option[Time]
)

// A unit of data that can be sent between nodes.
case class Message(header: MessageHeader, content: MessageContent)

case class MessageContent(stringContent: String)

// A store of messages. Can return messages ready to be delivered based on time.
case class MessageQueue(messages: List[Message]):

  def withMessage(message: Message): MessageQueue =
    MessageQueue(message :: messages)

  def withMessages(messages: Iterable[Message]): MessageQueue =
    messages.foldLeft(this)(_.withMessage(_))

  // Returns messages to be delivered at the current time.
  def currentMessages(time: Time): List[Message] =
    messages.filter {
      _.header.deliveryTime match {
        case None    => false
        case Some(t) => t == time
      }
    }

  // Returns the queue with all messages with past or present delivery times removed.
  def withoutPastMessages(time: Time): MessageQueue = {
    val filteredMessages = messages.filter {
      _.header.deliveryTime match {
        case None    => true
        case Some(t) => t > time
      }
    }
    MessageQueue(filteredMessages)
  }

  // Returns a list of all messages. For use in testing.
  def allMessages: List[Message] = messages

object MessageQueue {
  def empty: MessageQueue =
    MessageQueue(List.empty)

  def apply(messages: Message*): MessageQueue = this(messages.toList)
}
