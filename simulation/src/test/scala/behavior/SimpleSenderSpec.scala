package behavior

import core.{Message, MessageContent, MessageHeader, NodeHeader, NodeState}
import test_utils.NodeSpecUtil.testNodeState
import test_utils.UnitSpec

class SimpleSenderSpec extends UnitSpec {
  val message = Message(MessageHeader(0, 1, 2, 5, Some(9)), MessageContent(""))
  val sender = SimpleSender(5, message)
  val nodeState: NodeState =
    testNodeState(NodeHeader(1, 0), List.empty, List.empty)

  describe("trigger") {
    it("does nothing for earlier times") {
      sender
        .updated(4, nodeState)
        .sharedState
        .outgoingMessages shouldBe empty
    }

    it("does nothing for later times") {
      sender
        .updated(5, nodeState)
        .sharedState
        .outgoingMessages should contain theSameElementsAs List(message)
    }

    it("does sends the message at the specified time") {
      sender
        .updated(6, nodeState)
        .sharedState
        .outgoingMessages shouldBe empty
    }
  }
}
