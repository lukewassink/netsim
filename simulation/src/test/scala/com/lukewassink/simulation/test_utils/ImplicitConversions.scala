package com.lukewassink.simulation.test_utils

import com.lukewassink.simulation.core.MessageID
import com.lukewassink.simulation.core.MessageID.MessageID
import com.lukewassink.simulation.util.{Duration, Time}
import com.lukewassink.simulation.core.NodeID.NodeID
import com.lukewassink.simulation.core.NodeID

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
}
