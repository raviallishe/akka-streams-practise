package src.playground

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

object BackPressureBasics extends App {

  implicit val system = ActorSystem("BackPressure")
  implicit val materializer = ActorMaterializer()

  val fastSource = Source(1 to 1000)
  val slowSink = Sink.foreach[Int]{
    x =>
      Thread.sleep(1000)
      println(x)
  }

  fastSource.to(slowSink)
//    .run()
  // not backpressure as both runs on single actor

  fastSource.async.to(slowSink)
  //  .run()
  // it is backpressure as fastSource and slowSink both runs on different actors

  val simpleFlow = Flow[Int].map{
    x =>
      println(s">>> Incoming $x")
      x+1
  }

  fastSource.async
    .via(simpleFlow).async
    .to(slowSink)
//    .run()
  /*Explaination
    reactions to backpressure (in order):
    - try to slow down if possible
    - buffer elements until there's more demand
    - drop down elements from the buffer if it overflows
    - tear/kill the whole stream (FAILURE)
   */

  val bufferedFlow = simpleFlow.buffer(10, overflowStrategy = OverflowStrategy.dropHead)
  fastSource.async
    .via(bufferedFlow).async
    .to(slowSink)
//    .run()

  /*
  Explaination:

  1-16: nobody is backpressured
  17-26: flow will buffer, flow will start dropping at the head
  26-1000: flow will always drop the oldest
    => 991-1000 => 992-1001 => sink
   */
  /*
  overflow strategies:
  - drop head -> oldest
  - drop tail -> newest
  - drop new -> exact element to be added = keeps the buffer
  - drop the entire buffer
  - backpressure signal
  - fail
   */

  //throttleing (manually applying backpressure)

  import scala.concurrent.duration._
  fastSource.throttle(5, 1 second).runWith(Sink.foreach(println))
}
