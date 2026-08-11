package com.lukewassink.simulation.test_utils

import com.lukewassink.simulation.core.ExecutionContext
import com.lukewassink.simulation.util.Time

object ExecutionContextUtils {
  def testContext(time: Time): ExecutionContext = ExecutionContext(time, 1, 1)
}
