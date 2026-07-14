package core

import test_utils.MessageSpecUtil.testMessage
import test_utils.NetworkStateSpecUtil.testNetworkState
import test_utils.{BehaviorSpecUtil, UnitSpec}
import test_utils.NodeSpecUtil.testNodeState
import test_utils.RandomSpecUtil.InertRandom

class NetworkStateSpec extends UnitSpec {
  describe("NetworkState") {
    val emptyNetwork = testNetworkState(1, List.empty)

    val emptyContent = MessageContent("")
    val messageAToB = Message(MessageHeader(1, 1, 2, 3, 10), emptyContent)
    val messageAToC = Message(MessageHeader(4, 1, 3, 5, 10), emptyContent)
    val messageBToA = Message(MessageHeader(9, 2, 1, 4, 10), emptyContent)
    val messageCToB = Message(MessageHeader(0, 3, 2, 2, 16), emptyContent)
    val nodeA = Node(
      List.empty,
      testNodeState(NodeHeader(1, 0), List(messageAToB, messageAToC)),
      MessageQueue.empty
    )
    val nodeB = Node(
      List.empty,
      testNodeState(NodeHeader(2, 0), List(messageBToA)),
      MessageQueue.empty
    )
    val nodeC = Node(
      List(BehaviorSpecUtil.TestBehavior(messageCToB)),
      testNodeState(NodeHeader(3, 0), List.empty),
      MessageQueue.empty
    )
    val network = testNetworkState(1, List(nodeA, nodeB, nodeC))
    val nextNetwork = network.nextState()

    describe("NextState") {
      it("delivers outgoing messages") {
        network.nodes(1).incomingMessages.messages shouldBe empty
        network.nodes(2).incomingMessages.messages shouldBe empty
        network.nodes(3).incomingMessages.messages shouldBe empty
        nextNetwork
          .nodes(1)
          .incomingMessages
          .messages should contain theSameElementsAs List(messageBToA)
        nextNetwork
          .nodes(2)
          .incomingMessages
          .messages should contain theSameElementsAs List(messageAToB)
        nextNetwork
          .nodes(3)
          .incomingMessages
          .messages should contain theSameElementsAs List(messageAToC)
      }

      it("ticks the time forward") {
        assert(nextNetwork.time - network.time === 1)
      }

      it("triggers node behavior") {
        network.nodes(3).outgoingMessages shouldBe empty
        nextNetwork
          .nodes(3)
          .outgoingMessages should contain theSameElementsAs List(messageCToB)
      }
    }

    describe("List constructor") {
      it("handles an empty list") {
        testNetworkState(0, List.empty).nodes shouldBe empty
      }

      it("handles a list of nodes") {
        testNetworkState(
          0,
          List(nodeA, nodeB, nodeC)
        ).nodes should contain theSameElementsAs Map(
          1 -> nodeA,
          2 -> nodeB,
          3 -> nodeC
        )
      }
    }
  }
}
