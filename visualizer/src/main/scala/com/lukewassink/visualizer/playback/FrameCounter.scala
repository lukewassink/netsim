package com.lukewassink.visualizer.playback

import com.lukewassink.visualizer.core.RootRenderer.given
import com.raquo.laminar.api.L.{*, given}
import com.lukewassink.visualizer.util.PlaybackState

object FrameCounter {
  def render(using playbackState: PlaybackState): HtmlElement = input(
    typ("number"),
    cls("frame-counter control-element"),
    cls <-- playbackState.playing.signal.splitBoolean(_ => "disabled", _ => ""),
    minAttr("0"),
    maxAttr((playbackState.historyLength - 1).toString),
    value <-- playbackState.tick.map(_.toString),
    onChange.mapToValue.filterNot(_.isEmpty).map(_.toInt) -->
      playbackState.tickWriter,
    disabled <-- playbackState.playing,
    onBlur --> (_ => playbackState.refreshTick()),
    placeholder <-- playbackState.tick.map(_.toString)
  )
}
