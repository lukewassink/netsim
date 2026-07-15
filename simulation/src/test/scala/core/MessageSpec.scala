package core

import test_utils.UnitSpec

class MessageSpec extends UnitSpec {
  describe("A MessageQueue") {
    val content = MessageContent("")
    val message1 = Message(MessageHeader(0, 1, 2, 1, Some(9)), content)
    val message2 = Message(MessageHeader(12, 5, 2, 2, Some(8)), content)
    val message3 = Message(MessageHeader(19, 1, 2, 5, Some(8)), content)
    val sampleQueue = MessageQueue(message1, message2, message3)

    describe("currentMessages") {
      it("can return no messages") {
        assert(MessageQueue.empty.currentMessages(10).isEmpty)
        assert(sampleQueue.currentMessages(1).isEmpty)
      }

      it("can return current messages") {
        sampleQueue.currentMessages(8) should contain theSameElementsAs List(
          message2,
          message3
        )
        sampleQueue.currentMessages(9) should contain theSameElementsAs List(
          message1
        )
      }
    }

    describe("withMessage") {
      it("adds a message") {
        val queue = MessageQueue.empty
        assert(queue.currentMessages(9).isEmpty)
        val queueWithMessage = queue.withMessage(message1)
        assert(queueWithMessage.currentMessages(9) === List(message1))
      }
    }

    describe("withMessages") {
      it("adds the messages") {
        MessageQueue.empty.withMessages(List.empty).allMessages shouldBe empty
        MessageQueue.empty
          .withMessages(List(message1))
          .allMessages should contain theSameElementsAs List(message1)
        MessageQueue.empty
          .withMessages(List(message1, message2, message3))
          .allMessages should contain theSameElementsAs List(
          message1,
          message2,
          message3
        )
      }
    }

    describe("withoutDeliveredMessages") {
      it("removes delivered messages") {
        sampleQueue.currentMessages(8) should contain theSameElementsAs List(
          message2,
          message3
        )
        sampleQueue
          .withoutPastMessages(8)
          .currentMessages(9) should contain theSameElementsAs List(message1)
      }
    }

    describe("allMessages") {
      it("returns all messages") {
        MessageQueue.empty.allMessages shouldBe empty
        MessageQueue(
          message1
        ).allMessages should contain theSameElementsAs List(message1)
        MessageQueue(
          message1,
          message2,
          message3
        ).allMessages should contain theSameElementsAs List(
          message1,
          message2,
          message3
        )
      }
    }
  }
}
