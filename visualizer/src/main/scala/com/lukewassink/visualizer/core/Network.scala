package com.lukewassink.visualizer.core

import com.raquo.laminar.api.L.{*, given}
import NodeRenderer.addData
import com.lukewassink.runner.Runner
import com.lukewassink.simulation.core.MessageStage.Scheduled
import com.lukewassink.simulation.core.{Message, MessageID, Network, NodeID}
import com.lukewassink.visualizer.util.DefaultNetwork.defaultNetwork

// Render the nodes and messages in the network as lists of SVG elements.
object Network {
  private val historyLength = 10_000

  private val initialNetwork = defaultNetwork

  private val networkHistory =
    Runner.run(initialNetwork).take(historyLength).toVector

  val currentTick: Var[Int] = Var[Int](0)

  private val currentState: Signal[Network] =
    currentTick.signal.map(networkHistory(_))

  val nodeData: Signal[Map[NodeID, NodeData]] =
    currentState.map(addData)

  val nodeElements: Signal[List[SvgElement]] =
    nodeData.map(_.values.toList).split(_.node.id)(NodeRenderer.render)

  private val messageData: Signal[List[MessageData]] =
    currentState
      .combineWith(nodeData)
      .mapN(MessageRenderer.addData)

  val messageElements: Signal[List[SvgElement]] =
    messageData.split(_.message.uniqueID)(MessageRenderer.render)
}
