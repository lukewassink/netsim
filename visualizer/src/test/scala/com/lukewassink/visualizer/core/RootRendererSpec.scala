package com.lukewassink.visualizer.core

import com.lukewassink.visualizer.test_util.UnitSpec
import com.raquo.domtestutils.matching.ExpectedNode.{comment, element, textNode}
import com.raquo.laminar.api.L.{*, given}

class RootRendererSpec extends UnitSpec {
  describe("rootElement") {
    it("Renders the root SVG with a node and a message") {
      mount(RootRenderer.render(), "Root failed to mount")

      expectNode(div.of(
        h1 of textNode,
        div.of(
          div.of(
            svg.svg of
              (
                sentinel,
                // TODO(#49) add these back once the visualizer doesn't start with a blank state.
//                svg.circle,
//                svg.circle,
//                svg.circle,
                sentinel
              ),
            div.of(button, input, input)
          ),
          div.of(
            div.of(textArea, span.of("")),
            div.of(button.of("Run"), button.of("Reset Config"))
          )
        )
      ))
    }
  }
}
