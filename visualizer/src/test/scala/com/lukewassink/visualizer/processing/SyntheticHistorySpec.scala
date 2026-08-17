package com.lukewassink.visualizer.processing

import com.lukewassink.simulation.core.ExecutionContext
import com.lukewassink.simulation.test_utils.MessageSpecUtil.scheduledMessage
import com.lukewassink.simulation.util.LogEvent.MessageDropEvent
import com.lukewassink.visualizer.processing.SyntheticHistoryElement.DroppedMessageElement
import com.lukewassink.visualizer.test_util.UnitSpec

class SyntheticHistorySpec extends UnitSpec {
  describe("SyntheticHistory") {
    it("builds synthetic history from a log") {
      given e: ExecutionContext = ExecutionContext(1, 1, 1)
      val logger                = e.logger
      assert(SyntheticHistory(logger).history.isEmpty)

      val message = scheduledMessage(1, 1, 2, 1, 5, "Hi")
      logger.log(MessageDropEvent(message))

      val history = SyntheticHistory(logger).history
      history.keys should contain theSameElementsAs List(1, 2)
      history(1) should contain theSameElementsAs
        List(DroppedMessageElement(message))
      history(2) should contain theSameElementsAs history(1)
    }
  }
}
