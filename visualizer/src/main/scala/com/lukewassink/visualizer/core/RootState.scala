package com.lukewassink.visualizer.core

import com.lukewassink.runner.core.{Runner, Simulation}
import com.lukewassink.simulation.core.Network
import com.lukewassink.visualizer.util.DefaultSimulation.{
  defaultSimulation, emptySimulation
}
import com.lukewassink.visualizer.util.{DefaultSimulation, PlaybackState}
import com.raquo.laminar.api.L.{*, given}
import com.lukewassink.runner.util.Failure

object RootState:
  // Amount of the network history to take.
  private val HISTORY_LENGTH = 200

class RootState(val config: Var[String], val playbackState: PlaybackState) {
  def this(config: Var[String]) = this(
    config,
    PlaybackState(RootState.HISTORY_LENGTH)
  )

  private val result = config.signal.map(Runner.run)

  lazy val simulation: Signal[Simulation] = result.changes.filter(_.isSuccess)
    .map(_.getOrElse(emptySimulation)).startWith(defaultSimulation)

  val history: Signal[Vector[Network]] = simulation
    .map(_.history.take(RootState.HISTORY_LENGTH).toVector)

  val currentNetwork: Signal[Network] = playbackState.tick.combineWith(history)
    .map((t, h) => h(t))

  // If parsing the config fails, the error message goes here, empty string otherwise.
  val errorMessage: Signal[String] = result.map {
    case f: Failure[Simulation] => f.toString
    case _                      => ""
  }

  // Must be passed to a component to bind.
  val refreshPlaybackOnConfigUpdate: Binder.Base =
    simulation --> (_ => playbackState.reset())

  private val lastValidConfig: Var[String] = Var(DefaultSimulation.config)

  // Must be passed to a component to bind.
  val updateLastValidConfig: Binder.Base =
    config.signal.combineWith(result).changes.filter((_, r) => r.isSuccess)
      .map(_._1) --> lastValidConfig.set

  def resetConfig(): Unit = config.set(lastValidConfig.now())
}
