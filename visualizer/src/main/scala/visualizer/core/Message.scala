package visualizer.core

import com.raquo.laminar.api.L.{*, given}
import core.{Message, NetworkState}
import visualizer.util.Pos
import core.MessageHeader

// A message along with rendering data.
case class RenderableMessage(message: Message, center: Pos)

object MessageRenderer {
  def toRenderable(
      network: NetworkState,
      renderableNodes: Map[Int, RenderableNode]
  ): List[RenderableMessage] = {
    network.messagesInTransit.allMessages
      .map(message => {
        val MessageHeader(_, from, to, startTime, Some(endTime)) =
          message.header
        val sender = renderableNodes(from)
        val receiver = renderableNodes(to)

        // The portion of its journey the message has completed.
        val t =
          (network.time.toFloat - startTime) / (endTime - startTime)
        RenderableMessage(
          message,
          sender.center.interpolate(t, receiver.center)
        )
      })
  }

  def render(message: RenderableMessage): SvgElement = {
    val Pos(x, y) = message.center
    svg.circle(
      svg.cls := "message",
      svg.cx := x.toString,
      svg.cy := y.toString
    )
  }
}
