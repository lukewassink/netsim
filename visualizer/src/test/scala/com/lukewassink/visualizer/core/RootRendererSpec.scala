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
          svg.svg of
            (
              svg.cls is "network-panel",
              sentinel,
              svg.circle,
              svg.circle,
              svg.circle,
              sentinel
            ),
          div.of(button, input, input)
        )
      ))
    }
  }
}
