package visualizer.util

import scala.math.Numeric.Implicits.infixNumericOps

// Represents a position in Cartesian (x, y) coordinates.
case class Pos(x: Float, y: Float) {
  // Interpolate a point between the current point and endPoint.
  // t = 0 => this point
  // t = 1 => endPoint
  def interpolate[T: Numeric](scalar: T, endPoint: Pos): Pos = {
    val Pos(x2, y2) = endPoint
    val t = scalar.toFloat
    Pos((1 - t) * x + t * x2, (1 - t) * y + t * y2)
  }

  infix def +(other: Pos): Pos = Pos(x + other.x, y + other.y)

  infix def -(other: Pos): Pos = this + (-1 * other)
}

object Pos {
  // Allow scalar multiplication.
  extension [T: Numeric](scalar: T)
    infix def *(pos: Pos): Pos = {
      val s = scalar.toFloat
      Pos(s * pos.x, s * pos.y)
    }

  infix def -(pos: Pos): Pos = -1 * pos

  // Returns the coordinates of a point rotated by angle around center with radius.
  def fromPolar[S: Numeric, T: Numeric](angle: S, radius: T): Pos = {
    val a = angle.toDouble
    val r = radius.toFloat
    val x = Math.cos(a) * r
    val y = Math.sin(a) * r
    Pos(x, y)
  }

  def apply[S: Numeric, T: Numeric](x: S, y: T): Pos =
    Pos(x.toFloat, y.toFloat)
}
