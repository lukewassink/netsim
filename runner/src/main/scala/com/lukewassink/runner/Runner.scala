package com.lukewassink.runner

import com.lukewassink.simulation.core.NetworkState

object Runner {
  def run(initialState: NetworkState): LazyList[NetworkState] =
    initialState #:: run(initialState.nextState())
}
