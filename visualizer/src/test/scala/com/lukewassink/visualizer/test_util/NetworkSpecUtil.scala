package com.lukewassink.visualizer.test_util

import com.lukewassink.simulation.behavior.SimpleSender
import com.lukewassink.simulation.core.{
  Message,
  MessageContent,
  Network,
  Node,
  NodeHeader,
  NodeState
}
import com.lukewassink.simulation.util.XORRandom
import com.lukewassink.simulation.test_utils.ImplicitConversions.given
import com.lukewassink.simulation.test_utils.MessageSpecUtil.{
  draftedMessage,
  scheduledMessage
}
import com.raquo.laminar.api.L.{*, given}

// At preconfigured network for testing purposes.
object NetworkSpecUtil {

  private val messageAToB = scheduledMessage(1, 1, 2, 0, 10, "AToB")
  private val messageAToC = scheduledMessage(4, 1, 3, 0, 8, "AToC")
  private val messageBToA = scheduledMessage(9, 2, 1, 0, 11, "BToA")

  private val messageCToB = draftedMessage(2, "CToB")

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

  val testNetwork = Network(
    7,
    List(nodeA, nodeB, nodeC),
    List(messageAToB, messageAToC, messageBToA),
    XORRandom.fromSeed(1L)
  )

  val testNetworkVar: Var[Network] = Var(testNetwork)
}
