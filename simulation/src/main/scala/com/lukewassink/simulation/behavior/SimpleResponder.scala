package com.lukewassink.simulation.behavior

import com.lukewassink.simulation.core.MessageStage.{Drafted, Scheduled}
import com.lukewassink.simulation.core.{
  Message, MessageContent, ExecutionContext, NodeState
}
import com.lukewassink.simulation.util.Time

case class SimpleResponder() extends Behavior {
  private def createResponse(time: Time)(
      message: Message[Scheduled]
  ): Message[Drafted] = message
    .respond(MessageContent("Response to: " + message.content.stringContent))

  override def updated(using
      ctx: ExecutionContext
  )(state: NodeState): UpdatedState = UpdatedState(
    state.incomingMessages.map(createResponse(ctx.time))
      .foldLeft(state)((state, response) => state.withOutgoingMessage(response)),
    this
  )
}
