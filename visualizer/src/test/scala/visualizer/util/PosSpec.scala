package visualizer.util

import org.scalactic.Equality
import visualizer.test_util.UnitSpec
import visualizer.util.Pos.fromPolar
import visualizer.test_util.PosSpecUtil.given

class PosSpec extends UnitSpec {
  describe("interpolate") {
    it("returns this when t = 0") {
      Pos(1, 2).interpolate(0, Pos(3, 5)) shouldEqual Pos(1, 2)
    }

    it("returns the endpoint when t = 1") {
      Pos(1, 2).interpolate(1, Pos(3, 5)) shouldEqual Pos(3, 5)
    }

    it("interpolates to a point in the middle") {
      Pos(1.1, 2.3).interpolate(0.3, Pos(3, 5.7)) shouldEqual Pos(1.67, 3.32)
    }
  }

  describe("operation +") {
    it("adds positions") {
      Pos(1, 2) + Pos(3, 4) shouldEqual Pos(4, 6)
      Pos(-5, 2.3) + Pos(3, 4) shouldEqual Pos(-2, 6.3)
    }
  }

  describe("operation -") {
    it("subtracts positions") {
      Pos(1, 2) - Pos(3, 4) shouldEqual Pos(-2, -2)
      Pos(-5, 2.3) - Pos(3, 4) shouldEqual Pos(-8, -1.7)
    }
  }

  describe("operation *") {
    it("multiplies positions by scalars") {
      0 * Pos(1, 2) shouldEqual Pos(0, 0)
      5 * Pos(1, .5) shouldEqual Pos(5, 2.5)
      -2 * Pos(1, 2) shouldEqual Pos(-2, -4)
    }
  }

  describe("fromPolar") {
    it("returns the correct Cartesian coordinates given polar coordinates") {
      fromPolar(math.Pi / 2, 3).shouldEqual(Pos(0, 3))
      fromPolar(3 * math.Pi / 4, 2)
        .shouldEqual(Pos(-2 / math.sqrt(2), 2 / math.sqrt(2)))
    }
  }
}
