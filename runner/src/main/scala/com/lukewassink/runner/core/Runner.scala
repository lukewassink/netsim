package com.lukewassink.runner.core

import com.lukewassink.runner.config.SyntaxTree
import com.lukewassink.runner.config.Transformer.transform
import com.lukewassink.runner.util.Result
import com.lukewassink.simulation.core.Network
import io.github.edadma.hocon.{Config, EnvSource, Hocon}

object Runner:
  def run(configString: String): Result[Simulation] = run(
    Hocon.parse(configString)
  )

  private def run(config: Config): Result[Simulation] = SyntaxTree
    .fromConfig(config).map(transform)
