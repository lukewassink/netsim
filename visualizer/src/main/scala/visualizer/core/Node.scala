package visualizer.core

import com.raquo.laminar.api.L.{*, given}
import core.{NetworkState, Node}
import visualizer.util.Pos
import visualizer.util.Pos.fromPolar
import visualizer.core.Network.SquareSideLength

// A node along with its rendering data.
case class RenderableNode(node: Node, center: Pos)

object NodeRenderer {
  // Radius of the circle of nodes, in px.
  private val NodesRadius = 300

  def toRenderable(network: NetworkState): Map[Int, RenderableNode] = {
    val n = network.nodes.size

    network.nodes.zipWithIndex
      .map((idToNode, i) => {
        val (id, node) = idToNode

        // Start at pi/2 so the first node is at 12 o'clock.
        val angle = (2 * math.Pi * i / n) - (math.Pi / 2)
        val center =
          Pos(SquareSideLength / 2, SquareSideLength / 2)
        id -> RenderableNode(node, center + fromPolar(angle, NodesRadius))
      })
      .toMap
  }

  def render(node: RenderableNode): SvgElement = {
    val Pos(x, y) = node.center
    svg.circle(
      svg.cls := "node",
      svg.cx := x.toString,
      svg.cy := y.toString
    )
  }
}
