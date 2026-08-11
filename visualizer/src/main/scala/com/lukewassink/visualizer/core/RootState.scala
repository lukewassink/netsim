package com.lukewassink.visualizer.core

import com.lukewassink.runner.core.{Runner, Simulation}
import com.lukewassink.simulation.core.Network
import com.lukewassink.visualizer.util.DefaultSimulation.{
  defaultSimulation, emptySimulation
}
import com.lukewassink.visualizer.util.PlaybackState
import com.raquo.laminar.api.L.{*, given}
import com.lukewassink.runner.util.Failure

case class RootState(config: Var[String], playbackState: PlaybackState) {
  import com.lukewassink.visualizer.core.RootState.HistoryLength

  private val result = config.signal.map(Runner.run)

  val simulation: Signal[Simulation] = result.changes.filter(_.isSuccess)
    .map(_.getOrElse(emptySimulation)).startWith(defaultSimulation)

  val history: Signal[Vector[Network]] = simulation
    .map(_.history.take(HistoryLength).toVector)

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
}

case object RootState:
  // Amount of the network history to take.
  private val HistoryLength = 200

  def apply(config: Var[String]): RootState = RootState(
    config,
    PlaybackState(HistoryLength)
  )
