package test_utils

import util.Random

object RandomSpecUtil {
  // A mock random number generator that always returns 0. For tests that don't care about randomness.
  final case class InertRandom() extends Random {
    def next: (Int, InertRandom) = (0, this)
  }

  // A mock random number generator that simply returns the numbers in the supplied list. It will error if it runs out
  // of numbers.
  final case class MockRandom(numbersToGenerate: List[Int]) extends Random {
    def next: (Int, MockRandom) =
      (numbersToGenerate.head, MockRandom(numbersToGenerate.tail))
  }
}
