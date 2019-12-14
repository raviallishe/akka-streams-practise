package src.part3_graphs

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, Zip}

object GraphBasics extends App {

  implicit val system = ActorSystem("GraphBasics")
  implicit val materializer = ActorMaterializer()

  val input = Source(1 to 100)
  val increment = Flow[Int].map{ x => x + 1}
  val multiply = Flow[Int].map{ x => x * 2}
  val output = Sink.foreach(println)

  val graph = RunnableGraph.fromGraph(
    GraphDSL.create() {implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._ //brings nice operators in to scope

      //step-2 add necessary components of this graph
      val broadcast = builder.add(Broadcast[Int](2))   //fan-out operator
      val zip = builder.add(Zip[Int, Int])     //fan-in operator

      //step-3 tying all the components
      input ~> broadcast

      broadcast.out(0) ~> increment ~> zip.in0
      broadcast.out(1) ~> multiply ~> zip.in1

      zip.out ~> output

      //step-4 return a closed shape

      ClosedShape
      //shape
    } // graph
  ) // runnable graph

  graph.run()
}
