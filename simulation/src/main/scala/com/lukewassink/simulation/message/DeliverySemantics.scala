package com.lukewassink.simulation.message

import MessageID.MessageID
import com.lukewassink.simulation.core.NodeID.NodeID
import ResponseState.*
import com.lukewassink.simulation.util.Store

// Add OneOf, AllOf as needed.
// Protocol can add details: gossip, linear, etc.
enum RecipientSpecification:
  case Single(id: NodeID)

enum ResponseState:
  // Message is not a response to another message.
  case Request()
  // Message is a response to another message.
  case Response(nodeId: NodeID, messageId: MessageID)

enum Protocol:
  case Reliable()
  case ReliableAck(id: MessageUniqueID) // Acknowledge the message with this ID.

object Protocol {
  def empty: Store[Protocol] = Store.empty[Protocol]

  def apply(protocols: Protocol*): Store[Protocol] = Store(protocols.toList)
}

case class DeliverySemantics(
    responseState: ResponseState,
    broadcastProtocols: Store[Protocol]
)

object DeliverySemantics {
  def empty: DeliverySemantics = new DeliverySemantics(Request(), Protocol.empty)

  def apply(response: Response): DeliverySemantics =
    new DeliverySemantics(response, Protocol.empty)
}
