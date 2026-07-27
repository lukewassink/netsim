package com.lukewassink.visualizer.core

import com.lukewassink.visualizer.test_util.UnitSpec
import com.raquo.domtestutils.matching.ExpectedNode.{comment, element, textNode}
import com.raquo.laminar.api.L.{*, given}

class RootSpec extends UnitSpec {
  describe("rootElement") {
    it("Renders the root SVG with a node and a message") {
      mount(Root.rootElement().ref, "Root failed to mount")

      expectNode(
        div.of(
          h1 of textNode,
          svg.svg of (
            svg.cls is "network-panel",
            comment,
            comment
          )
        )
      )
    }
  }
}
