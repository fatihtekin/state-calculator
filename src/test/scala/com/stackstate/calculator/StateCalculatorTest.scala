package com.stackstate.calculator

import com.stackstate.calculator.CommandLineRunner.{Config, args, parser}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.io.Source

class StateCalculatorTest extends WordSpec
  with BeforeAndAfterAll
  with Matchers {

  implicit val formats = DefaultFormats

  "StateCalculator is called" when {
    "Valid files given" in {
        test("src/test/resources/graph.json", "events.json", "expected_output.json")
    }

    "Valid file with loop given" in {
      test("src/test/resources/loopCase/graph.json", "loopCase/events.json", "loopCase/expected_output.json")
    }

    "Valid file with loop2 given" in {
      test("src/test/resources/loopCase/graph.json", "loopCase/events2.json", "loopCase/expected_output2.json")
    }

    "Valid file with bidirectional given" in {
      test("src/test/resources/bidirectionalCase/graph.json", "bidirectionalCase/events.json", "bidirectionalCase/expected_output.json")
    }

    "Valid files with bidirectional one after the other is given to clear all the warnings and alerts" in {
      val stateCalculator = test("src/test/resources/bidirectionalCase/graph.json", "bidirectionalCase/events.json", "bidirectionalCase/expected_output.json")
      val eventData = parse(Source.fromResource("bidirectionalCase/events2.json").bufferedReader()).extract[EventData]
      stateCalculator.updateGraphByEvents(eventData.events)
      stateCalculator.getGraphAsJson() shouldBe Source.fromResource("bidirectionalCase/expected_output2.json").mkString
    }

    "Valid files are given for commandlinerunner" in {
      CommandLineRunner.main(Array("-g", "src/test/resources/graph.json", "-e", "src/test/resources/events.json"))
    }

  }

  private def test(graphFile: String, eventFile: String, expectedOutputFile: String): StateCalculator = {
    val config = Config(graphFile, eventFile)
    val eventData = parse(Source.fromResource(config.eventsFile).bufferedReader()).extract[EventData]
    val stateCalculator = StateCalculator(config)
    stateCalculator.updateGraphByEvents(eventData.events)
    stateCalculator.getGraphAsJson() shouldBe Source.fromResource(expectedOutputFile).mkString
    stateCalculator
  }

}
