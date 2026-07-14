package behavior

import core.{Message, MessageContent, MessageHeader, NodeHeader, NodeState}
import test_utils.MessageSpecUtil.testMessage
import test_utils.NodeSpecUtil.testNodeState
import test_utils.UnitSpec

class SimpleResponderSpec extends UnitSpec {
  describe("trigger") {
    val message1 = testMessage(2, 5, "One")
    val message2 = testMessage(2, 5, "Two")
    val responder = SimpleResponder()
    val nodeState = testNodeState(NodeHeader(2, 0), List.empty)

    it("handles zero messages") {
      responder
        .trigger(5, nodeState, List.empty)
        .outgoingMessages shouldBe empty
    }

    it("responds to one message") {
      val outgoingMessages =
        responder.trigger(5, nodeState, List(message1)).outgoingMessages
      outgoingMessages should have size 1
      all(outgoingMessages) should matchPattern {
        case Message(_, MessageContent("Response to: One")) =>
      }
    }

    it("responds to multiple messages") {
      val outgoingMessages =
        responder
          .trigger(5, nodeState, List(message1, message2))
          .outgoingMessages
      outgoingMessages should have size 2
      exactly(1, outgoingMessages) should matchPattern {
        case Message(_, MessageContent("Response to: One")) =>
      }
      exactly(1, outgoingMessages) should matchPattern {
        case Message(_, MessageContent("Response to: Two")) =>
      }
    }
  }
}
