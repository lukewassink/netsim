package com.lukewassink.simulation.core

import com.lukewassink.simulation.core.ExecutionContextSpec.{
  randChances, testContext
}
import com.lukewassink.simulation.test_utils.UnitSpec
import com.lukewassink.simulation.util.{Chance, Duration, Time}

import scala.util.Random

object ExecutionContextSpec {
  // Use to guarantee the returned chances will match randChances.
  def testContext(time: Time, ticksPerMillisecond: Double): ExecutionContext =
    ExecutionContext(time, ticksPerMillisecond, 1)

  // First 10 values return by Random(1).nextDouble().
  private val rand                = Random(1)
  val randChances: Vector[Double] = (1 to 10).map(i => rand.nextDouble())
    .toVector
}

class ExecutionContextSpec extends UnitSpec {
  describe("convertTime") {
    it("converts time using the conversion factor") {
      assert(ExecutionContext(1, 1, 1).convertTime(10) === Duration(10))
      assert(ExecutionContext(1, 2.5, 1).convertTime(10) === Duration(25))
    }
  }

  describe("withNextTime") {
    it("increments the time by 1") {
      assert(ExecutionContext(1, 1, 1).withNextTime === ExecutionContext(2, 1, 1))
    }
  }

  describe("nextChance") {
    it("returns the next random value for the seed") {
      val context = testContext(1, 1)
      assert(context.nextChance === randChances(0))
      assert(context.nextChance === randChances(1))
    }
  }

  describe("chances") {
    it("returns the next random value for the seed") {
      val context = ExecutionContext(1, 1, 1)
      assert(!context.chances(Chance(0.5)))
      assert(context.chances(Chance(0.5)))
    }
  }
}
