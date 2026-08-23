package com.lukewassink.simulation.core

import com.lukewassink.simulation.message.{
  DeliverySemantics, Message, MessageContent
}
import com.lukewassink.simulation.message.MessageStage.{Drafted, Scheduled}
import com.lukewassink.simulation.message.RecipientSpecification.Single
import com.lukewassink.simulation.message.ResponseState.Response
import com.lukewassink.simulation.test_utils.MessageSpecUtil.{
  draftedMessage, pendingMessage, scheduledMessage
}
import com.lukewassink.simulation.test_utils.UnitSpec

class MessageSpec extends UnitSpec {
  describe("Message[Drafted]") {
    describe("send") {
      it("adds node metadata to the message") {
        assert(
          draftedMessage(1, "Hi!").send(2, 3, 1, 14) ===
            pendingMessage(2, 3, 1, 14, "Hi!")
        )
      }
    }
  }

  describe("Message[Pending]") {
    describe("schedule") {
      it("adds the delivery time") {
        assert(
          pendingMessage(0, 0, 0, 0, "Hi!").schedule(5) ===
            scheduledMessage(0, 0, 0, 0, 5, "Hi!")
        )
      }
    }
  }

  describe("Message[Scheduled]") {
    val message = scheduledMessage(1, 2, 3, 4, 5, "Hi!")

    describe("readyToDeliver") {
      it("returns true if the message is ready to deliver") {
        assert(message.readyToDeliver(5))
        assert(message.readyToDeliver(6))
      }

      it("returns false if the message is not ready to deliver") {
        assert(!message.readyToDeliver(4))
        assert(!message.readyToDeliver(4.9))
      }
    }

    describe("stillWaiting") {
      it("returns true if the message is not ready to deliver") {
        assert(message.stillWaiting(4))
        assert(message.stillWaiting(4.9))
      }

      it("returns false if the message is ready to deliver") {
        assert(!message.stillWaiting(5))
        assert(!message.stillWaiting(6))
      }
    }

    describe("isResponseTo") {
      it("returns true if it is a response to the other message") {
        val response = Message[Drafted](
          Drafted(Single(2)),
          DeliverySemantics(Response(2, 1)),
          MessageContent("")
        )

        assert(response.isResponseTo(message))
      }

      it("returns false if it is not a response to the other message") {
        val response1 = Message[Drafted](
          Drafted(Single(3)),
          DeliverySemantics(Response(3, 1)),
          MessageContent("")
        )
        val response2 = Message[Drafted](
          Drafted(Single(2)),
          DeliverySemantics(Response(2, 0)),
          MessageContent("")
        )

        assert(!response1.isResponseTo(message))
        assert(!response2.isResponseTo(message))
      }
    }
  }
}
