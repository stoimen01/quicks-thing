package com.quicks.remote.ws

import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorLogging, ActorRef, Timers}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, OverflowStrategy}
import scala.concurrent.duration._

import scala.concurrent.Future

class WebSocketAgent(url: String) extends Actor with ActorLogging with Timers {

  private object WsReconnect
  private case class WsConnected(sender: ActorRef)
  private object WsClosed

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  implicit val system = context.system
  import system.dispatcher

  var senderActor: ActorRef = _

  override def preStart() {
    log.info("WebSocketWorker started !")
    startConnection()
  }

  override def receive = {

    case m: WsOutTextMsg =>
      if (senderActor != null) {
        senderActor ! outMsgOf(m)
        log.info("Ws message was sent !")
      } else {
        log.warning("Could not send ws message !")
      }

    case m: WsConnected =>
      log.info("Sender actor received !")
      senderActor = m.sender

    case WsClosed =>
      log.info("Setting ws reboot timer ...")
      timers.startSingleTimer(WsReconnect, WsReconnect, 3.second)

    case WsReconnect =>
      startConnection()
  }

  private def startConnection() {

    log.info("Starting ws connection !")

    val sink: Sink[Message, Future[Done]] =
      Sink.foreach(msg => context.parent ! inMsgOf(msg))

    val source = Source
      .actorRef[Message](10, OverflowStrategy.fail)
      .mapMaterializedValue { outActor =>
        self ! WsConnected(outActor)
        NotUsed
      }

    val flow = Flow.fromSinkAndSourceMat(sink, source)(Keep.left)

    val request = WebSocketRequest(url)

    val (upgradeResponse, closed) = Http().singleWebSocketRequest(request, flow)

    val connected = upgradeResponse.map { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) Done
      else throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
    }

    connected
      .onComplete(_.fold(
        ex => {
          log.error(ex,"Could not establish connection !")
          self ! WsClosed
        },
        _ => {
          log.info("Web socket connected !")
        }
      ))

    closed.onComplete(_.fold(
      ex => {
        log.error(ex, "Connection closed !")
        self ! WsClosed
      },
      _ => {
        log.error("Connection closed without error !")
        self ! WsClosed
      }
    ))

  }

  private def inMsgOf(msg: Message): WsInMessage = {
    msg match {
      case m: TextMessage => m match {
        case TextMessage.Strict(text) => WsInTextMsg(text)
        case TextMessage.Streamed(_) =>
          log.warning("Unknown msg received")
          WsUnknownMsg
      }
      case _: BinaryMessage =>
        log.warning("Unknown msg received")
        WsUnknownMsg
    }
  }

  private def outMsgOf(outMsg: WsOutMessage): Message = {
    outMsg match {
      case WsOutTextMsg(content) => TextMessage(content)
    }
  }

}
