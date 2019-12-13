package src.part2

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object OperatorFusion extends App {

  implicit val system = ActorSystem("OperatorFusion")
  implicit val materializer = ActorMaterializer()

  val simpleSource = Source(1 to 100)
  val simpleFlow = Flow[Int].map(_ + 1)
  val simpleFlow2 = Flow[Int].map(_ * 10)
  val simpleSink = Sink.foreach(println)

  //this runs on the same actor and is slow
//  simpleSource.via(simpleFlow).via(simpleFlow2).to(simpleSink).run()
  //operator fusion

  //the above is same as below akka actor
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case x: Int => {
        //flow operations
        val x2 = x + 1
        val y = x * 10
        //sink operations
        println(y)
      }
    }
  }

  val simpleActor = system.actorOf(Props[SimpleActor])
//   (1 to 100).foreach(simpleActor ! _)

  //complex flows
  val complexFlow = Flow[Int].map{x =>
    Thread.sleep(1000)
    x + 1
  }

  val complexFlow2 = Flow[Int].map{ x =>
    Thread.sleep(1000)
    x * 10
  }

//  simpleSource.via(complexFlow).via(complexFlow2).to(simpleSink).run()

  //async boundary

  simpleSource.via(complexFlow).async // runs on one actor
    .via(complexFlow2).async   //runs on another actor
    .to(simpleSink).async    // runs on yet another actor
//    .run()


  //ordering gurantees

  Source(1 to 5)
    .map(x => {println(s"order A: $x"); x}).async
    .map(x => {println(s"order B: $x"); x}).async
    .map(x => {println(s"order C: $x"); x}).async
    .runWith(simpleSink)
}
