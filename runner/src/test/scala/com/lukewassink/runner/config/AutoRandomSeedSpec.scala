package com.lukewassink.runner.config

import com.lukewassink.runner.config.AutoRandomSeed.withAutoSeed
import com.lukewassink.simulation.test_utils.UnitSpec

class AutoRandomSeedSpec extends UnitSpec {
  describe("withAutoSeed") {
    it("inserts a randomly generated seed") {
      val config =
        """name = "simulation-name"
           randomSeed = auto-seed()

           network {
             nodes = []
           }
        """.stripMargin

      // Check that auto-seed() has been replaced with a Long
      withAutoSeed(config).split("\\R")(1).split("\\s+")(3).toLongOption should
        matchPattern { case Some(l) => }
    }

    it("does nothing if the seed is already set") {
      val config =
        """name = "simulation-name"
           randomSeed = 15

           network {
             nodes = []
           }
        """.stripMargin

      assert(withAutoSeed(config) === config)
    }
  }
}
