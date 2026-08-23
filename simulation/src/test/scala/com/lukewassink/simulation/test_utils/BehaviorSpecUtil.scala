package com.lukewassink.simulation.test_utils

import com.lukewassink.simulation.behavior.{Behavior, UpdatedState}
import com.lukewassink.simulation.message.MessageStage.Drafted
import com.lukewassink.simulation.core.{ExecutionContext, NodeState}
import com.lukewassink.simulation.message.Message

object BehaviorSpecUtil {

  // Sends the specified message.
  case class TestMessageMainActionOnlyBehavior(message: Message[Drafted])
      extends Behavior {
    override def preAction(using
        ctx: ExecutionContext
    )(state: NodeState): UpdatedState = UpdatedState(
      this,
      state.withOutgoingMessage(message)
    )
  }

  // Sends the specified message.
  case class TestMessageBehavior(message: Message[Drafted]) extends Behavior {
    override def preAction(using
        ctx: ExecutionContext
    )(state: NodeState): UpdatedState = UpdatedState(
      this,
      state.withOutgoingMessage(message)
    )

    override def mainAction(using
        ctx: ExecutionContext
    )(state: NodeState): UpdatedState = UpdatedState(
      this,
      state.withOutgoingMessage(message)
    )

    override def postAction(using
        ctx: ExecutionContext
    )(state: NodeState): UpdatedState = UpdatedState(
      this,
      state.withOutgoingMessage(message)
    )
  }

  // Increments its own state.
  case class TestSelfUpdateBehavior(selfState: Int) extends Behavior {
    override def preAction(using
        ctx: ExecutionContext
    )(state: NodeState): UpdatedState = UpdatedState(
      TestSelfUpdateBehavior(selfState + 1),
      state
    )

    override def mainAction(using
        ctx: ExecutionContext
    )(state: NodeState): UpdatedState = UpdatedState(
      TestSelfUpdateBehavior(selfState + 1),
      state
    )

    override def postAction(using
        ctx: ExecutionContext
    )(state: NodeState): UpdatedState = UpdatedState(
      TestSelfUpdateBehavior(selfState + 1),
      state
    )
  }
}
