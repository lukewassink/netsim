package com.lukewassink.visualizer.core

import com.lukewassink.visualizer.test_util.UnitSpec
import com.lukewassink.visualizer.test_util.NetworkUtil.testNetwork

class MessageRendererSpec extends UnitSpec {

  describe("addData") {
    val numMessages = testNetwork.messagesInTransit.messages.size
    val nodeData = NodeRenderer.addData(testNetwork)
    val messageData = MessageRenderer.addData(testNetwork, nodeData)

    it("generates data for each message") {
      messageData should have size numMessages
    }

    it("copies the message to the message data") {
      val messagesInFlight = testNetwork.messagesInTransit.messages
      messageData.map(
        _.message
      ) should contain theSameElementsAs messagesInFlight
    }
  }
}
