package com.lukewassink.simulation.util

import com.lukewassink.simulation.core.NetworkExecutionContext

// A class that represents moments of time.
final case class Time(time: Double):

  // Tick forward to the next moment.
  def next: Time = Time(time + 1)

  infix def >(other: Time): Boolean = time > other.time

  // All defined in terms of >.
  infix def >=(other: Time): Boolean =
    (time > other.time) || (time == other.time)
  infix def <=(other: Time): Boolean = !(time > other.time)
  infix def <(other: Time): Boolean  =
    (time <= other.time) && (time != other.time)

  infix def +(duration: Duration): Time = Time(time + duration.time)

  infix def -(other: Time): Duration = Duration(time - other.time)

  infix def -(duration: Duration): Time = Time(time - duration.time)

case object Time:
  trait TimeConverter:
    def convertTime(millis: Double): Time

  extension (d: Double)
    def milliseconds(using converter: TimeConverter): Time = converter
      .convertTime(d)

  extension (l: Long)
    def milliseconds(using TimeConverter): Time = l.toDouble.milliseconds

// A class that represents the duration between two moments of time, measured in ticks.
final case class Duration(time: Double):

  infix def +(other: Duration): Duration = Duration(time + other.time)

  infix def -(other: Duration): Duration = Duration(time - other.time)

  // The ratio between two durations.
  infix def /(other: Duration): Float = time.toFloat / other.time.toFloat
