package com.quicks.thing.remote

import akka.actor.{Actor, ActorLogging, Props}
import com.quicks.thing.remote.ws.{WebSocketAgent, WsInTextMsg}

class ConnectionManager(wsUrl: String) extends Actor with ActorLogging {

  val WS_WORKER: String = "webSocketWorker"

  override def preStart() {
    log.info("Connection manager started !")
    context.actorOf(Props(new WebSocketAgent(wsUrl)), WS_WORKER)
  }

  override def receive = {
    case WsInTextMsg(msg) => {
      log.info(s"Received web socket notification $msg")
    }
  }

}
