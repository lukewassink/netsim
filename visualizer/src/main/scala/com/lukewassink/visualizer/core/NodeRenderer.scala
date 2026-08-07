package com.lukewassink.visualizer.core

import com.raquo.laminar.api.L.{*, given}
import NetworkRenderer.NetworkViewBoxSize
import com.lukewassink.visualizer.util.Pos
import com.lukewassink.visualizer.util.Pos.fromPolar
import com.lukewassink.simulation.core.{Network, Node, NodeID}

// A node along with its rendering data.
case class NodeData(node: Node, center: Pos)

object NodeRenderer {
  // Package nodes with their rendering data.
  def addData(network: Network): Map[NodeID, NodeData] = {
    val n      = network.nodes.size
    val radius = 0.45 * NetworkViewBoxSize

    network.nodes.zipWithIndex.map { (idToNode, i) =>
      val (id, node) = idToNode

      // Start at pi/2 so the first node is at 12 o'clock.
      val angle  = (2 * math.Pi * i / n) - (math.Pi / 2)
      val center = Pos(NetworkViewBoxSize / 2, NetworkViewBoxSize / 2)
      id -> NodeData(node, center + fromPolar(angle, radius))
    }.toMap
  }

  def render(
      id: NodeID,
      original: NodeData,
      data: Signal[NodeData]
  ): SvgElement = {
    val x = data.map(_.center.x.toString)
    val y = data.map(_.center.y.toString)

    svg.circle(svg.cls := "node", svg.cx <-- x, svg.cy <-- y)
  }
}
