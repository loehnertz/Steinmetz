package controller.data.extraction.graph

import model.graph.Attributes
import model.graph.Edge
import model.graph.Node
import model.neo4j.node.Unit
import model.neo4j.relationship.CallsRelationship


object GraphConverter {
    fun convertUnitListToRelationships(units: List<Unit>): ArrayList<Edge> {
        val relationships: MutableSet<CallsRelationship> = retrieveRelationships(units)
        return retrieveEdges(relationships)
    }

    @Suppress("SENSELESS_COMPARISON")
    private fun retrieveRelationships(units: List<Unit>): MutableSet<CallsRelationship> {
        val relationships: MutableSet<CallsRelationship> = mutableSetOf()

        for (unit in units) {
            if (unit.callerRelationships != null) unit.callerRelationships.forEach { if (!isSelfLooping(unit, it)) relationships.add(it) }
        }

        return relationships
    }

    private fun retrieveEdges(relationships: MutableSet<CallsRelationship>): ArrayList<Edge> {
        val edges: ArrayList<Edge> = arrayListOf()

        for (relationship in relationships) {
            val start = Node(identifier = relationship.caller.identifier, packageIdentifier = relationship.caller.packageIdentifier)
            val end = Node(identifier = relationship.callee.identifier, packageIdentifier = relationship.callee.packageIdentifier)
            val attributes = Attributes(couplingScore = relationship.couplingScore)

            edges.add(Edge(start = start, end = end, attributes = attributes))
        }

        return edges
    }

    private fun isSelfLooping(unit: Unit, relationship: CallsRelationship): Boolean {
        return relationship.callee.identifier == unit.identifier && relationship.callee.packageIdentifier == unit.packageIdentifier
    }
}