package com.lukewassink.runner.config

import com.lukewassink.simulation.test_utils.UnitSpec
import com.lukewassink.runner.config.BehaviorLocation.referenceLocations
import com.lukewassink.runner.config.BehaviorNode.{
  SimpleResponderNode, SimpleSenderNode
}

class BehaviorLocationSpec extends UnitSpec {
  describe("referenceLocations") {
    it("handles empty nodes") {
      val references =
        referenceLocations(SimulationNode("name", 10, NetworkNode(List.empty)))

      references shouldBe empty
    }

    it("handles empty behaviors") {
      val references = referenceLocations(SimulationNode(
        "name",
        10,
        NetworkNode(List(NodeNode("node-name", List.empty)))
      ))

      references shouldBe empty
    }

    it("returns the locations of name references with the names") {
      val references = referenceLocations(SimulationNode(
        "name",
        10,
        NetworkNode(List(
          NodeNode(
            "node1",
            List(SimpleSenderNode(10, "node2", "Hi!"), SimpleResponderNode())
          ),
          NodeNode(
            "node2",
            List(
              SimpleSenderNode(15, "node1", "Hi to you!"),
              SimpleSenderNode(20, "node1", "Hi again!")
            )
          )
        ))
      ))

      references should contain theSameElementsAs List(
        ReferenceLocation(
          "node2",
          BehaviorLocation("node1", 0, "SimpleSenderNode", 0)
        ),
        ReferenceLocation(
          "node1",
          BehaviorLocation("node2", 1, "SimpleSenderNode", 0)
        ),
        ReferenceLocation(
          "node1",
          BehaviorLocation("node2", 1, "SimpleSenderNode", 1)
        )
      )
    }
  }
}
