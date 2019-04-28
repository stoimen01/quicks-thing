package com.quicks

import akka.actor.{ActorSystem, Props}
import com.quicks.core.SystemManager

import scala.io.StdIn

object Quicks {

  def main(args: Array[String]) {

    val system = ActorSystem("quicksSystem")

    system.actorOf(Props(new SystemManager("ws://127.0.0.1:8080/ws")), "systemManager")

    println(">>> Press ENTER to exit <<<")
    try StdIn.readLine()
    finally system.terminate()

  }

}

