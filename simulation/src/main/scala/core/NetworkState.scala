package core

import util.Random

// The time it takes for a message to be delivered. Hardcoded for now. Later this should be changed to be config
// based.
final val DeliveryTime = 10

// The total state of the network. The network consists of nodes and the current time.
case class NetworkState(
    time: Int,
    nodes: Map[Int, Node],
    messagesInTransit: MessageQueue,
    random: Random
):

  // Logic:
  // 1) tick the time forward
  // 2) trigger pre-delivery node actions
  // 3) deliver messages
  // 4) trigger node behaviors
  // 5) collect outgoing messages from nodes
  def nextState(): NetworkState = {
    // Tick time
    val newTime = time + 1

    // Call node pre-delivery actions
    val initializedNodes = nodes.map(_ -> _.preDeliveryAction(newTime))

    // Deliver messages
    val nodesWithDeliveredMessages =
      messagesInTransit.currentMessages(newTime).foldLeft(initializedNodes) {
        (nodes, message) =>
          nodes.updatedWith(message.header.receiverId)(
            _.map(_.withIncomingMessage(message))
          )
      }

    // Trigger node behavior
    val updatedNodes = nodesWithDeliveredMessages.map { (id, node) =>
      (id, node.postDeliveryAction(newTime))
    }

    // New messages to deliver
    val toDeliver = for {
      (_, node) <- nodes
      message <- node.outgoingMessages
    } yield message.copy(header =
      message.header.copy(deliveryTime = Some(newTime + DeliveryTime))
    )

    // Clear delivered messages and add new messages
    val updatedMessages =
      messagesInTransit.withoutPastMessages(newTime).withMessages(toDeliver)

    NetworkState(newTime, updatedNodes, updatedMessages, random)
  }

object NetworkState {
  // A convenience method to initialize NetworkState using a list of nodes and list of messages.
  def apply(
      time: Int,
      nodes: List[Node],
      messages: List[Message],
      random: Random
  ): NetworkState = {
    val nodeMap: Map[Int, Node] = nodes.foldLeft(Map[Int, Node]()) {
      (map, node) =>
        map.updated(node.sharedState.header.id, node)
    }
    NetworkState(time, nodeMap, MessageQueue(messages), random)
  }
}
