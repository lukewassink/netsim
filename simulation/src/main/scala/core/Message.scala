package core

// A unit of data that can be sent between nodes.
case class Message(header: MessageHeader, content: MessageContent)

case class MessageHeader(messageId: Int, senderId: Int, receiverId: Int, sendTime: Int, deliveryTime: Int)

case class MessageContent(stringContent: String)


// A store of messages. Can return messages ready to be delivered based on time.
case class MessageQueue(messages: List[Message]):

  def withMessage(message: Message): MessageQueue =
    MessageQueue(message :: messages)

  def currentMessages(time: Int): List[Message] =
    messages.filter(_.header.deliveryTime <= time)

  def withoutDeliveredMessages(time: Int): MessageQueue =
    MessageQueue(messages.filter(_.header.deliveryTime > time))

object MessageQueue {
  def empty: MessageQueue =
    MessageQueue(List.empty)

  def apply(messages: Message*): MessageQueue = this (messages.toList)
}
