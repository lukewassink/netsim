package com.lukewassink.simulation.message

import MessageID.MessageID
import com.lukewassink.simulation.core.NodeID.NodeID
import com.lukewassink.simulation.core.*
import com.lukewassink.simulation.message.RecipientSpecification.Single
import com.lukewassink.simulation.message.ResponseState.{Request, Response}
import com.lukewassink.simulation.util.Time

// NOTE: a message ID uniquely identifies a message withing a node.
// The message's UniqueID(message ID, node ID) is required to uniquely
// identify the message within the network.
case object MessageID:
  opaque type MessageID = Int

  def apply(value: Int): MessageID = value

  extension (id: MessageID) def next: MessageID = MessageID(id + 1)

enum MessageStage:
  // Message has content and a recipient and is ready to be added to the outbox.
  case Drafted(recipientSpecification: RecipientSpecification)
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

import MessageStage.*

// The content of a message.
case class MessageContent(stringContent: String)

// A message that can be sent from one node toa another.
final case class Message[S <: MessageStage](
    messageStage: S,
    deliverySemantics: DeliverySemantics,
    content: MessageContent
):
  def isResponseTo(other: Message[Scheduled]): Boolean =
    deliverySemantics.responseState match
      case Request()                   => false
      case Response(nodeId, messageId) =>
        nodeId == other.messageStage.senderId &&
        messageId == other.messageStage.messageId

// Uniquely identifies the message modulo duplicated messages.
final case class MessageUniqueID(senderID: NodeID, messageID: MessageID)

case object Message {
  // Methods for Drafted messages.
  extension (message: Message[Drafted])
    def send(
        messageID: MessageID,
        senderID: NodeID,
        receiverID: NodeID,
        sendTime: Time
    ): Message[Pending] = Message(
      Pending(messageID, senderID, receiverID, sendTime),
      message.deliverySemantics,
      message.content
    )

  // Methods for Pending messages.
  extension (message: Message[Pending]) {
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
        message.deliverySemantics,
        message.content
      )
    }
  }

  // Methods for Scheduled messages.
  extension (message: Message[Scheduled]) {
    def readyToDeliver(time: Time): Boolean =
      message.messageStage.deliveryTime <= time

    def stillWaiting(time: Time): Boolean =
      message.messageStage.deliveryTime > time

    // Note: if this is also needed for Pending messages, move it to an extension of Pending | Scheduled,
    // and use pattern matching to extract the unique ID.
    def uniqueID: MessageUniqueID = MessageUniqueID(
      message.messageStage.senderId,
      message.messageStage.messageId
    )
  }
}
