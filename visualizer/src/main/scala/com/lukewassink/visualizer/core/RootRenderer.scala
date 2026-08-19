package com.lukewassink.visualizer.core

import com.lukewassink.visualizer.config.ConfigRenderer
import com.lukewassink.visualizer.core.NetworkRenderer
import com.lukewassink.visualizer.playback.PlaybackControls
import com.lukewassink.visualizer.util.{
  DefaultSimulation, PlaybackState, RootState
}
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

// Hook into the document and render the root element.
@main
def RenderRoot(): Unit = renderOnDomContentLoaded(
  dom.document.getElementById("app"),
  RootRenderer.render()
)

object RootRenderer:

  def render(): Element = div(
    cls := "root",
    h1("NetSim"),

    onMountInsert { ctx =>
      given Owner = ctx.owner

      given rootState: RootState = RootState(DefaultSimulation.config)

      given PlaybackState = rootState.playbackState
      div(
        cls("body"),
        div(
          NetworkRenderer.render(rootState.currentNetworkState),
          PlaybackControls.render,
          cls("container-vertical")
        ),
        ConfigRenderer.render
      )
    }
  )
