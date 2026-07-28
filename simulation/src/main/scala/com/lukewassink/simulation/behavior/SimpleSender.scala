package com.lukewassink.simulation.behavior

import com.lukewassink.simulation.core.MessageStage.Drafted
import com.lukewassink.simulation.core.{Message, NodeBehavior, NodeState}
import com.lukewassink.simulation.util.Time

case class SimpleSender(timeToSend: Time, message: Message[Drafted])
    extends NodeBehavior {
  override def updatedNodeState(
      time: Time,
      state: NodeState
  ): NodeState =
    if time == timeToSend then state.withOutgoingMessage(time, message)
    else state
}
