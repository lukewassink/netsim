package core

import test_utils.BehaviorSpecUtil.TestSelfUpdateBehavior
import test_utils.NetworkStateSpecUtil.testNetworkState
import test_utils.{BehaviorSpecUtil, UnitSpec}
import test_utils.NodeSpecUtil.testNodeState
import test_utils.RandomSpecUtil.InertRandom

class NetworkStateSpec extends UnitSpec {
  describe("NetworkState") {
    val emptyNetwork = testNetworkState(1, List.empty, List.empty)

    val emptyContent = MessageContent("")
    val messageAToB = Message(MessageHeader(1, 1, 2, 3, Some(10)), emptyContent)
    val messageAToC = Message(MessageHeader(4, 1, 3, 5, Some(10)), emptyContent)
    val messageBToA = Message(MessageHeader(9, 2, 1, 4, Some(10)), emptyContent)
    val nodeA = Node(
      List.empty,
      testNodeState(
        NodeHeader(1, 0),
        List(messageAToB, messageAToC),
        List.empty
      )
    )
    val nodeB = Node(
      List.empty,
      testNodeState(NodeHeader(2, 0), List(messageBToA), List.empty)
    )
    val nodeC = Node(
      List(BehaviorSpecUtil.TestSelfUpdateBehavior(0)),
      testNodeState(NodeHeader(3, 0), List.empty, List.empty)
    )
    val network = testNetworkState(
      1,
      List(nodeA, nodeB, nodeC),
      List(messageAToB, messageAToC, messageBToA)
    )
    val nextNetwork = network.nextState()

    describe("NextState") {
      it("ticks the time forward") {
        assert(nextNetwork.time - network.time === 1)
      }

      // TODO(#22): Make this test work once delivered messages are visible.
//      it("delivers current messages") {
//        val readyToSend = NetworkState(
//          9,
//          Map[Int, Node](1 -> stubNodeA, 2 -> stubNodeB, 3 -> stubNodeC),
//          MessageQueue(List(messageAToB, messageAToC, messageBToA)),
//          InertRandom
//        )
//        val withSentMessages = readyToSend.nextState()
//
//        readyToSend.nodes(1).sharedState.incomingMessages shouldBe empty
//        readyToSend.nodes(2).sharedState.incomingMessages shouldBe empty
//        readyToSend.nodes(3).sharedState.incomingMessages shouldBe empty
//        withSentMessages
//          .nodes(1)
//          .sharedState
//          .incomingMessages should contain theSameElementsAs List(messageBToA)
//        withSentMessages
//          .nodes(2)
//          .sharedState
//          .incomingMessages should contain theSameElementsAs List(messageAToB)
//        withSentMessages
//          .nodes(3)
//          .sharedState
//          .incomingMessages should contain theSameElementsAs List(messageAToC)
//      }

      it("triggers node behavior") {
        network.nodes(3).behaviors.head match {
          case TestSelfUpdateBehavior(selfState) =>
            selfState.should(equal(0))
        }
        nextNetwork.nodes(3).behaviors.head match {
          case TestSelfUpdateBehavior(selfState) =>
            selfState.should(equal(1))
        }
      }

      it("collects new messages") {}
    }

    describe("List constructor") {
      it("handles an empty list") {
        NetworkState(0, List.empty, List.empty, InertRandom()) should equal(
          NetworkState(0, Map.empty, MessageQueue.empty, InertRandom())
        )
      }

      it("handles a list of nodes") {
        NetworkState(
          0,
          List(nodeA, nodeB, nodeC),
          List.empty,
          InertRandom()
        ).nodes should contain theSameElementsAs Map(
          1 -> nodeA,
          2 -> nodeB,
          3 -> nodeC
        )
      }

      it("handles a list of messages") {
        NetworkState(
          0,
          List.empty,
          List(messageAToB, messageAToC, messageBToA),
          InertRandom()
        ).messagesInTransit.allMessages should contain theSameElementsAs List(
          messageAToB,
          messageAToC,
          messageBToA
        )
      }
    }
  }
}
