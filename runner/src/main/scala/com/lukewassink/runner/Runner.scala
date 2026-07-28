package com.lukewassink.runner

import com.lukewassink.simulation.core.Network

object Runner {
  def run(initialState: Network): LazyList[Network] =
    initialState #:: run(initialState.next())
}
