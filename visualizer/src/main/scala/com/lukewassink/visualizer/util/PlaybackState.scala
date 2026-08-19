package com.lukewassink.visualizer.util
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom.UIEvent

import Math.{max, min}

final class PlaybackState(
    val playing: Var[Boolean],
    val historyLength: Int,
    private val tickVar: Var[Int]
) {
  def this(length: Int) = this(Var(false), length, Var(0))

  def maxTick: Int = historyLength - 1

  def tick: StrictSignal[Int] = tickVar.signal

  def increment(): Unit = tickVar.update(i => min(i + 1, maxTick))

  def tickWriter: Observer[Int] = tickVar.writer
    .contramap[Int](i => min(max(i, 0), maxTick))

  // Forces a new event, which can be used to refresh UI elements.
  def refreshTick(): Unit = tickVar.update(identity)

  def pause(): Unit = playing.set(false)

  def togglePlaying(): Unit = playing.update(!_)

  val resetter: Observer[Any] = Observer { _ =>
    playing.set(true)
    tickVar.set(0)
  }
}
