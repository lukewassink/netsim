package com.lukewassink.visualizer.core

import com.lukewassink.simulation.core.MessageStage.Scheduled
import com.lukewassink.simulation.core.{Message, MessageUniqueID}
import com.lukewassink.simulation.util.{Duration, Time}
import com.lukewassink.visualizer.util.Pos
import com.raquo.laminar.api.L.{*, given}
import com.lukewassink.simulation.core.NodeID.NodeID
import com.lukewassink.visualizer.core.MessageStatus.{Default, Dropped}
import com.lukewassink.visualizer.core.NetworkRenderer.FrameLength
import com.lukewassink.visualizer.processing.{
  SyntheticHistory, SyntheticHistoryElement
}
import com.lukewassink.visualizer.processing.SyntheticHistoryElement.DroppedMessageElement

enum MessageStatus:
  case Default
  case Dropped

// Current tick relative to the message journey.
case class Progress(first: Int, last: Int, cur: Int)

// A message along with rendering data needed to render it.
case class MessageData(
    message: Message[Scheduled],
    center: Pos,
    progress: Progress,
    messageStatus: MessageStatus
):
  val firstOrLast: Boolean =
    progress.cur <= progress.first || progress.cur >= progress.last

object MessageRenderer {
  // Package messages with their rendering data.
  def addDataToMessages(
      state: NetworkState,
      nodeData: Map[NodeID, NodeData]
  ): List[MessageData] = {
    val (network, row) = state
    val time           = network.ctx.time
    val inTransit      = network.messagesInTransit.messages
      .map(message => addDataToMessage(message, time, nodeData, Default))
    val dropped = row.collect { case DroppedMessageElement(m) =>
      addDataToMessage(m, time, nodeData, Dropped)
    }
    inTransit ::: dropped
  }

  private def addDataToMessage(
      message: Message[Scheduled],
      time: Time,
      nodeData: Map[NodeID, NodeData],
      messageStatus: MessageStatus
  ): MessageData = {
    val Scheduled(_, from, to, startTime, endTime) = message.messageStage
    val sender                                     = nodeData(from)
    val receiver                                   = nodeData(to)

    // The portion of its journey the message has completed.
    val t      = (time - startTime) / (endTime - startTime - Duration(2))
    val center = sender.center.interpolate(t, receiver.center)

    val last =
      messageStatus match {
        case Default => endTime.tick - 2
        case Dropped => SyntheticHistory.droppedMessageEndTick(message)
      }

    MessageData(
      message,
      center,
      Progress(startTime.tick, last, time.tick),
      messageStatus
    )
  }

  // Render an individual message.
  def render(
      id: MessageUniqueID,
      original: MessageData,
      data: Signal[MessageData]
  ): SvgElement = {
    val x = data.map(_.center.x.toString)
    val y = data.map(_.center.y.toString)

    svg.circle(
      svg.cls   := "message",
      svg.style := s"transition-duration: ${FrameLength}ms",
      svg.cls <-- data.map(_.firstOrLast).splitBoolean(_ => "", _ => "show"),
      svg.cls <-- data.map(_.messageStatus == Dropped)
        .splitBoolean(_ => "message--dropped", _ => ""),
      svg.cls <-- data.map(d =>
        d.messageStatus == Dropped && d.progress.cur >= d.progress.last - 1
      ).splitBoolean(_ => "message--dropped-last", _ => ""),
      svg.cx <-- x,
      svg.cy <-- y
    )
  }
}
