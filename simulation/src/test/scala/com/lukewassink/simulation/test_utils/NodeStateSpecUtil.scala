package com.lukewassink.simulation.test_utils

import com.lukewassink.simulation.core.{Node, NodeHeader, NodeState}
import RandomSpecUtil.InertRandom
import com.lukewassink.simulation.message.MessageStage.{Pending, Scheduled}
import com.lukewassink.simulation.message.Message

object NodeStateSpecUtil {
  // Generates a node state with an inert random number generator for tests that don't care about randomness.
  def testNodeState(
      header: NodeHeader,
      outgoingMessages: List[Message[Pending]],
      incomingMessages: List[Message[Scheduled]]
  ): NodeState = NodeState(header, outgoingMessages, incomingMessages)
}
