package com.lukewassink.simulation.core

import com.lukewassink.simulation.util.Time

// A store of messages. Can return messages ready to be delivered based on time.
case class MessageQueue(messages: List[Message]):

  def withMessage(message: Message): MessageQueue =
    MessageQueue(message :: messages)

  def withMessages(messages: Iterable[Message]): MessageQueue =
    messages.foldLeft(this)(_.withMessage(_))

  // Returns messages to be delivered by the specified time.
  def readyToDeliver(time: Time): List[Message] =
    messages.filter {
      _.header.deliveryTime match {
        case None    => false
        case Some(t) => t <= time
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
