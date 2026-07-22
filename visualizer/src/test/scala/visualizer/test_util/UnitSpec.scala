package visualizer.test_util

import org.scalactic.Tolerance
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should

trait UnitSpec extends AnyFunSpec with should.Matchers with Tolerance
