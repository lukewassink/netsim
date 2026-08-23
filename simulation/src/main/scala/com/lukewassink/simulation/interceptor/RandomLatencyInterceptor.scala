package com.lukewassink.simulation.interceptor

import com.lukewassink.simulation.core.ExecutionContext
import com.lukewassink.simulation.message.{Message, MessageStage}
import com.lukewassink.simulation.util.Time.milliseconds
import com.lukewassink.simulation.util.{Distribution, Duration}

// Adds a random, nonnegative Duration to the message delivery time.
case class RandomLatencyInterceptor(distribution: Distribution[Double])
    extends MessageInterceptor {
  override def intercept(using
      ExecutionContext
  )(
      message: Message[MessageStage.Scheduled]
  ): List[Message[MessageStage.Scheduled]] = List(message.copy(messageStage =
    message.messageStage.copy(deliveryTime =
      message.messageStage.deliveryTime + math.max(0, distribution.next)
        .milliseconds
    )
  ))
}
