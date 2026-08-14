package com.lukewassink.simulation.util

import com.lukewassink.simulation.core.Message
import com.lukewassink.simulation.core.MessageStage.Scheduled
import com.lukewassink.simulation.test_utils.{MessageSpecUtil, UnitSpec}
import com.lukewassink.simulation.util.LogEvent.MessageDropEvent

class LoggerSpec extends UnitSpec {
  private val message = MessageSpecUtil.scheduledMessage(1, 1, "Hi")
  private val event   = MessageDropEvent(message)

  describe("log") {
    it("adds log events to the store and returns them as an immutable vector") {
      val logger = Logger()
      assert(logger.exportLog === Vector.empty)
      logger.addFrame()
      logger.log(event)
      logger.log(event)
      assert(logger.exportLog === Vector(List(event, event)))
    }

    it("adds a frame if the log is empty") {
      val logger = Logger()
      assert(logger.exportLog === Vector.empty)
      logger.log(event)
      assert(logger.exportLog === Vector(List(event)))
    }
  }

  describe("addFrame") {
    it("adds a frame to the log") {
      val logger = Logger()
      assert(logger.exportLog === Vector.empty)
      logger.addFrame()
      logger.addFrame()
      logger.log(event)
      logger.addFrame()
      logger.log(event)
      assert(logger.exportLog === Vector(List(), List(event), List(event)))
    }
  }
}
