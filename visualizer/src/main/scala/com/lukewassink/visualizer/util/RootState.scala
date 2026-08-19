package com.lukewassink.visualizer.util

import com.lukewassink.runner.core.{Runner, Simulation}
import com.lukewassink.runner.util.Failure
import com.lukewassink.simulation.core.Network
import com.lukewassink.visualizer.processing.{
  SyntheticHistory, SyntheticHistoryRow
}
import com.lukewassink.visualizer.util.DefaultSimulation.{
  defaultSimulation, emptySimulation
}
import com.lukewassink.visualizer.util.{DefaultSimulation, PlaybackState}
import com.raquo.laminar.api.L.{*, given}

type NetworkState = (network: Network, row: SyntheticHistoryRow)

object RootState:
  // Amount of the network history to take.
  private val HISTORY_LENGTH = 10_000

class RootState private (using Owner)(
    val configState: ConfigState,
    val playbackState: PlaybackState
) {
  def this(initialConfig: String)(using owner: Owner) = this(using owner)(
    ConfigState(initialConfig),
    PlaybackState(RootState.HISTORY_LENGTH)
  )

  private val history: Signal[Vector[Network]] = configState.simulation
    .map(_.history.take(RootState.HISTORY_LENGTH).toVector)

  private val syntheticHistory: Signal[SyntheticHistory] = history
    .map(h => SyntheticHistory(h.last.ctx.logger))

  val currentNetworkState: Signal[NetworkState] = playbackState.tick
    .combineWith(history, syntheticHistory)
    .map((t, h, sh) => (h(t), sh.history(t)))

  configState.simulation.addObserver(playbackState.resetter)
}
