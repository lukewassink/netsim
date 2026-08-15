package com.lukewassink.simulation.util

import com.lukewassink.simulation.core.ExecutionContext
import com.lukewassink.simulation.test_utils.{MessageSpecUtil, UnitSpec}
import com.lukewassink.simulation.util.LogEvent.MessageDropEvent

class LoggerSpec extends UnitSpec {
  private val message = MessageSpecUtil.scheduledMessage(1, 1, "Hi")
  private val event   = MessageDropEvent(message)

  describe("log") {
    it("adds log events to the store and returns them as an immutable vector") {
      given ExecutionContext = ExecutionContext(0, 1, 1)
      val logger             = Logger()
      assert(logger.exportLog === Vector.empty)
      logger.log(event)
      logger.log(event)
      assert(logger.exportLog === Vector(List(event, event)))
    }

    it("extends the log to the current tick and adds the logged event there") {
      val logger = Logger()
      assert(logger.exportLog === Vector.empty)
      logger.log(using ExecutionContext(0, 1, 1))(event)
      assert(logger.exportLog === Vector(List(event)))
      logger.log(using ExecutionContext(2, 1, 1))(event)
      assert(logger.exportLog === Vector(List(event), List(), List(event)))
    }
  }
}
