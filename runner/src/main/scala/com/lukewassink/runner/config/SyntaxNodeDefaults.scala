package com.lukewassink.runner.config

import io.github.edadma.hocon.{Config, Hocon}

// Default values to merge into SyntaxNodes. A config can overwrite them,
// but it doesn't have to.
object SyntaxNodeDefaults {
  private val defaultSimulationNodeString: String =
    """
      |ticksPerMillisecond = 1
      |
      |interceptors = []
      |nodes = []
      |""".stripMargin
  val defaultSimulationNodeConfig: Config = Hocon
    .parse(defaultSimulationNodeString)

  private val defaultNodeNodeString: String =
    """
      |behaviors = []
      |""".stripMargin
  val defaultNodeNodeConfig: Config = Hocon.parse(defaultNodeNodeString)

  private val defaultReliableBroadcasterNodeString: String =
    """
      |ackTimeout = 50
      |maxRetries = 3
      |dedupeTimeout = 200
      |""".stripMargin
  val defaultReliableBroadcasterNodeConfig: Config = Hocon
    .parse(defaultReliableBroadcasterNodeString)
}
