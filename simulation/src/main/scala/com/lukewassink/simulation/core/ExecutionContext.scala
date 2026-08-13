package com.lukewassink.simulation.core

import com.lukewassink.simulation.util.Chance.Chance
import com.lukewassink.simulation.util.Time.TimeConverter
import com.lukewassink.simulation.util.{BaseDistribution, Chance, Duration, Time}

import scala.util.Random

// Shared context that should be made available implicitly throughout the network.
case class ExecutionContext(
    time: Time,
    ticksPerMillisecond: Double,
    randomSeed: Long
) extends TimeConverter, BaseDistribution:
  private val rng: Random = Random(randomSeed)

  def convertTime(d: Double): Duration = Duration(ticksPerMillisecond * d)

  def withNextTime: ExecutionContext = this.copy(time = time.next)

  override def nextChance: Chance =
    val d = rng.nextDouble()
    if d == 0 then nextChance else Chance(d)

  // Returns true with probability chance, false otherwise.
  def chances(chance: Chance): Boolean = nextChance < chance
