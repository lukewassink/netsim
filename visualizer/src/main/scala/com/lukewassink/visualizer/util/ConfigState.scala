package com.lukewassink.visualizer.util

import com.lukewassink.runner.core.{Runner, Simulation}
import com.lukewassink.runner.util.{Failure, Result, Success}
import com.lukewassink.visualizer.util.DefaultSimulation.{
  defaultSimulation, emptySimulation
}
import com.lukewassink.visualizer.util.DefaultSimulation
import com.raquo.laminar.api.L.{*, given}

class ConfigState(using Owner)(initialConfig: String) {
  val config: Var[String] = Var(initialConfig)

  private val result = config.signal.map(Runner.run)

  private val simulationVar = Var(emptySimulation)

  result.addObserver(simulationVar.writer.contracollect { case Success(s) => s })

  val simulation: Signal[Simulation] = simulationVar.signal

  // If parsing the config fails, the error message goes her. Empty string otherwise.
  val errorMessage: Signal[String] = result.map {
    case f: Failure[Simulation] => f.toString
    case _                      => ""
  }

  private val lastValidConfig: Var[String] = Var(DefaultSimulation.config)

  config.signal.combineWith(result).changes.filter((_, r) => r.isSuccess)
    .map(_._1).addObserver(lastValidConfig.writer)

  def resetConfig(): Unit = config.set(lastValidConfig.now())
}
