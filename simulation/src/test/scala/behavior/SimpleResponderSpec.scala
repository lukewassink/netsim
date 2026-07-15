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
    val noMessagesState =
      testNodeState(NodeHeader(2, 0), List.empty, List.empty)
    val oneMessageState =
      testNodeState(NodeHeader(2, 0), List.empty, List(message1))
    val twoMessagesState =
      testNodeState(NodeHeader(2, 0), List.empty, List(message1, message2))

    it("handles zero messages") {
      responder
        .updated(5, noMessagesState)
        .sharedState
        .outgoingMessages shouldBe empty
    }

    it("responds to one message") {
      val outgoingMessages =
        responder
          .updated(5, oneMessageState)
          .sharedState
          .outgoingMessages
      outgoingMessages should have size 1
      all(outgoingMessages) should matchPattern {
        case Message(_, MessageContent("Response to: One")) =>
      }
    }

    it("responds to multiple messages") {
      val outgoingMessages =
        responder
          .updated(5, twoMessagesState)
          .sharedState
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
