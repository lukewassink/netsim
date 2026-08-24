package com.lukewassink.simulation.behavior

import com.lukewassink.simulation.message.MessageStage.{Drafted, Scheduled}
import com.lukewassink.simulation.core.{ExecutionContext, NodeState}
import com.lukewassink.simulation.message.RecipientSpecification.Single
import com.lukewassink.simulation.message.ResponseState.Response
import com.lukewassink.simulation.message.{Message, Content}
import com.lukewassink.simulation.util.Time

case class SimpleResponder() extends Behavior {
  private def createResponse(time: Time)(
      message: Message[Scheduled]
  ): Message[Drafted] =
    val messageStage = message.messageStage
    Message[Drafted](
      Drafted(Single(messageStage.senderId)),
      message.deliverySemantics.copy(responseState =
        Response(messageStage.senderId, messageStage.messageId)
      ),
      Content("Response to: " + message.content.stringContent)
    )

  override def mainAction(using
      ctx: ExecutionContext
  )(state: NodeState): UpdatedState = UpdatedState(
    this,
    state.incomingMessages.map(createResponse(ctx.time))
      .foldLeft(state)((state, response) => state.withOutgoingMessage(response))
  )
}
