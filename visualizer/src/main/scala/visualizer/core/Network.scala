package visualizer.core

import com.raquo.laminar.api.L.{*, given}
import core.NetworkState
import visualizer.core.NodeRenderer.addData
import visualizer.core.NodeRenderer
import visualizer.util.DefaultNetwork.defaultNetwork

// Render the nodes and messages in the network as lists of SVG elements.
object Network {
  private val currentState: Var[NetworkState] = Var(defaultNetwork)

  val nodeData: Signal[Map[Int, NodeData]] =
    currentState.signal.map(addData)

  val nodeElements: Signal[List[SvgElement]] =
    nodeData.map(_.values.map(NodeRenderer.render).toList)

  private val messageData: Signal[List[MessageData]] =
    currentState.signal
      .combineWith(nodeData)
      .mapN(MessageRenderer.addData)

  val messageElements: Signal[List[SvgElement]] =
    messageData.map(_.map(MessageRenderer.render))
}
