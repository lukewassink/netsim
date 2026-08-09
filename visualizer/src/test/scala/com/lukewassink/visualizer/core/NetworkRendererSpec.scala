package com.lukewassink.visualizer.core

import com.lukewassink.visualizer.test_util.NetworkUtil.testNetwork
import com.lukewassink.visualizer.test_util.UnitSpec
import com.lukewassink.visualizer.util.DefaultNetwork
import com.raquo.laminar.api.L.{*, given}

class NetworkRendererSpec extends UnitSpec {
  describe("render") {
    it("Renders the network and updates it over time") {
      val networkVar = Var(testNetwork)
      mount(
        NetworkRenderer.render(using NetworkState(networkVar.signal)),
        "Root failed to mount"
      )

      expectNode(
        svg.svg of
          (
            svg.cls is "network-panel",
            sentinel, // nodes:
            svg.circle,
            svg.circle,
            svg.circle,
            sentinel, // messages:
            svg.circle,
            svg.circle,
            svg.circle
          )
      )

      // Tick 15 times so all messages have been sent.
      for _ <- 1 to 15 do networkVar.update(n => n.next)

      expectNode(
        svg.svg of
          (
            svg.cls is "network-panel",
            sentinel, // nodes:
            svg.circle,
            svg.circle,
            svg.circle,
            sentinel // messages:
          )
      )
    }
  }
}
