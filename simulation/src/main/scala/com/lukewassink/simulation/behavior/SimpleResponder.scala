package com.lukewassink.simulation.behavior

import com.lukewassink.simulation.core.MessageStage.{Drafted, Scheduled}
import com.lukewassink.simulation.core.{
  Message, MessageContent, NodeBehavior, NodeState
}
import com.lukewassink.simulation.util.Time

case class SimpleResponder() extends NodeBehavior {
  private def createResponse(time: Time)(
      message: Message[Scheduled]
  ): Message[Drafted] = message
    .respond(MessageContent("Response to: " + message.content.stringContent))

  override def updatedNodeState(time: Time, state: NodeState): NodeState =
    state.incomingMessages.map(createResponse(time)).foldLeft(state)(
      (state, response) => state.withOutgoingMessage(time, response)
    )
}
