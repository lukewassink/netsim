package com.lukewassink.visualizer.playback

import com.lukewassink.visualizer.core.NetworkRenderer.FrameLength
import com.lukewassink.visualizer.core.RootRenderer.given
import com.lukewassink.visualizer.util.PlaybackState
import com.raquo.laminar.api.L.{*, given}

object PlayButton {
  def render(using playbackState: PlaybackState): HtmlElement = {
    val playbackStream = EventStream.periodic(FrameLength)
      .combineWith(playbackState.playing.signal.changes).filter((_, p) => p) // Only emit new events when playing is true

    button(
      cls := "play-button control-element",
      cls <-- playbackState.playing.signal.splitBoolean(_ => "paused", _ => ""),
      onClick --> (_ => playbackState.togglePlaying()),
      playbackStream --> (_ => playbackState.increment()),
      playbackState.tick -->
        (t => if t >= playbackState.historyLength - 1 then playbackState.pause())
    )
  }
}
