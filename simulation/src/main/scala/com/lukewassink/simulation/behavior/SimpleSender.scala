package com.lukewassink.simulation.behavior

import com.lukewassink.simulation.core.MessageStage.Drafted
import com.lukewassink.simulation.core.{
  Message, ExecutionContext, NodeBehavior, NodeState
}
import com.lukewassink.simulation.util.Time

case class SimpleSender(timeToSend: Time, message: Message[Drafted])
    extends NodeBehavior {
  override def updatedNodeState(using
      ctx: ExecutionContext
  )(state: NodeState): NodeState =
    if ctx.time == timeToSend then state.withOutgoingMessage(message) else state
}
