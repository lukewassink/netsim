package com.lukewassink.runner.core

import com.lukewassink.simulation.core.Network

// Additional data for a simulation that shouldn't be part of the network.
case class SimulationMetadata(name: String, randomSeed: Long)

// Return type for the runner. Contains:
// -- a stream of network states
// -- additional metadata for logging, rendering, storage, etc.
case class Simulation(metadata: SimulationMetadata, networks: LazyList[Network])
