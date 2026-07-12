package core

// The fundamental unit of behavior for a node. In response to current node state and incoming messages,
// it can update state and deliver messages.
trait NodeBehavior:
  def trigger(time: Int, state: NodeState, deliveredMessages: List[Message]): NodeState
