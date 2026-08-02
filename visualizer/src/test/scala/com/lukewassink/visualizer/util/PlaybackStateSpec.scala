package com.lukewassink.visualizer.util

import com.lukewassink.visualizer.test_util.UnitSpec

class PlaybackStateSpec extends UnitSpec {
  private val playbackState = PlaybackState(10)

  describe("constructor") {
    it("starts paused at tick 0") {
      playbackState.historyLength shouldEqual 10
      playbackState.playing.now() shouldEqual false
      playbackState.tick.now() shouldEqual 0
    }
  }

  describe("increment") {
    it("increments the tick") {
      playbackState.tick.now() shouldEqual 0
      playbackState.increment()
      playbackState.tick.now() shouldEqual 1
      playbackState.increment()
      playbackState.tick.now() shouldEqual 2
    }

    it("respects the max history length") {
      (1 to 7).foreach(_ => playbackState.increment())
      playbackState.tick.now() shouldEqual 9
      playbackState.increment()
      playbackState.tick.now() shouldEqual 9
    }
  }

  describe("tickWriter") {
    it("writes to the tick") {
      playbackState.tickWriter.onNext(5)
      playbackState.tick.now() shouldEqual 5
    }

    it("respects the min and max bounds for the tick") {
      playbackState.tickWriter.onNext(-1)
      playbackState.tick.now() shouldEqual 0
      playbackState.tickWriter.onNext(11)
      playbackState.tick.now() shouldEqual 9
    }
  }
}
