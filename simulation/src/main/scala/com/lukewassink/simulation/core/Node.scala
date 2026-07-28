package com.lukewassink.simulation.core

import com.lukewassink.simulation.core.MessageStage.{Pending, Scheduled}
import com.lukewassink.simulation.util.Time

// The fundamental abstraction of the simulation. It can send and receive messages in response to incoming
// messages and top its own state.
case class Node(
    behaviors: List[NodeBehavior],
    sharedState: NodeState
):

  // Updates the node before receiving new messages. Used for cleanup and initialization. No behaviors execute here.
  def preDeliveryAction(time: Time): Node = {
    // Clear sent messages from the last tick.
    Node(behaviors, sharedState.clearIncomingMessages)
  }

  // Updates the node based on delivered messages. Behaviors are triggered here.
  def postDeliveryAction(time: Time): Node =

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
  def outgoingMessages: List[Message[Pending]] =
    sharedState.outgoingMessages

  // Adds an incoming message.
  def withIncomingMessage(message: Message[Scheduled]): Node =
    Node(behaviors, sharedState.withIncomingMessage(message))
