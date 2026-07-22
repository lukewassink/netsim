package visualizer.test_util

import org.scalactic.Equality
import org.scalactic.Tolerance.convertNumericToPlusOrMinusWrapper
import org.scalatest.Assertions.convertToEqualizer
import visualizer.util.Pos

object PosSpecUtil {
  given Equality[Pos] with {
    override def areEqual(a: Pos, b: Any): Boolean =
      b match {
        case Pos(x, y) => a.x === x +- 0.02 && a.y === y +- 0.02
        case _         => false
      }
  }
}
