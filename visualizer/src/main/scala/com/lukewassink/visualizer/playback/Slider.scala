package com.lukewassink.visualizer.playback

import com.lukewassink.visualizer.core.PlaybackState
import com.lukewassink.visualizer.core.RootRenderer.given
import com.raquo.laminar.api.L.{*, given}

object Slider {
  def render(using playbackState: PlaybackState): HtmlElement = {
    val PlaybackState(playing, tick, historyLength) = playbackState

    input(
      typ("range"),
      minAttr("0"),
      cls("slider control-element"),
      maxAttr((historyLength - 1).toString),
      value <-- playbackState.tick.signal.map(_.toString),
      onClick --> (_ => playing.set(false)),
      onInput.mapToValue --> (v => tick.set(v.toInt))
    )
  }
}
