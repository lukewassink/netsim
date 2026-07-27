package com.lukewassink.visualizer.test_util

import com.lukewassink.simulation.behavior.SimpleSender
import com.lukewassink.simulation.core.{Message, MessageContent, MessageHeader, NetworkState, Node, NodeHeader, NodeState}
import com.lukewassink.simulation.util.XORRandom
import com.raquo.laminar.api.L.{*, given}

// At preconfigured network for testing purposes.
object NetworkUtil {

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

  val testNetwork = NetworkState(
    7,
    List(nodeA, nodeB, nodeC),
    List(messageAToB, messageAToC, messageBToA),
    XORRandom.fromSeed(1L)
  )

  val testNetworkVar: Var[NetworkState] = Var(testNetwork)
}
