package core

import test_utils.{BehaviorSpecUtil, UnitSpec}

class NetworkStateSpec extends UnitSpec {
  describe("NetworkState") {
    val emptyNetwork = NetworkState(1, Map.empty)

    val emptyContent = MessageContent("")
    val messageAToB = Message(MessageHeader(1, 1, 2, 3, 10), emptyContent)
    val messageAToC = Message(MessageHeader(4, 1, 3, 5, 10), emptyContent)
    val messageBToA = Message(MessageHeader(9, 2, 1, 4, 10), emptyContent)
    val messageCToB = Message(MessageHeader(6, 3, 2, 11, 16), emptyContent)
    val nodeA = Node(
      List.empty,
      NodeState(NodeHeader(1), List(messageAToB, messageAToC)),
      MessageQueue.empty
    )
    val nodeB = Node(
      List.empty,
      NodeState(NodeHeader(2), List(messageBToA)),
      MessageQueue.empty
    )
    val nodeC = Node(
      List(BehaviorSpecUtil.TestBehavior(messageCToB)),
      NodeState(NodeHeader(3), List.empty),
      MessageQueue.empty
    )
    val network = NetworkState(1, List(nodeA, nodeB, nodeC))
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
        NetworkState(0, List.empty).nodes shouldBe empty
      }

      it("handles a list of nodes") {
        NetworkState(
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
