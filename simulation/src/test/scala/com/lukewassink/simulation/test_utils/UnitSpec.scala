package com.lukewassink.simulation.test_utils

import org.scalatest.Inside
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should

trait UnitSpec
    extends AnyFunSpec
    with should.Matchers
    with MessageMatchers
    with Inside:
  export ImplicitConversions.given
