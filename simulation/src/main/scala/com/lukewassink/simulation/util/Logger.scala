package com.lukewassink.simulation.util

import com.lukewassink.simulation.core.{ExecutionContext, Message}
import com.lukewassink.simulation.core.MessageStage.Scheduled
import scala.collection.mutable.ArrayBuffer

// A log of simulation events that would otherwise be hard to infer from the simulation history.
// It should NOT be used in any simulation logic.
// It exists only for later analysis and rendering.
// The log is append-only.
class Logger {
  private val store = ArrayBuffer[List[LogEvent]]()

  // Adds a frame to the log store. Should be called by the simulation at the beginning of each tick.
  def addFrame(): Unit = store.append(List.empty)

  def log(event: LogEvent): Unit = {
    if store.isEmpty then addFrame()
    store.update(store.size - 1, event :: store.last)
  }

  def exportLog: Vector[List[LogEvent]] = store.toVector
}

enum LogEvent:
  case MessageDropEvent(message: Message[Scheduled])
