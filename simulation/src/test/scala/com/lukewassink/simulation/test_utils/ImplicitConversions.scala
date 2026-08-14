package com.lukewassink.simulation.test_utils

import com.lukewassink.simulation.core.MessageID
import com.lukewassink.simulation.core.MessageID.MessageID
import com.lukewassink.simulation.util.{Chance, Duration, Time}
import com.lukewassink.simulation.core.NodeID.NodeID
import com.lukewassink.simulation.core.NodeID
import com.lukewassink.simulation.util.Chance.Chance

// To simplify tests. These allow us to write:
//
//   NodeHeader(1, 3)
//
// instead of:
//
//   NodeHeader(NodeID(1), MessageID(3))
//
// in test files. Production code wants clarity and type-safety,
// but tests want to be concise and easy to write.
object ImplicitConversions {
  given Conversion[Int, NodeID] with
    def apply(id: Int): NodeID = NodeID(id)

  given Conversion[Int, MessageID] with
    def apply(id: Int): MessageID = MessageID(id)

  given Conversion[Int, Time] with
    def apply(ticks: Int): Time = Time(ticks)

  given Conversion[Double, Time] with
    def apply(ticks: Double): Time = Time(ticks)

  given Conversion[Double, Duration] with
    def apply(ticks: Double): Duration = Duration(ticks)

  given Conversion[Double, Chance] with
    def apply(chance: Double): Chance = Chance(chance)
}
