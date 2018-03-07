package com.stackstate.calculator

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import scopt.OptionParser

import scala.io.Source
/**
  * Main method entry for the graph calculator
  */
object CommandLineRunner extends App {

  implicit val formats = DefaultFormats
  //Commandline params to the case class
  case class Config(graphFile: String = null, eventsFile: String = null)

  /**
    * Command line options to Config with validations
    */
  val parser: OptionParser[Config] = new scopt.OptionParser[Config]("StackSafe") {
    head("Graph Calculator")
    opt[String]('g', "graphFile").validate(x => if (new java.io.File(x).exists) success else failure(s"$x graph file does not exists"))
      .required() action { (x, g) => g.copy(graphFile = x) } text "The canonical name of the graph file"

    opt[String]('e', "eventsFile").validate(x => if (new java.io.File(x).exists) success else failure(s"$x canonical file does not exists"))
      .required() action { (x, e) => e.copy(eventsFile = x) } text "The canonical name of the events file"
  }
  parser.parse(args, Config()).foreach(config => {
      val eventData: EventData = parse(Source.fromFile(config.eventsFile).bufferedReader()).extract[EventData]
      val componentCalculator: StateCalculator = StateCalculator(config)
      componentCalculator.updateGraphByEvents(eventData.events)
      println(componentCalculator.getGraphAsJson())
    }
  )

}
