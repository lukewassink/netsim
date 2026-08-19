package com.lukewassink.runner.config

import io.github.edadma.hocon.Hocon

import scala.util.Random

// Allow the user to set `randomSeed = auto-seed()` rather than picking a seed.
// If they do, pick a random one for them.
object AutoRandomSeed {
  def withAutoSeed(configText: String): String = configText.replaceFirst(
    "\\s+auto-seed\\(\\)\\s*\\R",
    s" ${Random.nextLong().toString}\n\n"
  )
}
