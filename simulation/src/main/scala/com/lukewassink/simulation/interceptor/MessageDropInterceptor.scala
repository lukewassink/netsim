package com.lukewassink.simulation.interceptor

import com.lukewassink.simulation.core.MessageStage.Scheduled
import com.lukewassink.simulation.core.{ExecutionContext, Message, MessageStage}
import com.lukewassink.simulation.util.Chance.*

case class MessageDropInterceptor(chanceOfDroppingMessage: Chance)
    extends MessageInterceptor:
  override def intercept(using
      ctx: ExecutionContext
  )(message: Message[Scheduled]): List[Message[Scheduled]] =
    if ctx.chances(chanceOfDroppingMessage) then List.empty else List(message)
