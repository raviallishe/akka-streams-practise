package src.part2

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import akka.{Done, NotUsed}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MaterializingStreams extends App {

  implicit val system = ActorSystem("MaterializingStreams")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val simpleGraph: Source[Int, NotUsed] = Source(1 to 10)
  val simpleMaterializedValue: RunnableGraph[NotUsed] = simpleGraph.to(Sink.foreach(println)) //NotUsed

  val sink: Sink[Int, Future[Int]] = Sink.reduce[Int]((a, b) => a + b)
  val sumFuture: Future[Int] = simpleGraph.runWith(sink)
  sumFuture.onComplete {
    case Success(value) => println(s"the sum of all values is: $value")
    case Failure(exception) => println("failed")
  }

  //choosing materialized value

  val source: Source[Int, NotUsed] = Source(1 to 10)
  val flow: Flow[Int, Int, NotUsed] = Flow[Int].map(x => x +1)
  val sinkGraph: Sink[Any, Future[Done]] = Sink.foreach(println)

  val value: RunnableGraph[NotUsed] = source.via(flow).to(sinkGraph)
  val materializedValue: RunnableGraph[Future[Done]] = source.viaMat(flow)(Keep.right).toMat(sinkGraph)(Keep.right)

  materializedValue.run().onComplete{
    case Success(_) => println("Stream processing completed")
    case Failure(ex) => println(s"failed processing: $ex")
  }


  //Excercise

  val exGraph = Source(1 to 10).toMat(Sink.last)(Keep.right).run()

  exGraph.onComplete{
    case Success(elem) => println("last element is: "+ elem)
    case Failure(exception) => println("didn't receive last element")
  }

  val wordSource: Source[String, NotUsed] = Source.single("Hello developers")
  val wordSink: Sink[String, Future[Int]] = Sink.fold[Int, String](0)((current, sentences) => current + sentences.split(" ").length)
  val wordFlow: Flow[String, Int, NotUsed] = Flow[String].fold(0)((current, sentences) => current + sentences.split(" ").length)

  wordSource.via(wordFlow).toMat(Sink.head)(Keep.right).run()

  val wordGraph: Future[Int] = wordSource.toMat(wordSink)(Keep.right).run()

  wordGraph.onComplete{
    case Success(count) => println(s"word count are $count")
    case Failure(ex) => println("word count failed")
  }




}
