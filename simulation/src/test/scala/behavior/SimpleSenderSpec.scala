package behavior

import core.{Message, MessageContent, MessageHeader, NodeState}
import test_utils.UnitSpec

class SimpleSenderSpec extends UnitSpec {
  val message = Message(MessageHeader(0, 1, 2, 5, 9), MessageContent(""))
  val sender = SimpleSender(5, message)
  val nodeState = NodeState(List.empty)

  describe("trigger") {
    it("does nothing for earlier times") {
      sender.trigger(4, nodeState, List.empty).outgoingMessages shouldBe empty
    }

    it("does nothing for later times") {
      sender
        .trigger(5, nodeState, List.empty)
        .outgoingMessages should contain theSameElementsAs List(message)
    }

    it("does sends the message at the specified time") {
      sender.trigger(6, nodeState, List.empty).outgoingMessages shouldBe empty
    }
  }
}
