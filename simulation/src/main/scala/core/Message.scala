package core

// A unit of data that can be sent between nodes.
case class Message(header: MessageHeader, content: MessageContent)

case class MessageHeader(
    messageId: Int,
    senderId: Int,
    receiverId: Int,
    sendTime: Int,
    deliveryTime: Option[Int]
)

case class MessageContent(stringContent: String)

// A store of messages. Can return messages ready to be delivered based on time.
case class MessageQueue(messages: List[Message]):

  def withMessage(message: Message): MessageQueue =
    MessageQueue(message :: messages)

  // Returns messages to be delivered at the current time.
  def currentMessages(time: Int): List[Message] =
    messages.filter {
      _.header.deliveryTime match {
        case None    => false
        case Some(t) => t == time
      }
    }

  // Returns the queue with all messages with past or present delivery times removed.
  def withoutPastMessages(time: Int): MessageQueue = {
    val filteredMessages = messages.filter {
      _.header.deliveryTime match {
        case None    => true
        case Some(t) => t > time
      }
    }
    MessageQueue(filteredMessages)
  }

object MessageQueue {
  def empty: MessageQueue =
    MessageQueue(List.empty)

  def apply(messages: Message*): MessageQueue = this(messages.toList)
}
