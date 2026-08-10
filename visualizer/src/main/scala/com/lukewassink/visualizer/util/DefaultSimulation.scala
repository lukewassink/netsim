package com.lukewassink.visualizer.util

import com.lukewassink.runner.core.{Simulation, SimulationMetadata}
import com.lukewassink.simulation.behavior.SimpleSender
import com.lukewassink.simulation.core.ResponseState.Request
import com.lukewassink.simulation.core.MessageStage.{Drafted, Scheduled}
import com.lukewassink.simulation.core.{
  Message, MessageContent, MessageID, Network, Node, NodeHeader, NodeID,
  NodeState
}
import com.lukewassink.simulation.util.{Time, XORRandom}

// The default network to display in the visualizer.
object DefaultSimulation {
  // The default value for the simulation config.
  val config: String =
    """name = "simulation-name"
randomSeed = 10

network {
 nodes = [{
     name = "node-name-1"
     behaviors = []
   }
   {
     name = "node-name-2"
     behaviors = [{type = "simple-responder"}]
   }
   {
     name = "node-name-3"
     behaviors = [
       {type = "simple-responder"}
       {type = "simple-responder"}
       {
         type = "simple-sender"
         time = 1
         receiver = "node-name-2"
         content = "Hi!"
       }
     ]
  }]
}""".stripMargin

  // An empty simulation that can serve as a starting value for Laminar signals. It should never actually be rendered.
  val emptySimulation = Simulation(
    SimulationMetadata("simulation-name", 1),
    Network(Time(0), List.empty, List.empty, XORRandom.fromSeed(1))
  )
}
