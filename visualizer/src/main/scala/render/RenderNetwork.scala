package render

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

@main
def RenderNetwork(): Unit =
  renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    Main.appElement()
  )

object Main:
  def appElement(): Element =
    div(
      h1("NetSim"),
      svg.svg(
        svg.cls := "root-svg",
        svg.rect(
          svg.cls := "network-background-rect",
          svg.x := "50%",
          svg.y := "10",
          svg.height := "700",
          svg.width := "700",
          svg.transform := "translate(-350, 0)"
        )
      )
    )
  end appElement
end Main
