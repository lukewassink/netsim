package com.lukewassink.visualizer.processing

import com.lukewassink.simulation.core.Message
import com.lukewassink.simulation.core.MessageStage.Scheduled
import com.lukewassink.simulation.util.LogEvent.MessageDropEvent
import com.lukewassink.simulation.util.{LogEvent, Logger, Time}
import com.lukewassink.visualizer.processing.SyntheticHistoryElement.DroppedMessageElement

type SyntheticHistoryRow = List[SyntheticHistoryElement]

// A network history computed from the log events.
// It complements the actual simulation history with additional history that's useful for rendering.
case class SyntheticHistory(history: Map[Int, SyntheticHistoryRow])

object SyntheticHistory:
  def apply(logger: Logger): SyntheticHistory =
    new SyntheticHistory(
      logger.exportLog.toList.flatten.flatMap(transform).groupMap(_.idx)(_.event)
        .withDefaultValue(List.empty)
    )

  private def transform(
      event: LogEvent
  ): List[(event: SyntheticHistoryElement, idx: Int)] =
    event match {
      case MessageDropEvent(message) =>
        val start = message.messageStage.sendTime.tick
        val end   = (message.messageStage.deliveryTime.tick + start) / 2
        (start to end)
          .map(idx => (event = DroppedMessageElement(message), idx = idx))
          .toList
    }

enum SyntheticHistoryElement:
  case DroppedMessageElement(message: Message[Scheduled])
