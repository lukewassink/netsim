package com.lukewassink.simulation.test_utils

import com.lukewassink.simulation.core.ResponseState.Request
import com.lukewassink.simulation.core.MessageStage.{Drafted, Pending, Scheduled}
import com.lukewassink.simulation.core.{
  Message, MessageContent, MessageID, NodeID
}
import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}
import com.lukewassink.simulation.util.Time
import com.lukewassink.simulation.core.NodeID.NodeID
import com.lukewassink.simulation.core.MessageID.MessageID

object MessageSpecUtil {
  def draftedMessage(receiverId: NodeID, content: String): Message[Drafted] =
    Message[Drafted](Drafted(receiverId), Request(), MessageContent(content))

  def pendingMessage(
      messageId: MessageID,
      nodeId: NodeID,
      receiverId: NodeID,
      sendTime: Time,
      content: String
  ): Message[Pending] = Message(
    Pending(messageId, nodeId, receiverId, sendTime),
    Request(),
    MessageContent(content)
  )

  def pendingMessage(receiverId: NodeID, content: String): Message[Pending] =
    pendingMessage(MessageID(0), NodeID(0), receiverId, Time(0), content)

  def scheduledMessage(
      messageId: MessageID,
      nodeId: NodeID,
      receiverId: NodeID,
      sendTime: Time,
      deliveryTime: Time,
      content: String
  ): Message[Scheduled] = pendingMessage(
    messageId,
    nodeId,
    receiverId,
    sendTime,
    content
  ).schedule(deliveryTime)

  def scheduledMessage(
      receiverId: NodeID,
      deliveryTime: Time,
      content: String
  ): Message[Scheduled] = pendingMessage(receiverId, content)
    .schedule(deliveryTime)
}

trait MessageMatchers {
  // Custom matcher to test the stringContent of messages.
  def stringContent(
      expectedContent: String
  ): HavePropertyMatcher[Message[?], String] =
    (message: Message[?]) =>
      HavePropertyMatchResult(
        message.content.stringContent == expectedContent,
        "stringContent",
        expectedContent,
        message.content.stringContent
      )

  // Custom matcher to test the messageID of messages.
  def messageID(
      expectedID: MessageID
  ): HavePropertyMatcher[Message[Pending], MessageID] =
    (message: Message[Pending]) =>
      HavePropertyMatchResult(
        message.messageStage.messageId == expectedID,
        "messageId",
        expectedID,
        message.messageStage.messageId
      )
}
