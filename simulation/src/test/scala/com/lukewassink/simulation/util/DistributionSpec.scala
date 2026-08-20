package com.lukewassink.simulation.util

import com.lukewassink.simulation.test_utils.UnitSpec
import com.lukewassink.simulation.util.Chance.*

import scala.util.Random

class TestBaseDistribution(chances: Vector[Chance]) extends BaseDistribution:
  private var i = 0

  override def nextChance: Chance = {
    val chance = chances(i)
    i += 1
    chance
  }

def uniformChances(n: Int): Vector[Chance] = (1 to n).map(_.toDouble / (n + 1))
  .map(Chance(_)).toVector

def uniformSample[A](n: Int, distribution: Distribution[A]): Vector[A] =
  given TestBaseDistribution(uniformChances(n))
  (for _ <- 1 to n yield distribution.next).toVector

class DistributionSpec extends UnitSpec {
  describe("UniformDistribution") {
    it("picks values uniformly in its range") {
      for n <- 1 to 50 do
        assert(uniformSample(n, UniformDistribution(0, 1)) == uniformChances(n))

      assert(
        uniformSample(5, UniformDistribution(10, 16)) ==
          Vector(11, 12, 13, 14, 15)
      )
    }
  }

  describe("Chance") {
    it("errors when the value is out of bounds")(
      an[IllegalArgumentException] shouldBe thrownBy(Chance(0))
    )

    it("exposes Double comparison methods")(assert(Chance(0.5) < Chance(0.7)))
  }
}
