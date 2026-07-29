package com.lukewassink.simulation.core

import com.lukewassink.simulation.util.Time

// NOTE: a message ID uniquely identifies a message withing a node.
// The pair (message ID, node ID) is required to uniquely identify the message within the network.
final case class MessageID(id: Int):
  def next: MessageID = MessageID(id + 1)

enum MessageStage:
  // Message has content and a recipient and is ready to be added to the outbox.
  case Drafted(receiverId: NodeID)
  // Message has been added to the node outbox to be sent.
  case Pending(
      messageId: MessageID,
      senderId: NodeID,
      receiverId: NodeID,
      sendTime: Time
  )
  // Message has been collected by the Network and scheduled to be delivered.
  case Scheduled(
      messageId: MessageID,
      senderId: NodeID,
      receiverId: NodeID,
      sendTime: Time,
      deliveryTime: Time
  )

import com.lukewassink.simulation.core.MessageStage.*

enum ResponseState:
  // Message is not a response to another message.
  case Request()
  // Message is a response to another message.
  case Response(nodeId: NodeID, messageId: MessageID)
import com.lukewassink.simulation.core.ResponseState.*

// The content of a message.
case class MessageContent(stringContent: String)

// A message that can be sent from one node toa another.
final case class Message[S <: MessageStage](
    messageStage: S,
    responseState: ResponseState,
    content: MessageContent
):
  def isResponseTo(other: Message[Scheduled]): Boolean = {
    responseState match
      case Request()                   => false
      case Response(nodeId, messageId) =>
        nodeId == other.messageStage.senderId && messageId == other.messageStage.messageId
  }

case object Message {
  // Methods for Drafted messages.
  extension (message: Message[Drafted])
    def send(
        messageId: MessageID,
        senderId: NodeID,
        sendTime: Time
    ): Message[Pending] =
      Message(
        Pending(
          messageId,
          senderId,
          message.messageStage.receiverId,
          sendTime
        ),
        message.responseState,
        message.content
      )

  // Methods for Pending messages.
  extension (message: Message[Pending])
    def schedule(deliveryTime: Time): Message[Scheduled] = {
      val messageStage = message.messageStage
      Message(
        Scheduled(
          messageStage.messageId,
          messageStage.senderId,
          messageStage.receiverId,
          messageStage.sendTime,
          deliveryTime
        ),
        message.responseState,
        message.content
      )
    }

  // Methods for Scheduled messages.
  extension (message: Message[Scheduled]) {
    def respond(content: MessageContent): Message[Drafted] = {
      val messageStage = message.messageStage
      Message[Drafted](
        Drafted(messageStage.senderId),
        Response(messageStage.senderId, messageStage.messageId),
        content
      )
    }

    def readyToDeliver(time: Time): Boolean =
      message.messageStage.deliveryTime <= time

    def stillWaiting(time: Time): Boolean =
      message.messageStage.deliveryTime > time
  }
}
