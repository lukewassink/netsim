package com.lukewassink.simulation.behavior

import com.lukewassink.simulation.behavior.SimpleSender
import com.lukewassink.simulation.core.{NodeHeader, NodeState}
import com.lukewassink.simulation.message.Message
import com.lukewassink.simulation.test_utils.MessageSpecUtil.draftedMessage
import com.lukewassink.simulation.test_utils.ExecutionContextUtils.testContext
import com.lukewassink.simulation.test_utils.UnitSpec
import com.lukewassink.simulation.test_utils.NodeStateSpecUtil.testNodeState

class SimpleSenderSpec extends UnitSpec {
  private val message              = draftedMessage(2, "")
  private val sender               = SimpleSender(5, message)
  private val nodeState: NodeState = testNodeState(
    NodeHeader(1, 0),
    List.empty,
    List.empty
  )

  describe("trigger") {
    it("does nothing at earlier times") {
      sender.mainAction(using testContext(4))(nodeState).sharedState
        .outgoingMessages shouldBe empty
    }

    it("does nothing at later times") {
      sender.mainAction(using testContext(10))(nodeState).sharedState
        .outgoingMessages shouldBe empty
    }

    it("sends the message at the specified time") {
      sender.mainAction(using testContext(5))(nodeState).sharedState
        .outgoingMessages should contain theSameElementsAs
        List(message.send(0, 1, 2, 5))
    }
  }
}
