package com.lukewassink.visualizer.playback

import com.raquo.laminar.api.L.{*, given}
import com.lukewassink.visualizer.core.RootRenderer.given
import com.lukewassink.visualizer.util.PlaybackState

object PlaybackControls {
  def render(using PlaybackState): HtmlElement = div(
    cls := "panel panel--controls",
    PlayButton.render,
    Slider.render,
    FrameCounter.render
  )
}
