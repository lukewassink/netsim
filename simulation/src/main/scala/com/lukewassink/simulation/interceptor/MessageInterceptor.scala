package com.lukewassink.simulation.interceptor

import com.lukewassink.simulation.core.Message
import com.lukewassink.simulation.core.MessageStage.Scheduled

trait MessageInterceptor:
  def intercept(message: Message[Scheduled]): List[Message[Scheduled]]
