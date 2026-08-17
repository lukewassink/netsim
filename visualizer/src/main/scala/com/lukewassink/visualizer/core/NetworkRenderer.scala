package com.lukewassink.visualizer.core

import com.raquo.laminar.api.L.{*, given}
import com.lukewassink.simulation.core.Network
import com.lukewassink.visualizer.core.{MessageRenderer, NodeRenderer}
import com.lukewassink.visualizer.core.RootRenderer.given
import com.lukewassink.simulation.core.NodeID.NodeID
import com.lukewassink.visualizer.util.NetworkState

// Render the network panel.
object NetworkRenderer {
  // Size of the viewBox for the network root SVG.
  val NetworkViewBoxSize = 1000

  // Amount of time between ticks in milliseconds.
  val FrameLength = 200

  def render(state: Signal[NetworkState]): SvgElement = {
    val viewBox = s"0, 0, $NetworkViewBoxSize, $NetworkViewBoxSize"

    val nodeData: Signal[Map[NodeID, NodeData]] = state
      .map(s => NodeRenderer.addData(s.network))

    val nodeElements: Signal[List[SvgElement]] =
      nodeData.map(_.values.toList).split(_.node.id)(NodeRenderer.render)

    val messageData: Signal[List[MessageData]] = state.combineWith(nodeData)
      .mapN(MessageRenderer.addDataToMessages)

    val messageElements: Signal[List[SvgElement]] =
      messageData.split(_.message.uniqueID)(MessageRenderer.render)

    svg.svg(
      svg.cls     := "panel panel--network",
      svg.viewBox := viewBox,
      children <-- nodeElements,
      children <-- messageElements
    )
  }
}
