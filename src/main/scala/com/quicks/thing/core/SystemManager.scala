package com.quicks.thing.core

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.quicks.thing.remote.ConnectionManager

class SystemManager(wsUrl: String) extends Actor with ActorLogging {

  var connectionManager: ActorRef = _

  override def preStart() {
    log.info("System manager started !")
    connectionManager = context.actorOf(Props(new ConnectionManager(wsUrl)), "connectionManager")
  }

  override def receive = {
    case _ =>
  }

}
