package test_utils

import core.{Message, MessageContent, MessageHeader}
import org.scalatest.matchers.{HavePropertyMatcher, HavePropertyMatchResult}

object MessageSpecUtil {
  def testMessage(
      receiverId: Int,
      deliveryTime: Int,
      content: String
  ): Message =
    Message(
      MessageHeader(0, 0, receiverId, 0, Some(deliveryTime)),
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
