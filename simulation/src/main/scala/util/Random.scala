package util

// A purely functional random number generator that returns the random number and the next state of the generator.
trait Random {
  def next: (Int, Random)
}

// An implementation of Random that uses an XORShift transition function.
final case class XORRandom private (private val state: Long) extends Random {
  // Taken from the last implementation in this section:
  // https://en.wikipedia.org/wiki/Xorshift#Example_implementation
  private def nextState: Long =
    val x = state ^ (state << 7)
    x ^ (x >>> 9)

  def next: (Int, XORRandom) =
    (state.toInt, XORRandom(nextState))
}

object XORRandom {
  // Return an XORRandom generator seeded with the specified seed.
  def fromSeed(seed: Long): XORRandom = {
    // Warmup the generator with 4 iterations. This number was not based on research.
    val (_, r1) = XORRandom(seed).next
    val (_, r2) = r1.next
    val (_, r3) = r2.next
    val (_, r4) = r3.next
    r4
  }
}
