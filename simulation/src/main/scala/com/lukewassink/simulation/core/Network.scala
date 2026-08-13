package com.lukewassink.simulation.core

import com.lukewassink.simulation.core.MessageStage.Scheduled
import com.lukewassink.simulation.util.Time.TimeConverter
import com.lukewassink.simulation.util.{BaseDistribution, Chance, Duration, Time}
import com.lukewassink.simulation.util.Chance.*
import com.lukewassink.simulation.core.NodeID.NodeID

import scala.util.Random

// The time it takes for a message to be delivered. Hardcoded for now. Later this should be changed to be config
// based.
final val DeliveryLatency = Duration(10)

case class Network(
    ctx: ExecutionContext,
    nodes: Map[NodeID, Node],
    messagesInTransit: DeliveryQueue
) {

  // Logic:
  // 1) tick the time forward
  // 2) trigger pre-delivery node actions
  // 3) deliver messages
  // 4) trigger node behaviors
  // 5) collect outgoing messages from nodes
  def next: Network = {
    given nextCtx: ExecutionContext = ctx.withNextTime

    // Call node pre-delivery actions
    val initializedNodes = nodes.map(_ -> _.preDeliveryAction)

    // Deliver messages
    val nodesWithDeliveredMessages =
      messagesInTransit.deliverableMessages(nextCtx.time)
        .foldLeft(initializedNodes) { (nodes, message) =>
          nodes.updatedWith(message.messageStage.receiverId)(_.map(
            _.withIncomingMessage(message)
          ))
        }

    // Trigger node behavior
    val updatedNodes = nodesWithDeliveredMessages
      .map((id, node) => (id, node.postDeliveryAction))

    // New messages to deliver
    val toDeliver: Iterable[Message[Scheduled]] =
      for {
        (_, node) <- updatedNodes
        message   <- node.outgoingMessages
      } yield message.schedule(nextCtx.time + DeliveryLatency)

    // Clear delivered messages and add new messages
    val updatedMessages = messagesInTransit.withoutPastMessages(nextCtx.time)
      .withMessages(toDeliver)

    Network(nextCtx, updatedNodes, updatedMessages)
  }
}

object Network {
  // A convenience method to initialize Network using a list of nodes and list of messages.
  def apply(
      ctx: ExecutionContext,
      nodes: List[Node],
      deliveryQueue: DeliveryQueue
  ): Network = {
    val nodeMap: Map[NodeID, Node] =
      nodes.foldLeft(Map[NodeID, Node]()) { (map, node) =>
        map.updated(node.sharedState.header.id, node)
      }
    Network(ctx, nodeMap, deliveryQueue)
  }

  // A convenience method to initialize Network using a list of nodes and list of messages.
  def apply(
      ctx: ExecutionContext,
      nodes: List[Node],
      messages: List[Message[Scheduled]]
  ): Network = {
    val nodeMap: Map[NodeID, Node] =
      nodes.foldLeft(Map[NodeID, Node]()) { (map, node) =>
        map.updated(node.sharedState.header.id, node)
      }
    Network(ctx, nodeMap, DeliveryQueue(List.empty, messages))
  }

  def apply(
      time: Time,
      nodes: List[Node],
      messages: List[Message[Scheduled]]
  ): Network = Network(ExecutionContext(time, 1, 1), nodes, messages)
}
