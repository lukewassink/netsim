package com.lukewassink.visualizer.core

import com.lukewassink.simulation.core.MessageStage.Scheduled
import com.lukewassink.simulation.core.{Message, Network, NodeID}
import com.lukewassink.visualizer.util.Pos
import com.raquo.laminar.api.L.{*, given}

// A message along with rendering data needed to render it.
case class MessageData(message: Message[Scheduled], center: Pos)

object MessageRenderer {
  // Wrap messages up along with their rendering data.
  def addData(
      network: Network,
      nodeData: Map[NodeID, NodeData]
  ): List[MessageData] = {
    network.messagesInTransit.messages
      .map(message => {
        val Scheduled(_, from, to, startTime, endTime) = message.messageStage
        val sender = nodeData(from)
        val receiver = nodeData(to)

        // The portion of its journey the message has completed.
        val t =
          (network.time - startTime) / (endTime - startTime)
        MessageData(
          message,
          sender.center.interpolate(t, receiver.center)
        )
      })
  }

  // Render an individual message.
  def render(message: MessageData): SvgElement = {
    val Pos(x, y) = message.center
    svg.circle(
      svg.cls := "message",
      svg.cx := x.toString,
      svg.cy := y.toString
    )
  }
}
