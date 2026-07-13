package behavior

import core.{Message, NodeBehavior, NodeState}

case class SimpleSender(timeToSend: Int, message: Message)
    extends NodeBehavior {
  def trigger(
      time: Int,
      state: NodeState,
      deliveredMessages: List[Message]
  ): NodeState =
    if time == timeToSend then state.withOutgoingMessage(time, message)
    else state
}
