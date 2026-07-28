package com.lukewassink.simulation.behavior

import com.lukewassink.simulation.behavior.SimpleSender
import com.lukewassink.simulation.core.{Message, NodeHeader, NodeState}
import com.lukewassink.simulation.test_utils.MessageSpecUtil.draftedMessage
import com.lukewassink.simulation.test_utils.UnitSpec
import com.lukewassink.simulation.test_utils.NodeStateSpecUtil.testNodeState

class SimpleSenderSpec extends UnitSpec {
  private val message = draftedMessage(2, "")
  private val sender = SimpleSender(5, message)
  private val nodeState: NodeState =
    testNodeState(NodeHeader(1, 0), List.empty, List.empty)

  describe("trigger") {
    it("does nothing at earlier times") {
      sender
        .updated(4, nodeState)
        .sharedState
        .outgoingMessages shouldBe empty
    }

    it("does nothing at later times") {
      sender
        .updated(10, nodeState)
        .sharedState
        .outgoingMessages shouldBe empty
    }

    it("sends the message at the specified time") {
      sender
        .updated(5, nodeState)
        .sharedState
        .outgoingMessages should contain theSameElementsAs List(
        message.send(0, 1, 5)
      )
    }
  }
}
