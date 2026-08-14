package com.lukewassink.runner.config

// Default values to merge into SyntaxNodes. A config can overwrite them,
// but it doesn't have to.
object SyntaxNodeDefaults {
  val defaultSimulationNode: String =
    """
      |interceptors = []
      |
      |network {
      |  nodes = []
      |}""".stripMargin
}
