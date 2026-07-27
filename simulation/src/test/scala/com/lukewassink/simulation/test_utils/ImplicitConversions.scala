package com.lukewassink.simulation.test_utils

import com.lukewassink.simulation.core.{MessageID, NodeID}

object ImplicitConversions {
  given Conversion[Int, NodeID] with
    def apply(id: Int): NodeID = NodeID(id)

  given Conversion[Int, MessageID] with
    def apply(id: Int): MessageID = MessageID(id)
}
