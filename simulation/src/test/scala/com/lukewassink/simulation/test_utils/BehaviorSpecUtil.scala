package com.lukewassink.simulation.test_utils

import com.lukewassink.simulation.behavior.{Behavior, UpdatedState}
import com.lukewassink.simulation.core.MessageStage.Drafted
import com.lukewassink.simulation.core.{ExecutionContext, Message, NodeState}

object BehaviorSpecUtil {

  // Sends the specified message.
  case class TestMessageBehavior(message: Message[Drafted]) extends Behavior {
    override def updated(using
        ctx: ExecutionContext
    )(state: NodeState): UpdatedState = UpdatedState(
      state.withOutgoingMessage(message),
      this
    )
  }

  // Increments its own state.
  case class TestSelfUpdateBehavior(selfState: Int) extends Behavior {
    override def updated(using
        ctx: ExecutionContext
    )(state: NodeState): UpdatedState = UpdatedState(
      state,
      TestSelfUpdateBehavior(selfState + 1)
    )
  }
}
