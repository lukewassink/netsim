package com.lukewassink.visualizer.playback

import com.raquo.laminar.api.L.{*, given}
import com.lukewassink.visualizer.core.RootRenderer.given

object PlaybackControls {
  def render: HtmlElement = div(
    cls := "playback-controls",
    PlayButton.render,
    Slider.render,
    FrameCounter.render
  )
}
