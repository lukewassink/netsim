package com.lukewassink.simulation.message

import MessageID.MessageID
import com.lukewassink.simulation.core.NodeID.NodeID
import ResponseState.*
import com.lukewassink.simulation.util.Store

// Add OneOf, AllOf as needed.
// BroadcastProtocols can add details: gossip, linear, etc.
enum RecipientSpecification:
  case Single(id: NodeID)

enum ResponseState:
  // Message is not a response to another message.
  case Request()
  // Message is a response to another message.
  case Response(nodeId: NodeID, messageId: MessageID)

enum Protocol:
  case Reliable()

case class BroadcastProtocols(protocols: Store[Protocol])

object BroadcastProtocols {
  def empty: BroadcastProtocols = new BroadcastProtocols(Store.empty[Protocol])
}

case class DeliverySemantics(
    responseState: ResponseState,
    broadcastProtocols: BroadcastProtocols
)

object DeliverySemantics {
  def empty: DeliverySemantics =
    new DeliverySemantics(Request(), BroadcastProtocols.empty)

  def apply(response: Response): DeliverySemantics =
    new DeliverySemantics(response, BroadcastProtocols.empty)
}
