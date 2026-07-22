package visualizer.core

import behavior.SimpleSender
import com.raquo.laminar.api.L.{*, given}
import core.{Node, *}
import visualizer.core.NodeRenderer.toRenderable
import visualizer.core.NodeRenderer
import util.XORRandom

// Render the nodes and messages in the network as lists of SVG elements.
object Network {
  // Side length for the square the network renders in, in px.
  val SquareSideLength = 700

  val messageAToB =
    Message(MessageHeader(1, 1, 2, 0, Some(10)), MessageContent("AToB"))
  val messageAToC =
    Message(MessageHeader(4, 1, 3, 0, Some(8)), MessageContent("AToC"))
  val messageBToA =
    Message(MessageHeader(9, 2, 1, 0, Some(15)), MessageContent("BToA"))
  val messageCToB =
    Message(MessageHeader(0, 0, 2, 0, None), MessageContent("CToB"))

  val nodeA = Node(
    List.empty,
    NodeState(
      NodeHeader(1, 2),
      List.empty,
      List.empty,
      XORRandom.fromSeed(1L)
    )
  )
  val nodeB = Node(
    List.empty,
    NodeState(NodeHeader(2, 5), List.empty, List.empty, XORRandom.fromSeed(1L))
  )
  val nodeC = Node(
    List(SimpleSender(3, messageCToB)),
    NodeState(NodeHeader(3, 10), List.empty, List.empty, XORRandom.fromSeed(1L))
  )

  val network = NetworkState(
    7,
    List(nodeA, nodeB, nodeC),
    List(messageAToB, messageAToC, messageBToA),
    XORRandom.fromSeed(1L)
  )

  val currentState: Var[NetworkState] = Var(network)

  val renderableNodes: Signal[Map[Int, RenderableNode]] =
    currentState.signal.map(toRenderable)

  val renderedNodes: Signal[List[SvgElement]] =
    renderableNodes.map(_.values.map(NodeRenderer.render).toList)

  val renderableMessages: Signal[List[RenderableMessage]] =
    currentState.signal
      .combineWith(renderableNodes)
      .mapN(MessageRenderer.toRenderable)

  val renderedMessages: Signal[List[SvgElement]] =
    renderableMessages.map(_.map(MessageRenderer.render))
}
