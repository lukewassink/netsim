package com.lukewassink.visualizer.playback

import com.lukewassink.visualizer.core.PlaybackState
import com.lukewassink.visualizer.core.RootRenderer.given
import com.raquo.laminar.api.L.{*, given}

object FrameCounter {
  def render(using playbackState: PlaybackState): HtmlElement = {
    val PlaybackState(playing, tick, historyLength) = playbackState
    input(
      typ("number"),
      cls("frame-counter control-element"),
      cls <-- playing.signal
        .splitBoolean(_ => "disabled", _ => ""),
      minAttr("0"),
      maxAttr((historyLength - 1).toString),
      value <-- tick.signal.map(_.toString),
      onChange.mapToValue.filterNot(_.isEmpty) --> (v =>
        tick.set(Math.min(v.toInt, historyLength - 1))
      ),
      disabled <-- playing,
      onBlur --> (_ => tick.update(identity)),
      placeholder <-- tick.signal.map(t => t.toString)
    )
  }
}
