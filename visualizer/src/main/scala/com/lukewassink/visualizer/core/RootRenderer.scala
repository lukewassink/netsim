package com.lukewassink.visualizer.core

import com.lukewassink.runner.core.{Runner, Simulation}
import com.lukewassink.runner.util.{Failure, Success}
import com.lukewassink.simulation.core.Network
import com.lukewassink.visualizer.core.NetworkRenderer
import com.lukewassink.visualizer.playback.PlaybackControls
import com.lukewassink.visualizer.util.{DefaultNetwork, PlaybackState}
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

import math.{max, min}

// Hook into the document and render the root element.
@main
def RenderRoot(): Unit = renderOnDomContentLoaded(
  dom.document.getElementById("app"),
  RootRenderer.render()
)

case class NetworkState(network: Signal[Network])

object RootRenderer:
  // Amount of the network history to take.
  private val HistoryLength = 100

  private val result = Runner.run(DefaultNetwork.config)

  private val networkHistory: Vector[Network] =
    result match {
      case Success(simulation: Simulation) =>
        simulation.history.take(HistoryLength).toVector
      case Failure(_) => Vector.empty
    }

  given playbackState: PlaybackState = PlaybackState(HistoryLength)

  private val currentNetwork: Signal[Network] = playbackState.tick
    .map(networkHistory)

  given NetworkState(currentNetwork)

  def render(): Element = div(
    cls := "root",
    h1("NetSim"),
    div(NetworkRenderer.render, PlaybackControls.render())
  )
  end render
end RootRenderer
