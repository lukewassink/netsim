package run

import core.NetworkState

object Runner {
  def run(initialState: NetworkState): LazyList[NetworkState] =
    initialState #:: run(initialState.nextState())
}
