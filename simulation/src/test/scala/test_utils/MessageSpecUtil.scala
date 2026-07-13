package test_utils

import core.{Message, MessageContent, MessageHeader}

object MessageSpecUtil {
  def testMessage(
      receiverId: Int,
      deliveryTime: Int,
      content: String
  ): Message =
    Message(
      MessageHeader(0, 0, receiverId, 0, deliveryTime),
      MessageContent(content)
    )
}
