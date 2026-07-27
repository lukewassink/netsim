package com.lukewassink.simulation.test_utils

import com.lukewassink.simulation.core.{Message, NetworkState, Node}
import RandomSpecUtil.InertRandom
import com.lukewassink.simulation.util.Time

object NetworkStateSpecUtil {
  // Generates a network state with an inert random number generator for tests that don't care about randomness.
  def testNetworkState(
      time: Time,
      nodes: List[Node],
      messages: List[Message]
  ): NetworkState =
    NetworkState(time, nodes, messages, InertRandom())
}
