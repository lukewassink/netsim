package com.lukewassink.visualizer.core

import com.lukewassink.visualizer.test_util.UnitSpec
import com.raquo.laminar.api.L.{*, given}
import com.lukewassink.visualizer.test_util.NetworkUtil.{nodeA, testNetwork}
import com.lukewassink.simulation.core.{Node, NodeHeader, NodeState}
import com.lukewassink.simulation.test_utils.NodeStateSpecUtil.testNodeState
import com.lukewassink.visualizer.util.Pos

class NodeRendererSpec extends UnitSpec {

  describe("addData") {
    val numNodes = testNetwork.nodes.size
    val nodeData = NodeRenderer.addData(testNetwork)

    it("generates data for each node")(nodeData should have size numNodes)

    it("copies the node to the node data") {
      val matchedNodes =
        for {
          (id, node)   <- testNetwork.nodes
          nodeFromData <- nodeData.get(id).map(_.node)
        } yield node -> nodeFromData

      matchedNodes should have size numNodes
      matchedNodes.foreach(===)
    }
  }

  describe("render") {
    it("renders a circle") {
      val node = Node(
        List.empty,
        testNodeState(NodeHeader(0, 0), List.empty, List.empty)
      )
      val nodeData = NodeData(node, Pos(5, 6))
      var nodeVar  = Var(nodeData)

      mount(
        NodeRenderer.render(0, nodeData, nodeVar.signal),
        "Node failed to mount"
      )

      expectNode(svg.circle.of(svg.cx is "5", svg.cy is "6"))
    }
  }
}
