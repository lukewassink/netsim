package com.lukewassink.simulation.interceptor

import com.lukewassink.simulation.core.ExecutionContext
import com.lukewassink.simulation.test_utils.UnitSpec
import com.lukewassink.simulation.core.ExecutionContextSpec.{
  randChances, testContext
}
import com.lukewassink.simulation.test_utils.MessageSpecUtil.scheduledMessage
import com.lukewassink.simulation.util.Chance

class MessageDropInterceptorSpec extends UnitSpec {
  private val message = scheduledMessage(1, 2, "Hi")

  describe("intercept") {
    it("drops the message if chances() == true") {
      given ExecutionContext = testContext(1, 1)
      assert(
        MessageDropInterceptor(Chance((randChances(0) + 1) / 2))
          .intercept(message).isEmpty
      )
    }

    it("doesn't drop the message if chances() == false") {
      given ExecutionContext = testContext(1, 1)
      assert(
        MessageDropInterceptor(Chance(randChances(1) / 2))
          .intercept(message) === List(message)
      )
    }
  }
}
