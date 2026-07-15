package behavior

import core.{Message, NodeBehavior, NodeState}

case class SimpleSender(timeToSend: Int, message: Message)
    extends NodeBehavior {
  override def updatedNodeState(
      time: Int,
      state: NodeState
  ): NodeState =
    if time == timeToSend then state.withOutgoingMessage(time, message)
    else state
}
