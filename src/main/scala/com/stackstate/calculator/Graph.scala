package com.stackstate.calculator

import com.stackstate.calculator.State.NoData

import scala.collection.mutable.Map
import enumeratum._
import org.json4s
import org.json4s.{CustomSerializer, JNull, JString}

case class Data(graph: Graph)

case class Graph(components: Set[Component])

case class Component (
  id: String,
  var own_state: State = NoData,
  var derived_state: State = NoData,
  check_states: Map[String, State],
  depends_on: Set[String],
  dependency_of: Set[String]) extends Ordered[Component] {
  def compare(that: Component): Int = this.id.compareTo(that.id)
}

sealed abstract class State (override val entryName: String, val order: Int) extends EnumEntry with Ordered[State] {
  def compare(that: State) = this.order - that.order
}

object State extends Enum[State]  {
  val values = findValues
  case object NoData extends State ("no_data", 1)
  case object Clear extends State ("clear", 2)
  case object Warning extends State ("warning", 3)
  case object Alert extends State ("alert", 4)
}

case class EventData(events: List[Event])

case class Event (timestamp: String, component: String, check_state: String, state: State)

case object StateSerializer extends CustomSerializer[State](_ => (
  {
    case JString(state) => State.withName(state)
    case JNull => null
  },
  {
    case state:State => JString(state.entryName)
  })
)