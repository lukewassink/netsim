package com.lukewassink.simulation.test_utils

import com.lukewassink.simulation.core.{Message, Node, NodeHeader, NodeState}
import RandomSpecUtil.InertRandom

object NodeSpecUtil {
  // Generates a node state with an inert random number generator for tests that don't care about randomness.
  def testNodeState(
      header: NodeHeader,
      outgoingMessages: List[Message],
      incomingMessages: List[Message]
  ): NodeState =
    NodeState(header, outgoingMessages, incomingMessages, InertRandom())
}
