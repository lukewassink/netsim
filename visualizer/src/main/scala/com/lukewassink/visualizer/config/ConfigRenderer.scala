package com.lukewassink.visualizer.config

import com.lukewassink.visualizer.core.RootState
import com.lukewassink.visualizer.util.DefaultSimulation
import com.raquo.laminar.api.L.{*, given}

object ConfigRenderer {
  private val configText = Var(DefaultSimulation.config)

  def render(using rootState: RootState): HtmlElement = div(
    cls("config"),
    textArea(
      value <-- configText,
      onInput.mapToValue --> configText.writer,
      cls("text")
    ),
    span(
      text <-- rootState.errorMessage,
      display <--
        rootState.errorMessage.map(e => if e.isEmpty then "none" else "block"),
      cls("config-errors")
    ),
    div(
      button("Run", onClick --> (e => rootState.config.set(configText.now()))),
      button(
        "Reset Config",
        onClick --> (e => configText.set(rootState.config.now()))
      ),
      cls("controls")
    )
  )
}
