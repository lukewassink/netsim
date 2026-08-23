package com.lukewassink.simulation.core

import com.lukewassink.simulation.core.DeliveryQueue
import com.lukewassink.simulation.core.DeliveryQueueSpec.TestInterceptor
import com.lukewassink.simulation.message.MessageStage.Scheduled
import com.lukewassink.simulation.interceptor.MessageInterceptor
import com.lukewassink.simulation.message.Message
import com.lukewassink.simulation.test_utils.MessageSpecUtil.{
  pendingMessage, scheduledMessage
}
import com.lukewassink.simulation.test_utils.UnitSpec

object DeliveryQueueSpec {
  case class TestInterceptor(replacementMessage: Message[Scheduled])
      extends MessageInterceptor:
    override def intercept(using
        ExecutionContext
    )(message: Message[Scheduled]): List[Message[Scheduled]] = List(
      replacementMessage
    )
}

class DeliveryQueueSpec extends UnitSpec {
  describe("DeliveryQueue") {
    given ExecutionContext = ExecutionContext(0, 1, 1)

    val message1    = scheduledMessage(0, 1, 2, 1, 9, "")
    val message2    = scheduledMessage(12, 5, 2, 2, 8, "")
    val message3    = scheduledMessage(19, 1, 2, 5, 8, "")
    val sampleQueue = DeliveryQueue(message1, message2, message3)

    describe("readyToDeliver") {
      it("can return no messages") {
        assert(DeliveryQueue.empty.deliverableMessages(10).isEmpty)
        assert(sampleQueue.deliverableMessages(1).isEmpty)
      }

      it("can return messages that are ready to deliver") {
        sampleQueue.deliverableMessages(8) should contain theSameElementsAs
          List(message2, message3)
        sampleQueue.deliverableMessages(9) should contain theSameElementsAs
          List(message1, message2, message3)
      }
    }

    describe("withMessage") {
      it("adds a message") {
        val queue = DeliveryQueue.empty
        assert(queue.deliverableMessages(9).isEmpty)
        val queueWithMessage = queue.withMessage(message1)
        assert(queueWithMessage.deliverableMessages(9) === List(message1))
      }

      it("runs the interceptors") {
        val message     = scheduledMessage(1, 1, "Bye!")
        val interceptor = TestInterceptor(message)

        DeliveryQueue(List(interceptor), List.empty)
          .withMessage(scheduledMessage(1, 1, "Bye!")).messages should
          contain theSameElementsAs List(message)
      }
    }

    describe("withMessages") {
      it("adds the messages") {
        DeliveryQueue.empty.withMessages(List.empty).messages shouldBe empty
        DeliveryQueue.empty.withMessages(List(message1)).messages should
          contain theSameElementsAs List(message1)
        DeliveryQueue.empty.withMessages(List(message1, message2, message3))
          .messages should contain theSameElementsAs
          List(message1, message2, message3)
      }
    }

    describe("withoutDeliveredMessages") {
      it("removes delivered messages") {
        sampleQueue.deliverableMessages(8) should contain theSameElementsAs
          List(message2, message3)
        sampleQueue.withoutPastMessages(8).deliverableMessages(9) should
          contain theSameElementsAs List(message1)
      }
    }

    describe("allMessages") {
      it("returns all messages") {
        DeliveryQueue.empty.messages shouldBe empty
        DeliveryQueue(message1).messages should contain theSameElementsAs
          List(message1)
        DeliveryQueue(message1, message2, message3).messages should
          contain theSameElementsAs List(message1, message2, message3)
      }
    }
  }
}
