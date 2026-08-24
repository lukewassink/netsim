package com.lukewassink.visualizer.core

import com.lukewassink.simulation.message.MessageStage.Scheduled
import com.lukewassink.simulation.message.{
  DeliverySemantics, Message, Content, MessageUniqueID
}
import com.lukewassink.visualizer.test_util.UnitSpec
import com.lukewassink.visualizer.test_util.NetworkUtil.testNetwork
import com.lukewassink.visualizer.util.Pos
import com.raquo.laminar.api.L.{*, given}
import com.lukewassink.simulation.test_utils.ImplicitConversions.given
import com.lukewassink.visualizer.core.MessageStatus.Default

class MessageRendererSpec extends UnitSpec {

  describe("addData") {
    val numMessages = testNetwork.messagesInTransit.messages.size
    val nodeData    = NodeRenderer.addData(testNetwork)
    val messageData = MessageRenderer
      .addDataToMessages((testNetwork, List.empty), nodeData)

    it("generates data for each message") {
      messageData should have size numMessages
    }

    it("copies the message to the message data") {
      val messagesInFlight = testNetwork.messagesInTransit.messages
      messageData.map(_.message) should contain theSameElementsAs
        messagesInFlight
    }
  }

  describe("render") {
    it("moves the message when messageData updates") {
      val id      = MessageUniqueID(0, 0)
      val message = Message[Scheduled](
        Scheduled(0, 0, 1, 5, 20),
        DeliverySemantics.empty,
        Content("")
      )
      val messageData = MessageData(
        message,
        Pos(5, 6),
        Progress(5, 18, 10),
        Default
      )
      val messageVar = Var(messageData)

      mount(
        MessageRenderer.render(id, messageData, messageVar.signal),
        "Message failed to mount"
      )

      expectNode(svg.circle.of(svg.cx is "5", svg.cy is "6"))

      messageVar.update(_ =>
        MessageData(message, Pos(8, 9), Progress(5, 18, 10), Default)
      )

      expectNode(svg.circle.of(svg.cx is "8", svg.cy is "9"))
    }
  }
}
