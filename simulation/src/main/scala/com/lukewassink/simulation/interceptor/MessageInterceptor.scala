package com.lukewassink.simulation.interceptor

import com.lukewassink.simulation.core.{ExecutionContext, Message}
import com.lukewassink.simulation.core.MessageStage.Scheduled

trait MessageInterceptor:
  def intercept(using
      ExecutionContext
  )(message: Message[Scheduled]): List[Message[Scheduled]]
