package com.lukewassink.visualizer.playback

import com.lukewassink.visualizer.core.PlaybackState
import com.lukewassink.visualizer.core.NetworkRenderer.FrameLength
import com.lukewassink.visualizer.core.RootRenderer.given
import com.raquo.laminar.api.L.{*, given}

object PlayButton {
  def render(using playbackState: PlaybackState): HtmlElement = {
    val PlaybackState(playing, tick, historyLength) = playbackState

    val playbackStream =
      EventStream
        .periodic(FrameLength)
        .combineWith(playing.signal.changes)
        .filter((_, p) => p)

    button(
      cls := "play-button control-element",
      cls <-- playing.signal.splitBoolean(_ => "paused", _ => ""),
      onClick --> (_ => playing.update(!_)),
      playbackStream --> (i =>
        tick.update(v => Math.min(v + 1, historyLength - 1))
      ),
      tick.signal --> (t => if t >= historyLength - 1 then playing.set(false))
    )
  }
}
