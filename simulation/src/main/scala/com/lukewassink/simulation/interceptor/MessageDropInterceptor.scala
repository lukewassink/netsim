package com.lukewassink.simulation.interceptor

import com.lukewassink.simulation.core.{ExecutionContext, Message, MessageStage}

case class MessageDropInterceptor(chanceOfDroppingMessage: Double)
    extends MessageInterceptor:
  override def intercept(using
      ctx: ExecutionContext
  )(
      message: Message[MessageStage.Scheduled]
  ): List[Message[MessageStage.Scheduled]] =
    if ctx.chances(chanceOfDroppingMessage) then List.empty else List(message)
