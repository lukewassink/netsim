package simulation

case class NetworkState(time: Int, nodes: Map[Int, Node]):

  def nextState(): NetworkState = {
    return NetworkState(0, Map.empty[Int, Node])
  }
