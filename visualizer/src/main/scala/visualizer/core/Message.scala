package visualizer.core

import com.raquo.laminar.api.L.{*, given}
import core.{Message, NetworkState}
import visualizer.util.Pos
import core.MessageHeader

// A message along with rendering data needed to render it.
case class MessageData(message: Message, center: Pos)

object MessageRenderer {
  // Wrap messages up along with their rendering data.
  def addData(
               network: NetworkState,
               nodeData: Map[Int, NodeData]
  ): List[MessageData] = {
    network.messagesInTransit.allMessages
      .map(message => {
        val MessageHeader(_, from, to, startTime, Some(endTime)) =
          message.header
        val sender = nodeData(from)
        val receiver = nodeData(to)

        // The portion of its journey the message has completed.
        val t =
          (network.time.toFloat - startTime) / (endTime - startTime)
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
