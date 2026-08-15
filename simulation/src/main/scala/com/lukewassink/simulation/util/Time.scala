package com.lukewassink.simulation.util

// This class models time.
//
// The simulation evolves in discrete iterations. We call each such iteration a "tick".
// Thus, if the simulation has iterated 5 times, it's history will be 6 ticks long:
//
//    0, 1, 2, 3, 4, 5.
//
// Time stores a double, and increments by 1 each time. This means that the system time
// will always be a double representation of the number of ticks. This double representation
// will exactly match integer arithmatic as long as ticks <= 2^53.
//
// To calculate the first tick at which a given time will have been reached, we simply take
// ceil(time: Double) and convert it to an integer. For the system time, which should always
// be a double representation of an integer anyway, no information will be lost. This
// conversion is provided by the Time.tick method.
final case class Time(time: Double):
  // Tick forward to the next moment.
  //
  // DO NOT change this from incrementing by 1.
  // This could lead to floating point errors when rounding up to the nearest tick.
  // In any case, such a change should never be necessary because the simulation
  // can set times in milliseconds, and the conversion from milliseconds to time
  // is configurable.
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

  // The first tick at which the time will have been reached.
  def tick: Int = math.ceil(time).toInt

case object Time:
  trait TimeConverter:
    def convertTime(millis: Double): Duration

  extension (d: Double)
    def milliseconds(using converter: TimeConverter): Duration = converter
      .convertTime(d)

  extension (l: Long)
    def milliseconds(using TimeConverter): Duration = l.toDouble.milliseconds

// A class that represents the duration between two moments of time, measured in ticks.
final case class Duration(time: Double):

  infix def +(other: Duration): Duration = Duration(time + other.time)

  infix def -(other: Duration): Duration = Duration(time - other.time)

  // The ratio between two durations.
  infix def /(other: Duration): Float = time.toFloat / other.time.toFloat
