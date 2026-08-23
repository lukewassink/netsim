package com.lukewassink.simulation.behavior

import com.lukewassink.simulation.message.MessageStage.Drafted
import com.lukewassink.simulation.core.{ExecutionContext, NodeState}
import com.lukewassink.simulation.message.Message
import com.lukewassink.simulation.util.Time

case class SimpleSender(timeToSend: Time, message: Message[Drafted])
    extends Behavior {
  override def mainAction(using
      ctx: ExecutionContext
  )(state: NodeState): UpdatedState =
    val nodeState =
      if ctx.time == timeToSend then state.withOutgoingMessage(message)
      else state
    UpdatedState(this, nodeState)
}
