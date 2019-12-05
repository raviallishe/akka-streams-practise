package src.playground

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Basics extends App {

  implicit val system = ActorSystem("Basics")
  implicit val materializer = ActorMaterializer()

  val source = Source(1 to 5)
  val sink = Sink.foreach(println)
  source.to(sink).run()

  val flow = Flow[Int].map(no => no * 2)

  source.via(flow).to(sink).run()

  // nulls not allowed
//  val notAllowed = Source.single[String](null)
//  notAllowed.to(sink).run()
  //use options instead

  //different types of sources
  val finiteSource = Source.single(1)
  val anotherFiniteSource = Source(List(1,2,3))
  val emptySource = Source.empty[Int]
  val infiniteSource = Source(Stream.from(1))
  val futureSource = Source.fromFuture(Future(33))

// different types of sinks
  val theMostBoringSink = Sink.ignore
  val foreachSink = Sink.foreach[String](println)
  val headSink = Sink.head[Int] //retrieves head and then closes the stream
  val foldSink = Sink.fold[Int, Int](0)((a,b) => (a + b))

//flow
  val mapFlow = Flow[Int].map(x => x * 2)
  val takeFlow = Flow[Int].take(3)
  // drop, filter but not flatMap


  //source -> flow -> flow -> sink
  val doubleFlowProcess = Source(1 to 5).via(Flow[Int].map(x => x * 2)).via(Flow[Int].map(x => x - 1)).to(Sink.foreach(println))
  val doubleFlowProcess2 = Source(1 to 5).via(mapFlow).via(takeFlow).to(Sink.foreach(println))
  doubleFlowProcess.run()

  //syntactic sugars
  val mapSource = Source(1 to 5).map(x => x * 2)
  //run streams directly
  mapSource.runForeach(println)


    //Excercise:

  val nameSource = Source(List("ravi", "Gunjan", "akshay", "kumar"))

  nameSource.via(Flow[String].filter(name => name.length < 5)).take(1).to(Sink.foreach(println)).run()






}
