package com.lukewassink.simulation.core

import com.lukewassink.simulation.behavior.{Behavior, UpdatedState}
import com.lukewassink.simulation.message.MessageStage.{Pending, Scheduled}
import com.lukewassink.simulation.core.NodeID.NodeID
import com.lukewassink.simulation.message.Message

// Represents a single device on the network.
// Can send and receive messages.
case class Node(behaviors: List[Behavior], sharedState: NodeState) {
  // Called before the network delivers new messages.
  def preDeliveryAction(using ctx: ExecutionContext): Node =
    // Clear incoming and outgoing messages from the last tick.
    Node(behaviors, sharedState.clearIncomingMessages.clearOutgoingMessages)

  // Updates the node after receiving messages before the main action
  def preAction(using ctx: ExecutionContext): Node = {
    // Update shared and behavior states by triggering behaviors in order.
    val (nextBehaviors, nextState) =
      behaviors.foldLeft((List.empty[Behavior], sharedState)) {
        case ((processedBehaviors, curState), behavior) =>
          val UpdatedState(nextB, nextS) = behavior.preAction(curState)
          (nextB :: processedBehaviors, nextS)
      }

    Node(nextBehaviors.reverse, nextState)
  }

  // Main action of the node.
  def mainAction(using ctx: ExecutionContext): Node = {
    // Update shared and behavior states by triggering behaviors in order.
    val (nextBehaviors, nextState) =
      behaviors.foldLeft((List.empty[Behavior], sharedState)) {
        case ((processedBehaviors, curState), behavior) =>
          val UpdatedState(nextB, nextS) = behavior.mainAction(curState)
          (nextB :: processedBehaviors, nextS)
      }

    Node(nextBehaviors.reverse, nextState)
  }

  // Updates the node after the main action,
  // before the network collects outgoing messages.
  def postAction(using ctx: ExecutionContext): Node = {
    // Update shared and behavior states by triggering behaviors in order.
    val (nextBehaviors, nextState) =
      behaviors.foldLeft((List.empty[Behavior], sharedState)) {
        case ((processedBehaviors, curState), behavior) =>
          val UpdatedState(nextB, nextS) = behavior.postAction(curState)
          (nextB :: processedBehaviors, nextS)
      }

    Node(nextBehaviors.reverse, nextState)
  }

  // Returns all outgoing messages.
  def outgoingMessages: List[Message[Pending]] = sharedState.outgoingMessages

  // Adds an incoming message.
  def withIncomingMessage(message: Message[Scheduled]): Node = Node(
    behaviors,
    sharedState.withIncomingMessage(message)
  )

  def id: NodeID = sharedState.header.id
}
