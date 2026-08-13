package com.lukewassink.runner.config

// Default values to merge into SyntaxNodes. The config can overwrite these defaults,
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
