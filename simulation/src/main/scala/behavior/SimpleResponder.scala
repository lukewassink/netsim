package behavior

import core.{Message, MessageContent, MessageHeader, NodeBehavior, NodeState}

case class SimpleResponder() extends NodeBehavior {
  private def createResponse(time: Int)(message: Message): Message = {
    val header = message.header
    val responseHeader = MessageHeader(
      header.messageId,
      header.receiverId,
      header.senderId,
      time,
      None
    )
    val responseContent = MessageContent(
      "Response to: " + message.content.stringContent
    )
    Message(responseHeader, responseContent)
  }

  override def updatedNodeState(
      time: Int,
      state: NodeState
  ): NodeState =
    state.incomingMessages
      .map(createResponse(time))
      .foldLeft(state)((state, response) =>
        state.withOutgoingMessage(time, response)
      )
}
