package com.stackstate.calculator

import com.stackstate.calculator.CommandLineRunner.Config
import com.stackstate.calculator.State.{Alert, Clear, NoData, Warning}
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
  *   - Breath First Search for alert and warning components
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
    traverseGraphToSetDerivedStates()
  }

  private def updateStatesOfComponent(c: Component): Unit = {
    c.own_state = c.check_states.values.max
    c.derived_state = if (c.own_state == Clear) State.NoData else c.own_state
  }

  def getGraphAsJson(): String = {
    val graph = data.graph.copy(collection.immutable.SortedSet[Component]() ++ data.graph.components)
    writePretty(data.copy(graph))(formats)
  }

  private def traverseGraphToSetDerivedStates(): Unit = {
    val warningComponents = data.graph.components.filter(_.own_state.in(Alert, Warning))
    val visitedSet = mutable.Set[String]()
    warningComponents.foreach(c => {
      visitedSet.add(c.id)
      var list: ListBuffer[Component] = ListBuffer[Component]()
      list += c
      while (list.nonEmpty) {
        val next = list.remove(0)
        next.dependency_of.flatMap(idComponentMap.get).foreach(dc =>
          if (!visitedSet.contains(dc.id)) {
            if (next.derived_state > dc.derived_state ) {
              dc.derived_state = next.derived_state
            }
            list += dc
            visitedSet.add(dc.id)
          }
        )
      }
    })
  }
}

object StateCalculator {

  case object StateSerializer extends CustomSerializer[State](_ => (
    {
      case JString(state) => State.withName(state)
      case JNull => null
    },
    {
      case state:State => JString(state.entryName)
    })
  )

  implicit val formats = DefaultFormats.skippingEmptyValues + StateSerializer

  def apply(config: Config): StateCalculator = {
    val graphData: Data = parse(Source.fromFile(config.graphFile).bufferedReader()).extract[Data]
    val idComponentMap: Map[String, Component] = graphData.graph.components.map(c => (c.id, c)).toMap
    new StateCalculator(graphData, idComponentMap)
  }

}