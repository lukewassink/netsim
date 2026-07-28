package com.lukewassink.visualizer.util

import com.lukewassink.simulation.behavior.SimpleSender
import com.lukewassink.simulation.core.ResponseState.Request
import com.lukewassink.simulation.core.MessageStage.{Drafted, Scheduled}
import com.lukewassink.simulation.core.{
  Message,
  MessageContent,
  MessageID,
  Network,
  Node,
  NodeHeader,
  NodeID,
  NodeState
}
import com.lukewassink.simulation.util.{Time, XORRandom}

// The default network to display in the visualizer.
//
// TODO(#32) delete this once we can load the network from a config.
object DefaultNetwork {

  // Implicit conversions to allow simply passing Int values to message, node, and network constructors.
  // It's OK to just copy/paste these in here because this whole file will be deleted as soon as we can load
  // networks from config.
  given Conversion[Int, NodeID] with
    def apply(id: Int): NodeID = NodeID(id)

  given Conversion[Int, MessageID] with
    def apply(id: Int): MessageID = MessageID(id)

  given Conversion[Int, Time] with
    def apply(ticks: Int): Time = Time(ticks)

  val messageAToB =
    Message[Scheduled](
      Scheduled(1, 1, 2, 0, 10),
      Request(),
      MessageContent("AToB")
    )
  val messageAToC =
    Message[Scheduled](
      Scheduled(4, 1, 3, 0, 8),
      Request(),
      MessageContent("AToC")
    )
  val messageBToA =
    Message[Scheduled](
      Scheduled(9, 2, 1, 0, 15),
      Request(),
      MessageContent("BToA")
    )

  val messageCToB =
    Message[Drafted](Drafted(2), Request(), MessageContent("CToB"))

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

  val defaultNetwork = Network(
    7,
    List(nodeA, nodeB, nodeC),
    List(messageAToB, messageAToC, messageBToA),
    XORRandom.fromSeed(1L)
  )
}
