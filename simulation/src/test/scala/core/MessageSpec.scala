package core

import test_utils.UnitSpec

class MessageSpec extends UnitSpec {
  describe("A MessageQueue") {
    val content = MessageContent("")
    val message1 = Message(MessageHeader(0, 1, 2, 1, 9), content)
    val message2 = Message(MessageHeader(12, 5, 2, 2, 4), content)
    val message3 = Message(MessageHeader(19, 1, 2, 5, 8), content)
    val sampleQueue = MessageQueue(message1, message2, message3)

    describe("currentMessages") {
      it ("can return no messages") {
        assert(MessageQueue.empty.currentMessages(10).isEmpty)
        assert(sampleQueue.currentMessages(1).isEmpty)
      }
      it ("can return only some of the messages") {
        assert(sampleQueue.currentMessages(4) === List(message2))
      }
      it ("can return all messages") {
        assert(sampleQueue.currentMessages(10)  === List(message1, message2, message3))
      }
    }

    describe("withMessage") {
      it ("adds a message") {
        val queue = MessageQueue.empty
        assert(queue.currentMessages(10).isEmpty)
        val queueWithMeassage = queue.withMessage(message1)
        assert(queueWithMeassage.currentMessages(10) === List(message1))
      }
    }

    describe("withoutDeliveredMessages") {
      it ("removes delivered messages") {
        assert(sampleQueue.currentMessages(8) === List(message2, message3))
        assert(sampleQueue.withoutDeliveredMessages(8).currentMessages(10) === List(message1))
      }
    }
  }
}
