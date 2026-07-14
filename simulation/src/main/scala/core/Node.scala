package core

import util.Random

// Metadata for a node.
case class NodeHeader(id: Int, nextMessageId: Int)

// The shared internal state of the node. It contains incoming messages and any shared node history or data required by
// the behaviors. Individual behaviors can also store their own state.
case class NodeState(
    header: NodeHeader,
    outgoingMessages: List[Message],
    random: Random
):
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
    sharedState: NodeState,
    incomingMessages: MessageQueue
):

  // Returns the node with updated shared and behavior state, including outgoing messages.
  def nextNode(time: Int): Node =

    // Clear messages that were sent last tick.
    val clearedState = sharedState.clearOutgoingMessages

    // Update shared and behavior states by triggering behaviors in order.
    val (nextState, nextBehaviors) =
      behaviors.foldLeft((clearedState, List[NodeBehavior]())) {
        case ((curState, processedBehaviors), behavior) =>
          val UpdatedState(nextS, nextB) = behavior.updated(
            time,
            curState,
            incomingMessages.currentMessages(time)
          )
          (nextS, nextB :: processedBehaviors)
      }

    // Clear delivered messages now that they have been processed.
    val updatedMessages = incomingMessages.withoutPastMessages(time)

    // Reverse nextBehaviors because triggering the behaviors reverses it.
    Node(nextBehaviors.reverse, nextState, updatedMessages)

  // Returns all outgoing messages.
  def outgoingMessages: List[Message] =
    sharedState.outgoingMessages

  // Adds an incoming messages.
  def withIncomingMessage(message: Message): Node =
    Node(behaviors, sharedState, incomingMessages.withMessage(message))
