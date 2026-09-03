# Netsim
Luke Wassink

Netsim is a functional simulator for distributed systems written in scala.

## Features

Netsim is:

- **Deterministic and functional** The network state evolves in discrete steps.
    At each step, the state is a deterministic, pure function of the previous
    state. This makes it trivial to rebuild the network history given the
    initial state.
- **Configurable** Set up and run a simulation by configuring it in
    [HOCON](https://github.com/lightbend/config/blob/main/HOCON.md) without
    writing a single line of code. Configs are validated to produce useful error
    messages if you misconfigure.
- **Easy to visualize** Run the web-based visualizer to watch nodes send
    messages back and forth. Update the config to run new simulations right in
    the UI. Play, pause, and scrub through the simulation history.
- **Modular** Want to add something the simulator doesn't support? Write a few
    isolated chunks of code to add new logic for nodes, for message handling,
    etc.

## Primatives

The core primitives that simulations are built on:

- **Ticks** represent discrete moments in time. The simulation runs by
    calculating the state of the network at the next tick based on the state at
    the current tick.
- **Nodes** represent individual devices in a networked system. They contain
    logic describing their behavior and they can store state.
- **Messages** are inert chunks of data that can be sent from one node to
    another.
- **Behaviors** are composable units of logic that can be added to nodes. They
    send and respond to messages and update the node state and their own state.
- **Interceptors** intercept messages and process them before they are
    delivered. They allow the simulation to model message dropping, random
    latency, etc.

## Usage

There are a few steps to run the simulator for yourself.

1. Install [sbt](https://github.com/sbt/sbt), [npm](https://www.npmjs.com/), and [Vite](https://vite.dev/).
1. Run `sbt`, then run `fastLinkJS` to compile the javascript code.
1. From inside the `web-client` directory, run `npm run dev` to start Vite.
1. Open the localhost link that Vite returns to play with the simulator.

NetSim is provided under the [MIT license](https://github.com/lukewassink/netsim/blob/main/LICENSE.md).
