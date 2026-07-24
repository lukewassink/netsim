package visualizer.core

import com.raquo.laminar.api.L.{*, given}
import core.{NetworkState, Node}
import visualizer.core.Root.NetworkPanelSideLength
import visualizer.util.Pos
import visualizer.util.Pos.fromPolar

// A node along with its rendering data.
case class NodeData(node: Node, center: Pos)

object NodeRenderer {
  // Radius of the circle of nodes, in px.
  private val NodesRadius = 300

  def addData(network: NetworkState): Map[Int, NodeData] = {
    val n = network.nodes.size

    network.nodes.zipWithIndex
      .map((idToNode, i) => {
        val (id, node) = idToNode

        // Start at pi/2 so the first node is at 12 o'clock.
        val angle = (2 * math.Pi * i / n) - (math.Pi / 2)
        val center =
          Pos(NetworkPanelSideLength / 2, NetworkPanelSideLength / 2)
        id -> NodeData(node, center + fromPolar(angle, NodesRadius))
      })
      .toMap
  }

  def render(node: NodeData): SvgElement = {
    val Pos(x, y) = node.center
    svg.circle(
      svg.cls := "node",
      svg.cx := x.toString,
      svg.cy := y.toString
    )
  }
}
