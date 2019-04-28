package com.quicks.remote.ws

sealed trait WsMessage


/* INCOMING MESSAGES */

sealed trait WsInMessage extends WsMessage

case class WsInTextMsg(msg: String) extends WsInMessage

object WsUnknownMsg extends WsInMessage


/* OUTGOING MESSAGES */

sealed trait WsOutMessage extends WsMessage

case class WsOutTextMsg(content: String) extends WsOutMessage
