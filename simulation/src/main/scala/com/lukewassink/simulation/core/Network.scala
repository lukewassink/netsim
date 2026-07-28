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
  // 1) tick the time forward
  // 2) trigger pre-delivery node actions
  // 3) deliver messages
  // 4) trigger node behaviors
  // 5) collect outgoing messages from nodes
  def next(): Network = {
    // Tick time
    val newTime = time.next

    // Call node pre-delivery actions
    val initializedNodes = nodes.map(_ -> _.preDeliveryAction(newTime))

    // Deliver messages
    val nodesWithDeliveredMessages =
      messagesInTransit
        .deliverableMessages(newTime)
        .foldLeft(initializedNodes) { (nodes, message) =>
          nodes.updatedWith(message.messageStage.receiverId)(
            _.map(_.withIncomingMessage(message))
          )
        }

    // Trigger node behavior
    val updatedNodes = nodesWithDeliveredMessages.map { (id, node) =>
      (id, node.postDeliveryAction(newTime))
    }

    // New messages to deliver
    val toDeliver: Iterable[Message[Scheduled]] = for {
      (_, node) <- updatedNodes
      message <- node.outgoingMessages
    } yield message.schedule(newTime + DeliveryLatency)

    // Clear delivered messages and add new messages
    val updatedMessages =
      messagesInTransit.withoutPastMessages(newTime).withMessages(toDeliver)

    Network(newTime, updatedNodes, updatedMessages, random)
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
