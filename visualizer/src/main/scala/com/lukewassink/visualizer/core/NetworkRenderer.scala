package com.lukewassink.visualizer.core

import com.raquo.laminar.api.L.{*, given}
import com.lukewassink.simulation.core.{Network, NodeID}
import com.lukewassink.visualizer.core.{
  NodeRenderer, MessageRenderer, NetworkState
}
import com.lukewassink.visualizer.core.RootRenderer.given

// Render the network panel.
object NetworkRenderer {
  // Size of the viewBox for the network root SVG.
  val NetworkViewBoxSize = 1000

  // Amount of time between ticks in milliseconds.
  val FrameLength = 200

  def render(using networkState: NetworkState): SvgElement = {
    val network = networkState.network

    val viewBox = s"0, 0, $NetworkViewBoxSize, $NetworkViewBoxSize"

    val nodeData: Signal[Map[NodeID, NodeData]] = network
      .map(NodeRenderer.addData)

    val nodeElements: Signal[List[SvgElement]] =
      nodeData.map(_.values.toList).split(_.node.id)(NodeRenderer.render)

    val messageData: Signal[List[MessageData]] = network.combineWith(nodeData)
      .mapN(MessageRenderer.addData)

    val messageElements: Signal[List[SvgElement]] =
      messageData.split(_.message.uniqueID)(MessageRenderer.render)

    svg.svg(
      svg.cls     := "network-panel",
      svg.viewBox := viewBox,
      children <-- nodeElements,
      children <-- messageElements
    )
  }
}
