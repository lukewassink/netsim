package com.lukewassink.simulation.util

import com.lukewassink.simulation.test_utils.UnitSpec

class TimeSpec extends UnitSpec {
  val half  = Time(0.5)
  val one   = Time(1)
  val two   = Time(2)
  val three = Time(3)

  val durTwo   = Duration(2)
  val durFive  = Duration(5)
  val durSeven = Duration(7)

  describe("Time") {
    it("allows non-integers") {
      assert(half !== one)
      assert(half != Time(0))
    }

    it("increments") {
      assert(one.next === two)
      assert(two.next === three)
    }

    it("compares using >") {
      assert(one > half)
      assert(!(one > one))
      assert(!(one > three))
    }

    it("compares using <") {
      assert(!(one < half))
      assert(!(one < one))
      assert(one < three)
    }

    it("compares using >=") {
      assert(one >= half)
      assert(one >= one)
      assert(!(one >= three))
    }

    it("compares using <=") {
      assert(!(one <= half))
      assert(one <= one)
      assert(one <= three)
    }

    it("adds a Duration to the time")(assert(one + durTwo === three))

    it("subtracts another time to get a Duration")(assert(three - one === durTwo))

    it("subtracts a duration to get another time")(assert(three - durTwo === one))
  }

  describe("Duration") {
    it("adds two durations")(assert(durFive + durTwo === durSeven))

    it("subtracts one duration from another") {
      assert(durSeven - durFive === durTwo)
    }

    it("calculates the ratio of two durations") {
      assert(durSeven / durFive === 1.4f)
    }
  }
}
