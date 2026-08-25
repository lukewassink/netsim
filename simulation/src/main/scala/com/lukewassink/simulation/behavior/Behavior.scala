package com.lukewassink.simulation.behavior

import com.lukewassink.simulation.core.{ExecutionContext, NodeState}

final case class UpdatedState(selfState: Behavior, sharedState: NodeState)

// Equip Behaviors to Nodes to give them custom logic.
trait Behavior {
  // Called after messages are delivered to the node.
  // Behaviors that want to intercept or preprocess messages before the main action
  // can overload this method.
  def preAction(using
      ctx: ExecutionContext
  )(sharedState: NodeState): UpdatedState = UpdatedState(this, sharedState)

  def mainAction(using
      ctx: ExecutionContext
  )(sharedState: NodeState): UpdatedState = UpdatedState(this, sharedState)

  // Called before outgoing messages are collected by the network.
  // Behaviors that want to intercept or preprocess messages before the network
  // collects them for delivery can override this method.
  def postAction(using
      ctx: ExecutionContext
  )(sharedState: NodeState): UpdatedState = UpdatedState(this, sharedState)
}
