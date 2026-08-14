package com.lukewassink.visualizer.util

import com.lukewassink.runner.core.{Runner, Simulation, SimulationMetadata}
import com.lukewassink.simulation.core.Network
import com.lukewassink.simulation.util.Time

// The default network to display in the visualizer.
object DefaultSimulation {
  // The default value for the simulation config.
  val config: String =
    """|name = "default-simulation"
      |randomSeed = 10
      |
      |interceptors = [{
      |    type = random-latency
      |    distribution {
      |      type = uniform
      |      min = 5
      |      max = 25
      |    }
      |  }
      |]
      |
      |nodes = [{
      |   name = "node-1"
      |   behaviors = [{type = "simple-responder"}]
      | }
      | {
      |   name = "node-2"
      |   behaviors = [
      |     {type = "simple-responder"}
      |     {
      |       type = "simple-sender"
      |       time = 1
      |       receiver = "node-1"
      |       content = "Hi!"
      |     }
      |   ]
      | }
      |]""".stripMargin

  // An empty simulation that can serve as a starting value for Laminar signals. It should never actually be rendered.
  val emptySimulation = Simulation(
    SimulationMetadata("simulation-name", 1),
    Network(Time(0), List.empty, List.empty)
  )

  val defaultSimulation: Simulation = Runner.run(config)
    .getOrElse(emptySimulation)
}
