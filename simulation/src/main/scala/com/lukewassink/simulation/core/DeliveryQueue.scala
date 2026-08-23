package com.lukewassink.simulation.core

import com.lukewassink.simulation.message.MessageStage.Scheduled
import com.lukewassink.simulation.util.Time
import com.lukewassink.simulation.interceptor.MessageInterceptor
import com.lukewassink.simulation.message.Message

// A store of scheduled messages. Can return messages ready to be delivered based on time.
case class DeliveryQueue(
    interceptors: List[MessageInterceptor],
    messages: List[Message[Scheduled]]
):
  // Passes the message through all the interceptors in order and prepends the result to the queue.
  def withMessage(using
      ExecutionContext
  )(message: Message[Scheduled]): DeliveryQueue = {
    val interceptedMessages =
      interceptors.foldLeft(List(message))((ms, interceptor) =>
        ms.flatMap(interceptor.intercept)
      )
    this.copy(messages = interceptedMessages ::: messages)
  }

  def withMessages(using
      ExecutionContext
  )(messages: Iterable[Message[Scheduled]]): DeliveryQueue =
    messages.foldLeft(this)(_.withMessage(_))

  // Returns messages to be delivered by the specified time.
  def deliverableMessages(time: Time): List[Message[Scheduled]] = messages
    .filter(_.readyToDeliver(time))

  // Returns the queue with all messages with past or present delivery times removed.
  def withoutPastMessages(time: Time): DeliveryQueue = this
    .copy(messages = messages.filter(_.stillWaiting(time)))

object DeliveryQueue {
  def empty: DeliveryQueue = DeliveryQueue(List.empty, List.empty)

  def apply(messages: Message[Scheduled]*): DeliveryQueue = this(
    List.empty,
    messages.toList
  )
}
