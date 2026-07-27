package com.lukewassink.simulation.test_utils

import com.lukewassink.simulation.core.NodeID

object ImplicitConversions {
  given Conversion[Int, NodeID] with
    def apply(id: Int): NodeID = NodeID(id)
}
