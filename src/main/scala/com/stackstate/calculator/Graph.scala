package com.stackstate.calculator

import scala.collection.mutable.Map

case class Data(graph: Graph)

case class Graph(components: Set[Component])

case class Component (
  id: String,
  var own_state: String = "no_data",
  var derived_state: String = "no_data",
  check_states: Map[String, String],
  depends_on: Set[String],
  dependency_of: Set[String]) extends Ordered[Component] {
  def compare(that: Component): Int = this.id.compareTo(that.id)
}

case class EventData(events: List[Event])

case class Event (timestamp: String, component: String, check_state: String, state: String)