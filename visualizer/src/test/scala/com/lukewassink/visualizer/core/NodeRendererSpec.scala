package com.lukewassink.visualizer.core

import com.lukewassink.visualizer.test_util.UnitSpec
import com.raquo.laminar.api.L.{*, given}
import com.lukewassink.visualizer.test_util.NetworkUtil.{nodeA, testNetwork}

class NodeRendererSpec extends UnitSpec {

  describe("addData") {
    val numNodes = testNetwork.nodes.size
    val nodeData = NodeRenderer.addData(testNetwork)

    it("generates data for each node") {
      nodeData should have size numNodes
    }

    it("copies the node to the node data") {
      val matchedNodes = for {
        (id, node) <- testNetwork.nodes
        nodeFromData <- nodeData.get(id).map(_.node)
      } yield node -> nodeFromData

      matchedNodes should have size numNodes
      matchedNodes.foreach(===)
    }
  }
}
