package com.lukewassink.simulation.core

import com.lukewassink.simulation.core.MessageStage.Scheduled
import com.lukewassink.simulation.util.Time.TimeConverter
import com.lukewassink.simulation.util.{Duration, Random, Time}

// Shared context that should be made available implicitly throughout the network.
case class NetworkExecutionContext(time: Time, ticksPerMillisecond: Double)
    extends TimeConverter:
  def convertTime(d: Double): Time = Time(ticksPerMillisecond * d)

// The time it takes for a message to be delivered. Hardcoded for now. Later this should be changed to be config
// based.
final val DeliveryLatency = Duration(10)

// The total state of the network. The network consists of nodes and the current time.
case class Network(
    time: Time,
    nodes: Map[NodeID, Node],
    messagesInTransit: DeliveryQueue,
    random: Random,
    ticksPerMillisecond: Double
) {

  // Logic:
  // 1) tick the time forward
  // 2) trigger pre-delivery node actions
  // 3) deliver messages
  // 4) trigger node behaviors
  // 5) collect outgoing messages from nodes
  def next: Network = {
    // Tick time
    val newTime = time.next

    given NetworkExecutionContext = NetworkExecutionContext(
      newTime,
      ticksPerMillisecond
    )

    // Call node pre-delivery actions
    val initializedNodes = nodes.map(_ -> _.preDeliveryAction)

    // Deliver messages
    val nodesWithDeliveredMessages =
      messagesInTransit.deliverableMessages(newTime)
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
      } yield message.schedule(newTime + DeliveryLatency)

    // Clear delivered messages and add new messages
    val updatedMessages = messagesInTransit.withoutPastMessages(newTime)
      .withMessages(toDeliver)

    Network(newTime, updatedNodes, updatedMessages, random, ticksPerMillisecond)
  }
}

object Network {
  // A convenience method to initialize Network using a list of nodes and list of messages.
  def apply(
      time: Time,
      nodes: List[Node],
      messages: List[Message[Scheduled]],
      random: Random,
      ticksPerMillisecond: Double
  ): Network = {
    val nodeMap: Map[NodeID, Node] =
      nodes.foldLeft(Map[NodeID, Node]()) { (map, node) =>
        map.updated(node.sharedState.header.id, node)
      }
    Network(time, nodeMap, DeliveryQueue(messages), random, ticksPerMillisecond)
  }

  def apply(
      time: Time,
      nodes: List[Node],
      messages: List[Message[Scheduled]],
      random: Random
  ): Network = Network(time, nodes, messages, random, 1)
}
