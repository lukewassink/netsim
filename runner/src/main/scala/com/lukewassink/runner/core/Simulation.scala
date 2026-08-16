package com.lukewassink.runner.core

import com.lukewassink.simulation.core.Network

// Additional data for a simulation that shouldn't be part of the network.
case class SimulationMetadata(name: String, randomSeed: Long)

// Return type for the runner. Contains:
// -- the initial network state
// -- additional metadata for logging, rendering, storage, etc.
case class Simulation(metadata: SimulationMetadata, initialNetwork: Network):
  // Lazily compute and cache the network evolution over time.
  lazy val history: LazyList[Network] = Simulation.history(initialNetwork)

  val logger: Logger = initialNetwork.ctx.logger

case object Simulation:
  private def history(network: Network): LazyList[Network] =
    network #:: history(network.next)
