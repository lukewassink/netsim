package com.lukewassink.simulation.behavior

import com.lukewassink.simulation.core.MessageStage.Drafted
import com.lukewassink.simulation.core.{Message, ExecutionContext, NodeState}
import com.lukewassink.simulation.util.Time

case class SimpleSender(timeToSend: Time, message: Message[Drafted])
    extends Behavior {
  override def updated(using
      ctx: ExecutionContext
  )(state: NodeState): UpdatedState =
    val nodeState =
      if ctx.time == timeToSend then state.withOutgoingMessage(message)
      else state
    UpdatedState(nodeState, this)
}
