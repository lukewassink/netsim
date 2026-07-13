package test_utils

import core.{Message, NodeBehavior, NodeState}

object BehaviorSpecUtil {
  case class TestBehavior(message: Message) extends NodeBehavior {
    override def trigger(
        time: Int,
        state: NodeState,
        deliveredMessages: List[Message]
    ): NodeState =
      state.withOutgoingMessage(time, message)
  }
}
