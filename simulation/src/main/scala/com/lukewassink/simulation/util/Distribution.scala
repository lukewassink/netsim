package com.lukewassink.simulation.util

trait BaseDistribution:
  // Should return a pseudorandom number uniformly distributed in [0, 1).
  def nextDouble: Double

  // Should return a pseudorandom number uniformly distributed in (0, 1).
  // This is useful because we can continuously map (0, 1) -> {real line},
  // but this is not possible for closed intervals.
  def openNextDouble: Double

trait Distribution:
  def next(using BaseDistribution): Double

case class UniformDistribution(min: Double, max: Double):
  def next(using base: BaseDistribution): Double =
    val d = base.nextDouble
    (max - min) * d + min

case class NormalDistribution(mean: Double, stDev: Double):
  def next(using base: BaseDistribution): Double =
    // Use the Box-Muller transform. See https://en.wikipedia.org/wiki/Box%E2%80%93Muller_transform.
    val u1 = base.openNextDouble
    val u2 = base.openNextDouble
    val z  = math.sqrt(-2 * math.log(u1)) * math.cos(2 * math.Pi * u2)
    mean + stDev * z

case class LogNormalDistribution(logMean: Double, logStDev: Double):
  def next(using base: BaseDistribution): Double =
    val normal = NormalDistribution(logMean, logStDev)
    math.exp(normal.next)
