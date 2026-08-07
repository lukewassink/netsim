package com.lukewassink.runner.util

import com.lukewassink.simulation.test_utils.UnitSpec

class ResultSpec extends UnitSpec {
  describe("flatMap") {
    it("maps a Success") {
      Success(10).flatMap(i => Success(2 * i)) shouldEqual Success(20)
    }

    it("doesn't affect a Failure") {
      val result = Failure[Int](RuntimeException("Test message"))
        .flatMap(i => Success(2 * i))

      inside(result) { case Failure(errors: List[Throwable]) =>
        errors should have length 1
        assert(errors.head.isInstanceOf[RuntimeException])
        errors.head.getMessage shouldEqual "Test message"
      }
    }
  }
}
