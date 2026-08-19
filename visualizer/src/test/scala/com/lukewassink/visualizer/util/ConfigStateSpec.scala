package com.lukewassink.visualizer.util

import com.lukewassink.visualizer.test_util.UnitSpec

class ConfigStateSpec extends UnitSpec {
  describe("simulation") {
    it("transforms the config into a simulation") {}

    it("doesn't update if there is an error in the config") {}
  }

  describe("errorMessage") {
    it("is empty if there is no error") {}

    it("contains error messages if the config isn't valid") {}
  }

  describe("resetConfig") {
    it("does nothing if the config is valid") {}

    it("reverts the the last valid config") {}
  }
}
