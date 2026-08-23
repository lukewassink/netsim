package com.lukewassink.simulation.util

import com.lukewassink.simulation.core.ExecutionContext
import com.lukewassink.simulation.message.MessageStage.Scheduled
import com.lukewassink.simulation.message.Message

import scala.collection.mutable.ArrayBuffer

// A log of simulation events that would otherwise be hard to infer from the simulation history.
// It should NOT be used in any simulation logic.
// It exists only for later analysis and rendering.
// The log is append-only.
class Logger {
  private val store = ArrayBuffer[List[LogEvent]]()

  def log(using ctx: ExecutionContext)(event: LogEvent): Unit = {
    val gap = ctx.tick + 1 - store.size
    if gap > 0 then store ++= Seq.fill(gap)(List.empty[LogEvent])
    store.update(ctx.tick, event :: store.last)
  }

  def exportLog: Vector[List[LogEvent]] = store.toVector

  def size: Int = store.size

  override def equals(obj: Any): Boolean =
    obj match {
      case other: Logger => store == other.store
      case _             => false
    }
}

enum LogEvent:
  case MessageDropEvent(message: Message[Scheduled])
