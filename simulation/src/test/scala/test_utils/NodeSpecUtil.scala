package test_utils

import core.{Message, Node, NodeHeader, NodeState}
import test_utils.RandomSpecUtil.InertRandom

object NodeSpecUtil {
  // Generates a node state with an inert random number generator for tests that don't care about randomness.
  def testNodeState(
      header: NodeHeader,
      outgoingMessages: List[Message],
      incomingMessages: List[Message]
  ): NodeState =
    NodeState(header, outgoingMessages, incomingMessages, InertRandom())
}
