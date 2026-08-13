package com.lukewassink.simulation.util
import Chance.*

object Chance:
  // Encapsulates a Double that's required to be in (0, 1).
  opaque type Chance <: Double = Double

  def apply(value: Double): Chance =
    if value <= 0 || value >= 1 then
      throw IllegalArgumentException(
        s"$value is an illegal Chance. Chance values must be in (0, 1)."
      )
    else value

trait BaseDistribution:
  // Should return a pseudorandom Double uniformly distributed in (0, 1).
  // This is useful because we can continuously map (0, 1) -> {real line},
  // but this is not possible for closed intervals.
  def nextChance: Chance

trait Distribution:
  def next(using BaseDistribution): Double

case class UniformDistribution(min: Double, max: Double) extends Distribution:
  def next(using base: BaseDistribution): Double =
    val d = base.nextChance
    (max - min) * d + min

case class NormalDistribution(mean: Double, stDev: Double) extends Distribution:
  def next(using base: BaseDistribution): Double =
    // Use the Box-Muller transform. See https://en.wikipedia.org/wiki/Box%E2%80%93Muller_transform.
    val u1 = base.nextChance
    val u2 = base.nextChance
    val z  = math.sqrt(-2 * math.log(u1)) * math.cos(2 * math.Pi * u2)
    mean + stDev * z

case class LogNormalDistribution(logMean: Double, logStDev: Double)
    extends Distribution:
  def next(using base: BaseDistribution): Double =
    val normal = NormalDistribution(logMean, logStDev)
    math.exp(normal.next)
