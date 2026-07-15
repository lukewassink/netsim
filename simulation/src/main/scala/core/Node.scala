package core

import util.Random

// Metadata for a node.
case class NodeHeader(id: Int, nextMessageId: Int)

// The shared internal state of the node. It contains incoming messages and any shared node history or data required by
// the behaviors. Individual behaviors can also store their own state.
case class NodeState(
    header: NodeHeader,
    outgoingMessages: List[Message],
    incomingMessages: List[Message],
    random: Random
):
  def clearOutgoingMessages: NodeState =
    copy(outgoingMessages = List.empty)

  def clearIncomingMessages: NodeState =
    copy(incomingMessages = List.empty)

  // Sets the message ID, sender ID, and send time for outgoing messages and adds it to the list.
  def withOutgoingMessage(time: Int, message: Message): NodeState = {
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

  def withIncomingMessage(message: Message): NodeState =
    copy(incomingMessages = message :: incomingMessages)

// The fundamental abstraction of the simulation. It can send and receive messages in response to incoming
// messages and top its own state.
case class Node(
    behaviors: List[NodeBehavior],
    sharedState: NodeState
):

  // Updates the node before receiving new messages. Used for cleanup and initialization. No behaviors execute here.
  def preDeliveryAction(time: Int): Node = {
    // Clear sent messages from the last tick.
    Node(behaviors, sharedState.clearIncomingMessages)
  }

  // Updates the node based on delivered messages. Behaviors are triggered here.
  def postDeliveryAction(time: Int): Node =

    // Clear messages that were sent last tick.
    val clearedState = sharedState.clearOutgoingMessages

    // Update shared and behavior states by triggering behaviors in order.
    val (nextState, nextBehaviors) =
      behaviors.foldLeft((clearedState, List[NodeBehavior]())) {
        case ((curState, processedBehaviors), behavior) =>
          val UpdatedState(nextS, nextB) = behavior.updated(
            time,
            curState
          )
          (nextS, nextB :: processedBehaviors)
      }

    // Reverse nextBehaviors because triggering the behaviors reverses it, and clear incoming messages now that they
    // have been read.
    Node(nextBehaviors.reverse, nextState)

  // Returns all outgoing messages.
  def outgoingMessages: List[Message] =
    sharedState.outgoingMessages

  // Adds an incoming message.
  def withIncomingMessage(message: Message): Node =
    Node(behaviors, sharedState.withIncomingMessage(message))
