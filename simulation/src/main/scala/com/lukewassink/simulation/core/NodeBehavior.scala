package com.lukewassink.simulation.core

import com.lukewassink.simulation.util.Time

// A data class that encapsulates the return type of the NodeBehavior.updated() method, which can update shared state,
// and/or itself.
final case class UpdatedState(sharedState: NodeState, selfState: NodeBehavior)

// The fundamental unit of behavior for a node. In response to current node state and incoming messages, it can update
// its own state and shared state, including delivering messages.
trait NodeBehavior:

  // Overwrite if a behavior wants to update the shared state.
  protected def updatedNodeState(
      time: Time,
      sharedState: NodeState
  ): NodeState = sharedState

  // Overwrite if a behavior wants to update its own state.
  protected def updatedSelfState(
      time: Time,
      sharedState: NodeState
  ): NodeBehavior = this

  final def updated(
      time: Time,
      sharedState: NodeState
  ): UpdatedState =
    UpdatedState(
      updatedNodeState(time, sharedState),
      updatedSelfState(time, sharedState)
    )
