package com.lukewassink.simulation.core

import com.lukewassink.simulation.core.MessageStage.Scheduled
import com.lukewassink.simulation.util.{Duration, Random, Time}

// The time it takes for a message to be delivered. Hardcoded for now. Later this should be changed to be config
// based.
final val DeliveryLatency = Duration(10)

// The total state of the network. The network consists of nodes and the current time.
case class Network(
    time: Time,
    nodes: Map[NodeID, Node],
    messagesInTransit: DeliveryQueue,
    random: Random
):

  // Logic:
  // 1) trigger pre-delivery node actions
  // 2) deliver messages
  // 3) trigger node behaviors
  // 4) collect outgoing messages from nodes
  // 5) tick the time forward
  def next(): Network = {
    // Call node pre-delivery actions
    val initializedNodes = nodes.map(_ -> _.preDeliveryAction(time))

    // Deliver messages
    val nodesWithDeliveredMessages =
      messagesInTransit
        .deliverableMessages(time)
        .foldLeft(initializedNodes) { (nodes, message) =>
          nodes.updatedWith(message.messageStage.receiverId)(
            _.map(_.withIncomingMessage(message))
          )
        }

    // Trigger node behavior
    val updatedNodes = nodesWithDeliveredMessages.map { (id, node) =>
      (id, node.postDeliveryAction(time))
    }

    // New messages to deliver
    val toDeliver: Iterable[Message[Scheduled]] = for {
      (_, node) <- updatedNodes
      message <- node.outgoingMessages
    } yield message.schedule(time + DeliveryLatency)

    // Clear delivered messages and add new messages
    val updatedMessages =
      messagesInTransit.withoutPastMessages(time).withMessages(toDeliver)

    Network(time.next, updatedNodes, updatedMessages, random)
  }

object Network {
  // A convenience method to initialize Network using a list of nodes and list of messages.
  def apply(
      time: Time,
      nodes: List[Node],
      messages: List[Message[Scheduled]],
      random: Random
  ): Network = {
    val nodeMap: Map[NodeID, Node] = nodes.foldLeft(Map[NodeID, Node]()) {
      (map, node) =>
        map.updated(node.sharedState.header.id, node)
    }
    Network(time, nodeMap, DeliveryQueue(messages), random)
  }
}
