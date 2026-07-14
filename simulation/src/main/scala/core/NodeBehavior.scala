package core

// A data class that encapsulates the return type of the NodeBehavior.updated() method, which can update shared state,
// and/or itself.
final case class UpdatedState(sharedState: NodeState, selfState: NodeBehavior)

// The fundamental unit of behavior for a node. In response to current node state and incoming messages, it can update
// its own state and shared state, including delivering messages.
trait NodeBehavior:

  // Overwrite if a behavior wants to update the shared state.
  protected def updatedNodeState(
      time: Int,
      sharedState: NodeState,
      deliveredMessages: List[Message]
  ): NodeState = sharedState

  // Overwrite if a behavior wants to update its own state.
  protected def updatedSelfState(
      time: Int,
      sharedState: NodeState,
      deliveredMessages: List[Message]
  ): NodeBehavior = this

  final def updated(
      time: Int,
      sharedState: NodeState,
      deliveredMessages: List[Message]
  ): UpdatedState =
    UpdatedState(
      updatedNodeState(time, sharedState, deliveredMessages),
      updatedSelfState(time, sharedState, deliveredMessages)
    )
