package com.lukewassink.simulation.interceptor

import com.lukewassink.simulation.core.{ExecutionContext, Message, MessageStage}
import com.lukewassink.simulation.util.Time.milliseconds
import com.lukewassink.simulation.util.{Distribution, Duration}

case class RandomLatencyInterceptor(distribution: Distribution)
    extends MessageInterceptor {
  override def intercept(using
      ExecutionContext
  )(
      message: Message[MessageStage.Scheduled]
  ): List[Message[MessageStage.Scheduled]] = List(message.copy(messageStage =
    message.messageStage.copy(deliveryTime =
      message.messageStage.deliveryTime + distribution.next.milliseconds
    )
  ))
}
