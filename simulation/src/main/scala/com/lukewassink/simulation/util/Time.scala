package com.lukewassink.simulation.util

// A class that represents moments of time, measured in ticks.
final case class Time(ticks: Int):

  // Tick forward to the next moment.
  def next: Time = Time(ticks + 1)

  infix def >(other: Time): Boolean = ticks > other.ticks

  infix def +(other: Duration): Time = Time(ticks + other.ticks)

  infix def -(other: Time): Duration = Duration(ticks - other.ticks)

// A class that represents the duration between two moments of time, measured in ticks.
final case class Duration(ticks: Int):

  infix def +(other: Duration): Duration = Duration(ticks + other.ticks)

  infix def -(other: Duration): Duration = Duration(ticks - other.ticks)

  // The ratio between two durations.
  infix def /(other: Duration): Float = ticks.toFloat / other.ticks.toFloat
