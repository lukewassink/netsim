package com.lukewassink.visualizer.core

import com.lukewassink.simulation.core.Network
import com.lukewassink.visualizer.test_util.UnitSpec
import com.raquo.domtestutils.matching.ExpectedNode.comment
import com.raquo.laminar.api.L.{*, given}
import com.lukewassink.visualizer.test_util.NetworkSpecUtil.testNetwork

class NetworkRendererSpec extends UnitSpec {
  describe("render") {
    it("renders nodes and messages") {
      val networkVar: Var[Network] = Var(testNetwork)
      val networkState = NetworkState(networkVar.signal)

      mount(NetworkRenderer.render(using networkState).ref)

      expectNode(
        svg.svg of (
          svg.cls is "network-panel",
          comment, // There should also be 3 svg.circles here
          comment // and also here
        )
      )
    }
  }
}
