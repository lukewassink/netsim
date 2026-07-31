package com.lukewassink.visualizer.core

import com.lukewassink.runner.Runner
import com.lukewassink.simulation.core.Network
import com.lukewassink.visualizer.core.NetworkRenderer
import com.lukewassink.visualizer.playback.PlaybackControls
import com.lukewassink.visualizer.util.DefaultNetwork.defaultNetwork
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

// Hook into the document and render the root element.
@main
def RenderRoot(): Unit =
  renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    RootRenderer.render()
  )

case class PlaybackState(
    playing: Var[Boolean],
    tick: Var[Int],
    historyLength: Int
)

case class NetworkState(network: Signal[Network])

object RootRenderer:
  // Amount of the network history to take.
  private val HistoryLength = 100

  private val initialNetwork = defaultNetwork

  private val networkHistory =
    Runner.run(initialNetwork).take(HistoryLength).toVector

  given playbackState: PlaybackState =
    PlaybackState(Var(false), Var(0), HistoryLength)

  private val currentNetwork: Signal[Network] =
    playbackState.tick.signal.map(networkHistory)

  given NetworkState(currentNetwork)

  def render(): Element = {
    div(
      cls := "root",
      h1("NetSim"),
      div(
        NetworkRenderer.render,
        PlaybackControls.render()
      )
    )
  }
  end render
end RootRenderer
