package com.lukewassink.simulation.behavior

import com.lukewassink.simulation.behavior.SimpleResponder
import com.lukewassink.simulation.core.{ExecutionContext, NodeHeader}
import com.lukewassink.simulation.message.{Message, MessageContent}
import com.lukewassink.simulation.test_utils.UnitSpec
import com.lukewassink.simulation.test_utils.MessageSpecUtil.scheduledMessage
import com.lukewassink.simulation.test_utils.ExecutionContextUtils.testContext
import com.lukewassink.simulation.test_utils.NodeStateSpecUtil.testNodeState
import com.lukewassink.simulation.util.Time

class SimpleResponderSpec extends UnitSpec {
  describe("updated") {
    given ExecutionContext = testContext(5)

    val message1  = scheduledMessage(2, 5, "One")
    val message2  = scheduledMessage(2, 5, "Two")
    val responder = SimpleResponder()
    val noMessagesState = testNodeState(NodeHeader(2, 0), List.empty, List.empty)
    val oneMessageState = testNodeState(
      NodeHeader(2, 0),
      List.empty,
      List(message1)
    )
    val twoMessagesState = testNodeState(
      NodeHeader(2, 0),
      List.empty,
      List(message1, message2)
    )

    it("handles zero messages") {
      responder.mainAction(noMessagesState).sharedState
        .outgoingMessages shouldBe empty
    }

    it("responds to one message") {
      val outgoingMessages =
        responder.mainAction(oneMessageState).sharedState.outgoingMessages
      outgoingMessages should have size 1
      all(outgoingMessages) should have(stringContent("Response to: One"))
    }

    it("responds to multiple messages") {
      val outgoingMessages =
        responder.mainAction(twoMessagesState).sharedState.outgoingMessages
      outgoingMessages should have size 2
      exactly(1, outgoingMessages) should have(stringContent("Response to: One"))
      exactly(1, outgoingMessages) should have(stringContent("Response to: Two"))
    }
  }
}
