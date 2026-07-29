package com.lukewassink.visualizer.core

import com.lukewassink.simulation.behavior.SimpleSender
import com.lukewassink.simulation.core.Node
import com.lukewassink.simulation.util.XORRandom
import com.lukewassink.visualizer.core.Network.currentTick
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

// Hook into the document and render the root element.
@main
def RenderRoot(): Unit =
  renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    Root.rootElement()
  )

// Render the root element of the page. All other elements descend from it.
object Root:
  // Size of the viewBox for the network root SVG.
  val NetworkViewBoxSize = 1000

  // Amount of time between ticks in milliseconds.
  val FrameLength = 200

  def rootElement(): Element = {
    val viewBox =
      s"0, 0, $NetworkViewBoxSize, $NetworkViewBoxSize"

    div(
      h1("NetSim"),
      svg.svg(
        svg.cls := "network-panel",
        svg.viewBox := viewBox,
        children <-- Network.nodeElements,
        children <-- Network.messageElements
//        onMountCallback(_ =>
//          dom.window.setInterval(() => currentTick.update(_ + 1), FrameLength)
//        )
      )
    )
  }
  end rootElement
end Root
