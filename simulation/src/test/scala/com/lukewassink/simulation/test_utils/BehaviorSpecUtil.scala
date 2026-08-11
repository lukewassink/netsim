package com.lukewassink.simulation.test_utils

import com.lukewassink.simulation.core.MessageStage.Drafted
import com.lukewassink.simulation.core.{
  Message, ExecutionContext, NodeBehavior, NodeState
}
import com.lukewassink.simulation.test_utils.ExecutionContextUtils.testContext
import com.lukewassink.simulation.util.Time

object BehaviorSpecUtil {

  // Sends the specified message.
  case class TestMessageBehavior(message: Message[Drafted]) extends NodeBehavior {
    override def updatedNodeState(using
        ctx: ExecutionContext
    )(state: NodeState): NodeState = state.withOutgoingMessage(message)
  }

  // Increments its own state.
  case class TestSelfUpdateBehavior(selfState: Int) extends NodeBehavior {
    override def updatedSelfState(using
        ctx: ExecutionContext
    )(state: NodeState): NodeBehavior = TestSelfUpdateBehavior(selfState + 1)
  }
}
