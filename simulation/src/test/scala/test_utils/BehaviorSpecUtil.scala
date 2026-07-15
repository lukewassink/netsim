package test_utils

import core.{Message, NodeBehavior, NodeState}

object BehaviorSpecUtil {

  // Sends the specified message.
  case class TestMessageBehavior(message: Message) extends NodeBehavior {
    override def updatedNodeState(
        time: Int,
        state: NodeState
    ): NodeState =
      state.withOutgoingMessage(time, message)
  }

  // Increments its own state.
  case class TestSelfUpdateBehavior(selfState: Int) extends NodeBehavior {
    override def updatedSelfState(
        time: Int,
        state: NodeState
    ): NodeBehavior =
      TestSelfUpdateBehavior(selfState + 1)
  }
}
