package com.lukewassink.simulation.core

import com.lukewassink.simulation.core.MessageStage.Scheduled
import com.lukewassink.simulation.util.Time

// A store of scheduled messages. Can return messages ready to be delivered based on time.
case class DeliveryQueue(messages: List[Message[Scheduled]]):

  def withMessage(message: Message[Scheduled]): DeliveryQueue =
    DeliveryQueue(message :: messages)

  def withMessages(messages: Iterable[Message[Scheduled]]): DeliveryQueue =
    messages.foldLeft(this)(_.withMessage(_))

  // Returns messages to be delivered by the specified time.
  def deliverableMessages(time: Time): List[Message[Scheduled]] =
    messages.filter(_.readyToDeliver(time))

  // Returns the queue with all messages with past or present delivery times removed.
  def withoutPastMessages(time: Time): DeliveryQueue =
    DeliveryQueue(messages.filter(_.stillWaiting(time)))

object DeliveryQueue {
  def empty: DeliveryQueue =
    DeliveryQueue(List.empty)

  def apply(messages: Message[Scheduled]*): DeliveryQueue =
    this(messages.toList)
}
