package com.lukewassink.simulation.test_utils

import com.lukewassink.simulation.core.{
  Message,
  MessageContent,
  MessageHeader,
  MessageID,
  NodeID
}
import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}
import com.lukewassink.simulation.util.Time

object MessageSpecUtil {
  def testMessage(
      receiverId: NodeID,
      deliveryTime: Time,
      content: String
  ): Message =
    Message(
      MessageHeader(
        MessageID(0),
        NodeID(0),
        receiverId,
        Time(0),
        Some(deliveryTime)
      ),
      MessageContent(content)
    )
}

trait MessageMatchers {
  // Custom matcher to check stringContent of messages in tests.
  def stringContent(
      expectedContent: String
  ): HavePropertyMatcher[Message, String] =
    (message: Message) =>
      HavePropertyMatchResult(
        message.content.stringContent == expectedContent,
        "stringContent",
        expectedContent,
        message.content.stringContent
      )
}
