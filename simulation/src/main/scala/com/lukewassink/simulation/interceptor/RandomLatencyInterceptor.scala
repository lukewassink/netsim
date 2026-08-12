package com.lukewassink.simulation.interceptor

import com.lukewassink.simulation.core.{Message, MessageStage}
import com.lukewassink.simulation.util.Duration

case class RandomLatencyInterceptor(minLatency: Duration, maxLatency: Duration)
    extends MessageInterceptor {
  override def intercept(
      message: Message[MessageStage.Scheduled]
  ): List[Message[MessageStage.Scheduled]] = ???
}
