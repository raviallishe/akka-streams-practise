package src.part4_techniques

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout

object IntegratingWithActors extends App {

  implicit val system = ActorSystem("IntegratingWithActors")
  implicit val materializer = ActorMaterializer()

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case str: String => {
        println(s"received string $str")
        s"$str $str"
      }
      case num: Int => {
        println(s"received number $num")
        num * 2
      }
      case _ =>
    }
  }

  import scala.concurrent.duration._
  implicit val timeout = Timeout(3 second)
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")
  val source = Source(1 to 10)

  val actorBasedFlow = Flow[Int].ask(parallelism = 5)(simpleActor)

  source.via(actorBasedFlow).to(Sink.foreach(println))
//    .run()

    /*
    Actor as a source
     */

  val actorPoweredSource = Source.actorRef[Int](bufferSize = 5, overflowStrategy = OverflowStrategy.dropHead)
  val materializedActorRef = actorPoweredSource.to(Sink.foreach(x => println(s"received $x")))
    .run()

  materializedActorRef ! 10
  //terminating the stream
  materializedActorRef ! akka.actor.Status.Success("complete")

  // Actor as Sink

  case object StreamInit
  case object StreamAck
  case object StreamComplete
  case class StreamFail(ex: Throwable)

  class SinkActor extends Actor with ActorLogging{
    override def receive: Receive = {
      case StreamInit => {
        log.info("Stream initialized")
        sender() ! StreamAck
      }
      case StreamComplete =>{
        log.info("Stream Completed")
        context.stop(self)
      }
      case StreamFail(ex) => {
        log.warning(s"Stream failed with Ex $ex")
      }
      case message: String => {
        log.info(s"received message: $message")
        sender() ! StreamAck
      }
    }
  }

  val sinkActor =  system.actorOf(Props[SinkActor], "sinkActor")

  val actorPoweredSink = Sink.actorRefWithAck[Int](
    sinkActor,
    StreamInit,
    StreamAck,
    StreamComplete,
    throwable => StreamFail(throwable))

  Source(1 to 10).to(actorPoweredSink).run()
}
