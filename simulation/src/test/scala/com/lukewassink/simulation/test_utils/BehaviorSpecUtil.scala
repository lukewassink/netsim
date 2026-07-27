package com.lukewassink.simulation.test_utils

import com.lukewassink.simulation.core.{Message, NodeBehavior, NodeState}
import com.lukewassink.simulation.util.Time

object BehaviorSpecUtil {

  // Sends the specified message.
  case class TestMessageBehavior(message: Message) extends NodeBehavior {
    override def updatedNodeState(
        time: Time,
        state: NodeState
    ): NodeState =
      state.withOutgoingMessage(time, message)
  }

  // Increments its own state.
  case class TestSelfUpdateBehavior(selfState: Int) extends NodeBehavior {
    override def updatedSelfState(
        time: Time,
        state: NodeState
    ): NodeBehavior =
      TestSelfUpdateBehavior(selfState + 1)
  }
}
