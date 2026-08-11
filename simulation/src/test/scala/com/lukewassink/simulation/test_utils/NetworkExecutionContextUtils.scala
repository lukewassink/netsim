package com.lukewassink.simulation.test_utils

import com.lukewassink.simulation.core.NetworkExecutionContext
import com.lukewassink.simulation.util.Time

object NetworkExecutionContextUtils {
  def testContext(time: Time): NetworkExecutionContext = NetworkExecutionContext(
    time,
    1
  )
}
