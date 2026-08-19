package com.lukewassink.visualizer.config

import com.lukewassink.runner.config.AutoRandomSeed.withAutoSeed
import com.lukewassink.visualizer.util.{
  ConfigState, DefaultSimulation, RootState
}
import com.raquo.laminar.api.L.{*, given}

object ConfigRenderer {
  private val configText = Var(DefaultSimulation.config)

  def render(using rootState: RootState): HtmlElement = {
    val configState = rootState.configState

    div(
      cls("config container-vertical"),
      div(
        cls("config-panel container-vertical"),
        textArea(
          value <-- configText,
          onInput.mapToValue --> configText.writer,
          cls("panel panel--config-input")
        ),
        span(
          text <-- configState.errorMessage,
          display <--
            configState.errorMessage
              .map(e => if e.isEmpty then "none" else "block"),
          cls("config-errors")
        )
      ),
      div(
        button(
          "Run",
          cls("control-element button"),
          onClick -->
            (e =>
              configText.update(withAutoSeed)
              configState.config.set(configText.now())
            )
        ),
        button(
          cls("control-element button"),
          "Reset Config",
          onClick -->
            (_ => {
              configState.resetConfig()
              configText.set(configState.config.now())
            })
        ),
        cls("panel panel--controls")
      )
    )
  }
}
