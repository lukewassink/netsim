package core

// The total state of the network. The network consists of nodes and the current time.
case class NetworkState(time: Int, nodes: Map[Int, Node]):

  // Logic:
  // 1) deliver any outgoing messages
  // 2) tick the time forward
  // 3) trigger node behaviors
  def nextState(): NetworkState = {
    val toDeliver = for {
      (_, node) <- nodes
      message <- node.outgoingMessages
    } yield message

    println()
    println("To Deliver:")
    println(toDeliver)

    val nodesWithDeliveredMessages = toDeliver.foldLeft(nodes) { (nodes, message) =>
      nodes.updatedWith(message.header.receiverId)(_.map(_.withIncomingMessage(message)))
    }
    
    val nextTime = time + 1

    val updatedNodes = nodesWithDeliveredMessages.map { (id, node) => (id, node.nextNode(nextTime)) }
    
    NetworkState(nextTime, updatedNodes)
  }
