package com.stackstate.calculator

import com.stackstate.calculator.CommandLineRunner.Config
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import org.json4s._

import scala.collection.mutable.ListBuffer
import scala.io.Source
import org.json4s.jackson.Serialization.writePretty

import scala.collection.mutable

/**
  * The main logic is
  *   - Prepare a map of components by their ids
  *   - Set derived/own states of the components
  *   - Sort events by timestamp and update the states by the events
  *   - Breath First Search for alert components so that we wont miss components because of early visiting of no_data/clear/warning components
  *   - Breath First Search for alert components so that we wont miss components because of early visiting of no_data/clear components
  */
class StateCalculator(val data: Data, val idComponentMap: Map[String, Component]) {

  import StateCalculator._

  def updateGraphByEvents(events: List[Event]): Unit = {
    data.graph.components.foreach(updateStatesOfComponent)
    events.sortBy(_.timestamp.toLong)
      .foreach(e => idComponentMap.get(e.component)
        .foreach(c => {
          c.check_states.put(e.check_state, e.state)
          updateStatesOfComponent(c)
        }))
    bfsGraph(alert)
    bfsGraph(warning)
  }

  def getGraphAsJson(): String = {
    val graph = data.graph.copy(collection.immutable.SortedSet[Component]() ++ data.graph.components)
    writePretty(data.copy(graph))(formats)
  }

  private def bfsGraph(status: String): Unit = {
    val warningComponents = data.graph.components.filter(_.own_state == status)
    val visitedSet = mutable.Set[String]()
    warningComponents.foreach(c => {
      visitedSet.add(c.id)
      var list: ListBuffer[Component] = ListBuffer[Component]()
      list += c
      while (list.nonEmpty) {
        val next = list.remove(0)
        next.dependency_of.flatMap(idComponentMap.get).foreach(dc =>
          if (!visitedSet.contains(dc.id)) {
            if (next.derived_state == alert) {
              dc.derived_state = alert
            } else if (next.derived_state == warning && dc.derived_state != alert) {
              dc.derived_state = warning
            }
            list += dc
            visitedSet.add(dc.id)
          }
        )
      }
    })
  }

  private def updateStatesOfComponent(c: Component): Unit = {
    c.own_state = no_data
    c.derived_state = no_data
    if (c.check_states.values.exists(cs => cs == alert)) {
      c.own_state = alert
      c.derived_state = alert
    } else if (c.check_states.values.exists(cs => cs == warning)) {
      c.own_state = warning
      c.derived_state = warning
    } else if (c.check_states.values.exists(cs => cs == clear)) {
      c.own_state = clear
    }
  }
}

object StateCalculator {

  val alert = "alert"
  val warning = "warning"
  val clear = "clear"
  val no_data = "no_data"

  implicit val formats = DefaultFormats.skippingEmptyValues

  def apply(config: Config): StateCalculator = {
    val graphData: Data = parse(Source.fromFile(config.graphFile).bufferedReader()).extract[Data]
    val idComponentMap: Map[String, Component] = graphData.graph.components.map(c => (c.id, c)).toMap
    new StateCalculator(graphData, idComponentMap)
  }

}