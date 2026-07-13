package behavior

import core.{Message, MessageContent, MessageHeader, NodeBehavior, NodeState}

case class SimpleResponder() extends NodeBehavior {
  private def createResponse(message: Message): Message = {
    val header = message.header
    val responseHeader = MessageHeader(
      header.messageId,
      header.receiverId,
      header.senderId,
      header.deliveryTime,
      header.deliveryTime + 1
    )
    val responseContent = MessageContent(
      "Response to: " + message.content.stringContent
    )
    Message(responseHeader, responseContent)
  }

  def trigger(
      time: Int,
      state: NodeState,
      deliveredMessages: List[Message]
  ): NodeState =
    deliveredMessages
      .map(createResponse)
      .foldLeft(state)((state, response) => state.withOutgoingMessage(time, response))
}
