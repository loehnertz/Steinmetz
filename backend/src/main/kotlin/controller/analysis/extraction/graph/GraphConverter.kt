package controller.analysis.extraction.graph

import model.graph.Edge
import model.graph.EdgeAttributes
import model.neo4j.node.Unit
import model.neo4j.relationship.CallsRelationship


class GraphConverter(private val units: List<Unit>) {
    fun convertUnitListToRelationships(): ArrayList<Edge> {
        val relationships: MutableSet<CallsRelationship> = retrieveRelationships()
        return retrieveEdges(relationships)
    }

    @Suppress("SENSELESS_COMPARISON")
    private fun retrieveRelationships(): MutableSet<CallsRelationship> {
        val relationships: MutableSet<CallsRelationship> = mutableSetOf()

        for (unit in units) {
            if (unit.callerRelationships != null) unit.callerRelationships.forEach { if (!isSelfLooping(unit, it)) relationships.add(it) }
        }

        return relationships
    }

    private fun retrieveEdges(relationships: MutableSet<CallsRelationship>): ArrayList<Edge> {
        val edges: ArrayList<Edge> = arrayListOf()

        for (relationship in relationships) {
            val start = model.graph.Unit(identifier = relationship.caller.identifier, packageIdentifier = relationship.caller.packageIdentifier)
            val end = model.graph.Unit(identifier = relationship.callee.identifier, packageIdentifier = relationship.callee.packageIdentifier)
            val attributes = EdgeAttributes(couplingScore = relationship.couplingScore)

            edges.add(Edge(start = start, end = end, attributes = attributes))
        }

        return edges
    }

    private fun isSelfLooping(unit: Unit, relationship: CallsRelationship): Boolean {
        return relationship.callee.identifier == unit.identifier && relationship.callee.packageIdentifier == unit.packageIdentifier
    }
}
