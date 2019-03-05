package controller.data

import model.graph.node.Unit
import model.graph.relationship.CallsRelationship


object GraphExtractor {
    fun convertUnitListToSerializable(units: List<Unit>): ArrayList<Edge> {
        val relationships: MutableSet<CallsRelationship> = retrieveRelationships(units)
        return retrieveEdges(relationships)
    }

    @Suppress("SENSELESS_COMPARISON")
    private fun retrieveRelationships(units: List<Unit>): MutableSet<CallsRelationship> {
        val relationships: MutableSet<CallsRelationship> = mutableSetOf()

        for (unit in units) {
            if (unit.callerRelationships != null) {
                unit.callerRelationships.forEach { relationships.add(it) }
            }
        }

        return relationships
    }

    private fun retrieveEdges(relationships: MutableSet<CallsRelationship>): ArrayList<Edge> {
        val edges: ArrayList<Edge> = arrayListOf()

        for (relationship in relationships) {
            val start = Node(identifier = relationship.caller.identifier, packageIdentifier = relationship.caller.packageIdentifier)
            val end = Node(identifier = relationship.callee.identifier, packageIdentifier = relationship.callee.packageIdentifier)
            val attributes = Attributes(couplingScore = relationship.couplingScore)

            val edge = Edge(start = start, end = end, attributes = attributes)

            edges.add(edge)
        }

        return edges
    }
}
