package src.part4_techniques

import java.util.Date

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.Future

object IntegratingWithExternalServices extends App {

  implicit val system = ActorSystem("IntegratingWithExternalServices")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  def genExternalService[A,B](element: A): Future[B] = {
    ???
  }

  case class PagerEvent(application: String, description: String, date: Date)

  val eventSource = Source(List(
    PagerEvent("AkkaInfra", "infra broke", new Date()),
    PagerEvent("FrontEnd", "searchbar broke", new Date()),
    PagerEvent("AkkaInfra", "infra db broke", new Date())))

  object PagerService {
    private val engineers = List("ravi", "kumar", "kohli")
    private val emails = Map("ravi" -> "ravi@gmail.com", "kumar" -> "kumar@gmail.com", "kohli" -> "kohli@gmail.com")

    def processEvent(pagerEvent: PagerEvent) = Future {
      val engineerIndex = pagerEvent.date.toInstant.getEpochSecond / (24 * 3600) % engineers.length
      val engineer = engineers(engineerIndex.toInt)
      val email = emails(engineer)

      //page the engineer
      println(s"sending engineer $engineer a notification alert")
      email
    }
  }

  val pagedEngineerEmails = eventSource.mapAsync(parallelism = 2)(event => PagerService.processEvent(event))
  // gurantees the relative order but slow than mapAsyncUnordered method

  val pagedEmailSink = Sink.foreach[String](email => println(s"email is sent to $email"))

  pagedEngineerEmails.to(pagedEmailSink).run()
}


