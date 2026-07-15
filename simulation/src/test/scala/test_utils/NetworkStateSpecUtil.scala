package test_utils

import core.{Message, NetworkState, Node}
import test_utils.RandomSpecUtil.InertRandom

object NetworkStateSpecUtil {
  // Generates a network state with an inert random number generator for tests that don't care about randomness.
  def testNetworkState(
      time: Int,
      nodes: List[Node],
      messages: List[Message]
  ): NetworkState =
    NetworkState(time, nodes, messages, InertRandom())
}
