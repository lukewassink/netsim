package visualizer.core

import behavior.SimpleSender
import com.raquo.laminar.api.L.{*, given}
import core.{Node, *}
import org.scalajs.dom
import util.XORRandom

// Hook into the document and render the root element.
@main
def RenderRoot(): Unit =
  renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    Root.rootElement()
  )

// Render the root element of the page. All other elements descend from it.
object Root:
  // Side length for the square the network renders in of, in px.
  // We can't set this in CSS because we need it to calculate the network layout.
  val NetworkPanelSideLength = 700

  def rootElement(): Element =
    div(
      h1("NetSim"),
      svg.svg(
        svg.cls := "network-panel",
        svg.height := NetworkPanelSideLength.toString,
        svg.width := NetworkPanelSideLength.toString,
        children <-- Network.nodeElements,
        children <-- Network.messageElements
      )
    )
  end rootElement
end Root
