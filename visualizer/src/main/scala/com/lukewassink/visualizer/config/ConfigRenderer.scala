package com.lukewassink.visualizer.config

import com.lukewassink.visualizer.core.RootState
import com.lukewassink.visualizer.util.DefaultSimulation
import com.raquo.laminar.api.L.{*, given}

object ConfigRenderer {
  private val configText = Var(DefaultSimulation.config)

  def render(using rootState: RootState): HtmlElement = div(
    cls("config container-vertical"),
    div(
      cls("config-panel container-vertical"),
      textArea(
        value <-- configText,
        onInput.mapToValue --> configText.writer,
        cls("panel panel--config-input")
      ),
      span(
        text <-- rootState.errorMessage,
        display <--
          rootState.errorMessage.map(e => if e.isEmpty then "none" else "block"),
        cls("config-errors")
      )
    ),
    div(
      button(
        "Run",
        cls("control-element button"),
        onClick --> (e => rootState.config.set(configText.now()))
      ),
      button(
        cls("control-element button"),
        "Reset Config",
        onClick --> (e => configText.set(rootState.config.now()))
      ),
      cls("panel panel--controls")
    )
  )
}
