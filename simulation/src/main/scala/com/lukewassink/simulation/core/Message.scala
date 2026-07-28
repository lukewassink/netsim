package com.lukewassink.simulation.core

import com.lukewassink.simulation.util.Time

// NOTE: a message ID uniquely identifies a message withing a node.
// The pair (message ID, node ID) is required to uniquely identify the message within the network.
case class MessageID(id: Int)

case class MessageHeader(
    id: MessageID,
    senderId: NodeID,
    receiverId: NodeID,
    sendTime: Time,
    deliveryTime: Option[Time]
)

// A unit of data that can be sent between nodes.
case class Message(header: MessageHeader, content: MessageContent)

case class MessageContent(stringContent: String)
