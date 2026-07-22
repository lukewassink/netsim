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
  def rootElement(): Element =
    div(
      h1("NetSim"),
      svg.svg(
        svg.cls := "root-svg",
        svg.rect(
          svg.cls := "network-background-rect"
        ),
        children <-- Network.renderedNodes,
        children <-- Network.renderedMessages
      )
    )
  end rootElement
end Root
