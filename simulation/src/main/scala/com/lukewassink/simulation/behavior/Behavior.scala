package com.lukewassink.simulation.behavior

import com.lukewassink.simulation.core.{ExecutionContext, NodeState}

// A data class that encapsulates the return type of the NodeBehavior.updated() method, which can update shared state,
// and/or itself.
final case class UpdatedState(sharedState: NodeState, selfState: Behavior)

// Plan:
// 1. get rid of updatedNodeState and updatedSelfState. Behaviors can just updated the whole thing
// 2. Three methods: preReceiveAction, mainAction, preSendAction. Just override the ones you want
// 3. Nodes should have three actions: preReceiveAction, mainAction, preSendAction.
// 4. Trigger all three node actions from the network.

// The fundamental unit of behavior for a node. In response to current node state and incoming messages, it can update
// its own state and shared state, including delivering messages.
trait Behavior {
  def updated(using
      ctx: ExecutionContext
  )(sharedState: NodeState): UpdatedState = UpdatedState(sharedState, this)
}
