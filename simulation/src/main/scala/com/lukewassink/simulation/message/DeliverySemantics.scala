package com.lukewassink.simulation.message

import MessageID.MessageID
import com.lukewassink.simulation.core.NodeID.NodeID
import ResponseState.*
import com.lukewassink.simulation.message.ReliableAcks
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

sealed trait Protocol

case class Reliable() extends Protocol

case class ReliableAcks(ids: List[MessageUniqueID]) extends Protocol:
  def withID(id: MessageUniqueID): ReliableAcks = ReliableAcks(id :: ids)

object BroadcastProtocols {
  def empty: Store[Protocol] = Store.empty[Protocol]

  def apply(protocols: Protocol*): Store[Protocol] = Store(protocols.toList)
}

case class DeliverySemantics(
    responseState: ResponseState,
    broadcastProtocols: Store[Protocol]
):
  def withReliableAck(id: MessageUniqueID): DeliverySemantics = this
    .copy(broadcastProtocols =
      broadcastProtocols.update[ReliableAcks](r =>
        Some(r.getOrElse[ReliableAcks](ReliableAcks(List.empty)).withID(id))
      )
    )

object DeliverySemantics {
  def empty: DeliverySemantics =
    new DeliverySemantics(Request(), BroadcastProtocols.empty)

  def apply(responseState: ResponseState): DeliverySemantics =
    new DeliverySemantics(responseState, BroadcastProtocols.empty)
}
