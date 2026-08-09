package com.lukewassink.simulation.test_utils

import com.lukewassink.simulation.core.{Message, Node, NodeHeader, NodeState}
import RandomSpecUtil.InertRandom
import com.lukewassink.simulation.core.MessageStage.{Pending, Scheduled}

object NodeStateSpecUtil {
  // Generates a node state with an inert random number generator for tests that don't care about randomness.
  def testNodeState(
      header: NodeHeader,
      outgoingMessages: List[Message[Pending]],
      incomingMessages: List[Message[Scheduled]]
  ): NodeState = NodeState(
    header,
    outgoingMessages,
    incomingMessages,
    InertRandom()
  )
}
