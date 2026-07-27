package com.lukewassink.simulation.behavior

import com.lukewassink.simulation.core.{
  Message,
  MessageContent,
  MessageHeader,
  NodeBehavior,
  NodeState
}
import com.lukewassink.simulation.util.Time

case class SimpleResponder() extends NodeBehavior {
  private def createResponse(time: Time)(message: Message): Message = {
    val header = message.header
    val responseHeader = MessageHeader(
      header.id,
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
      time: Time,
      state: NodeState
  ): NodeState =
    state.incomingMessages
      .map(createResponse(time))
      .foldLeft(state)((state, response) =>
        state.withOutgoingMessage(time, response)
      )
}
