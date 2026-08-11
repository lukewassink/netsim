package com.lukewassink.visualizer.core

import com.lukewassink.simulation.core.MessageStage.Scheduled
import com.lukewassink.simulation.core.{
  Message, MessageUniqueID, Network, NodeID
}
import com.lukewassink.simulation.util.Duration
import com.lukewassink.visualizer.util.Pos
import com.raquo.laminar.api.L.{*, given}
import com.lukewassink.visualizer.core.NetworkRenderer.FrameLength

// A message along with rendering data needed to render it.
case class MessageData(
    message: Message[Scheduled],
    center: Pos,
    // Signals the message to fade in/out.
    firstOrLast: Boolean
)

object MessageRenderer {
  // Package messages with their rendering data.
  def addData(
      network: Network,
      nodeData: Map[NodeID, NodeData]
  ): List[MessageData] = network.messagesInTransit.messages.map { message =>
    val time                                       = network.ctx.time
    val Scheduled(_, from, to, startTime, endTime) = message.messageStage
    val sender                                     = nodeData(from)
    val receiver                                   = nodeData(to)

    // The portion of its journey the message has completed.
    val t      = (time - startTime) / (endTime - startTime - Duration(2))
    val center = sender.center.interpolate(t, receiver.center)

    MessageData(
      message,
      center,
      time == startTime || time >= endTime - Duration(2)
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
      svg.cx <-- x,
      svg.cy <-- y
    )
  }
}
