package com.lukewassink.visualizer.playback

import com.lukewassink.visualizer.core.RootRenderer.given
import com.lukewassink.visualizer.util.PlaybackState
import com.raquo.laminar.api.L.{*, given}

object Slider {
  def render(using playbackState: PlaybackState): HtmlElement = {
    input(
      typ("range"),
      minAttr("0"),
      cls("slider control-element"),
      maxAttr(playbackState.maxTick.toString),
      value <-- playbackState.tick.map(_.toString),
      onClick --> (_ => playbackState.pause()),
      onInput.mapToValue.map(_.toInt) --> playbackState.tickWriter
    )
  }
}
