package core

// Metadata for a node.
case class NodeHeader(id: Int, nextMessageId: Int)

// The share internal state of the node. It contains incoming messages and any shared node history or data required by
// the behaviors. Individual behaviors can also store their own state.
case class NodeState(header: NodeHeader, outgoingMessages: List[Message]):
  def clearOutgoingMessages: NodeState =
    copy(outgoingMessages = List.empty)

  def withOutgoingMessage(time: Int, message: Message): NodeState = {
    // Automatically set message ID, sender ID, and send time for outgoing messages.
    val messageWithNodeMetadata = message.copy(header =
      message.header.copy(
        messageId = header.nextMessageId,
        senderId = header.id,
        sendTime = time
      )
    )

    copy(
      outgoingMessages = messageWithNodeMetadata :: outgoingMessages,
      header = header.copy(nextMessageId = header.nextMessageId + 1)
    )
  }

// The fundamental abstraction of the simulation. It can send and receive messages in response to incoming
// messages and top its own state.
case class Node(
    behaviors: List[NodeBehavior],
    state: NodeState,
    incomingMessages: MessageQueue
):

  // Returns the node with updated state including outgoing messages.
  def nextNode(time: Int): Node =
    val clearedState = state.clearOutgoingMessages
    val updatedState = behaviors.foldLeft(clearedState)((nextState, behavior) =>
      behavior.trigger(time, nextState, incomingMessages.currentMessages(time))
    )
    val updatedMessages = incomingMessages.withoutDeliveredMessages(time)

    Node(behaviors, updatedState, updatedMessages)

  // Returns all outgoing messages.
  def outgoingMessages: List[Message] =
    state.outgoingMessages

  // Adds an incoming messages.
  def withIncomingMessage(message: Message): Node =
    Node(behaviors, state, incomingMessages.withMessage(message))
