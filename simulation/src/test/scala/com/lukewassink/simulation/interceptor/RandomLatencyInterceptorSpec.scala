package com.lukewassink.simulation.interceptor

import com.lukewassink.simulation.core.ExecutionContext
import com.lukewassink.simulation.test_utils.UnitSpec
import com.lukewassink.simulation.core.ExecutionContextSpec.{
  randChances, testContext
}
import com.lukewassink.simulation.test_utils.MessageSpecUtil.scheduledMessage
import com.lukewassink.simulation.util.{Chance, UniformDistribution}

class RandomLatencyInterceptorSpec extends UnitSpec {
  private val message = scheduledMessage(1, 2, "Hi")

  describe("intercept") {
    it("adds random latency") {
      given ExecutionContext = testContext(1, 1)
      val interceptor = RandomLatencyInterceptor(UniformDistribution(5, 10))

      interceptor.intercept(message) should contain theSameElementsAs
        List(scheduledMessage(1, 2 + randChances(0) * 5 + 5, "Hi"))
      interceptor.intercept(message) should contain theSameElementsAs
        List(scheduledMessage(1, 2 + randChances(1) * 5 + 5, "Hi"))
    }
  }
}
