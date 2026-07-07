# Netsim
Luke Wassink

Netsim is a purely functional distributed network simulator written in scala.
It includes:

* The simulator which generates successive networks states as a pure function
  of the current network state.
* The runner, which generates the initial state based on a config and runs the
  simulator.
* The storage handler, which persists simulation historys to disk and loads
  them.
* The visualizer, which provides a simple in-browser simulation of the network
  activity.
