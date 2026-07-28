package com.lukewassink.simulation.test_utils

import com.lukewassink.simulation.core.{Message, Network, Node}
import RandomSpecUtil.InertRandom
import com.lukewassink.simulation.core.MessageStage.Scheduled
import com.lukewassink.simulation.util.Time

object NetworkSpecUtil {
  // Generates a network state with an inert random number generator for tests that don't care about randomness.
  def testNetwork(
      time: Time,
      nodes: List[Node],
      messages: List[Message[Scheduled]]
  ): Network =
    Network(time, nodes, messages, InertRandom())
}
