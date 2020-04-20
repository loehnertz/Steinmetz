package controller.analysis.extraction.graph

import model.graph.*
import model.neo4j.node.Unit
import model.neo4j.relationship.CallsRelationship


class GraphConverter(private val units: List<Unit>) {
    fun convertUnitListToGraph(): Graph {
        val relationships: MutableSet<CallsRelationship> = retrieveRelationships()
        val graph = Graph(edges = retrieveEdges(relationships).toMutableSet())
        graph.nodes.map { attachUnitFootprints(it) }.forEach { graph.addOrUpdateNode(it) }
        return graph
    }

    @Suppress("SENSELESS_COMPARISON")
    private fun retrieveRelationships(): MutableSet<CallsRelationship> {
        val relationships: MutableSet<CallsRelationship> = mutableSetOf()

        for (unit: Unit in units) {
            if (unit.callerRelationships != null) unit.callerRelationships.forEach { if (!isSelfLooping(unit, it)) relationships.add(it) }
        }

        return relationships
    }

    private fun retrieveEdges(relationships: MutableSet<CallsRelationship>): ArrayList<Edge> {
        val edges: ArrayList<Edge> = arrayListOf()

        for (relationship: CallsRelationship in relationships) {
            val start = model.graph.Unit(identifier = relationship.caller.identifier, packageIdentifier = relationship.caller.packageIdentifier)
            val end = model.graph.Unit(identifier = relationship.callee.identifier, packageIdentifier = relationship.callee.packageIdentifier)
            val attributes = EdgeAttributes(dynamicCouplingScore = relationship.dynamicCouplingScore, semanticCouplingScore = relationship.semanticCouplingScore, evolutionaryCouplingScore = relationship.evolutionaryCouplingScore)

            edges.add(Edge(start = start, end = end, attributes = attributes))
        }

        return edges
    }

    private fun attachUnitFootprints(node: Node): Node {
        val unitCharacterCount: Long = units.first { it.identifier == node.unit.identifier && it.packageIdentifier == node.unit.packageIdentifier }.size
        node.attributes.footprint = UnitFootprint(characters = unitCharacterCount)
        return node
    }

    private fun isSelfLooping(unit: Unit, relationship: CallsRelationship): Boolean {
        return relationship.callee.identifier == unit.identifier && relationship.callee.packageIdentifier == unit.packageIdentifier
    }
}
