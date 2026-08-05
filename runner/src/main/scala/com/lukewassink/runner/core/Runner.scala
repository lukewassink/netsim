package com.lukewassink.runner.core

import com.lukewassink.simulation.core.Network

object Runner {
  def run(initialState: Network): LazyList[Network] = initialState.toStream
}
