package com.lukewassink.simulation.core

import com.lukewassink.simulation.util.Chance.Chance
import com.lukewassink.simulation.util.Time.TimeConverter
import com.lukewassink.simulation.util.{
  BaseDistribution, Chance, Duration, LogEvent, Logger, Time
}

import scala.util.Random

object ExecutionContext:
  def apply(
      time: Time,
      ticksPerMillisecond: Double,
      seed: Long
  ): ExecutionContext =
    new ExecutionContext(time, ticksPerMillisecond, Random(seed), Logger())

// Shared context that should be made available implicitly throughout the network.
case class ExecutionContext(
    time: Time,
    ticksPerMillisecond: Double,
    private val rng: Random,
    logger: Logger
) extends TimeConverter, BaseDistribution:
  def convertTime(d: Double): Duration = Duration(ticksPerMillisecond * d)

  def withNextTime: ExecutionContext = this.copy(time = time.next)

  override def nextChance: Chance =
    val d = rng.nextDouble()
    if d == 0 then nextChance else Chance(d)

  // Returns true with probability chance, false otherwise.
  def chances(chance: Chance): Boolean = nextChance < chance

  override def equals(other: Any): Boolean =
    other match {
      case ExecutionContext(t, tpm, _, _) =>
        time == t && ticksPerMillisecond == tpm
      case _ => false
    }

  def log(event: LogEvent): Unit = logger.log(using this)(event)

  def tick: Int = time.tick
