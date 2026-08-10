package com.lukewassink.visualizer.core

import com.lukewassink.visualizer.config.ConfigRenderer
import com.lukewassink.visualizer.core.NetworkRenderer
import com.lukewassink.visualizer.playback.PlaybackControls
import com.lukewassink.visualizer.util.{DefaultSimulation, PlaybackState}
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

// Hook into the document and render the root element.
@main
def RenderRoot(): Unit = renderOnDomContentLoaded(
  dom.document.getElementById("app"),
  RootRenderer.render()
)

object RootRenderer:
  private val config = Var(DefaultSimulation.config)

  given rootState: RootState = RootState(config)

  given PlaybackState = rootState.playbackState

  def render(): Element = div(
    cls := "root",
    h1("NetSim"),
    div(
      cls("body"),
      div(
        NetworkRenderer.render(rootState.currentNetwork),
        PlaybackControls.render,
        cls("container-vertical")
      ),
      ConfigRenderer.render
    ),
    rootState.refreshPlaybackOnConfigUpdate
  )
