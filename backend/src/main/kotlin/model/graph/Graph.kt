package model.graph

import model.graph.EdgeAttributes.Companion.mergeEdgeAttributes
import model.graph.UnitFootprint.Companion.mergeUnitFootprints


data class Graph(val nodes: MutableSet<Node> = mutableSetOf(), val edges: MutableSet<Edge> = mutableSetOf()) {
    init {
        inferNodesOutOfEdges()
    }

    fun addOrUpdateNode(node: Node) {
        val existingNode: Node? = nodes.firstOrNull { it == node }
        nodes.removeIf { it == node }

        if (existingNode != null) {
            if (node.attributes.footprint == null && existingNode.attributes.footprint != null) {
                node.attributes.footprint = existingNode.attributes.footprint
            } else if (node.attributes.footprint != null && existingNode.attributes.footprint != null) {
                node.attributes.footprint = mergeUnitFootprints(node.attributes.footprint!!, existingNode.attributes.footprint!!)
            }
        }

        nodes.add(node)
    }

    fun addOrUpdateEdge(edge: Edge) {
        val existingEdge: Edge? = edges.find { it == edge }

        if (edges.removeIf { it == edge } && existingEdge != null) {
            val newEdge: Edge = mergeEqualEdges(existingEdge, edge)
            edges.add(newEdge)
            inferNodesOutOfEdge(newEdge)
        } else {
            edges.add(edge)
            inferNodesOutOfEdge(edge)
        }
    }

    fun updateEdge(edge: Edge) {
        val existingEdge: Edge = edges.find { it == edge } ?: return
        edges.remove(existingEdge)
        edges.add(mergeEqualEdges(existingEdge, edge))
    }

    fun findNodeByUnit(unit: Unit): Node? {
        return nodes.firstOrNull { it.unit == unit }
    }

    private fun mergeEqualEdges(firstEdge: Edge, secondEdge: Edge): Edge {
        require(firstEdge == secondEdge) { "The two passed edges are not equal" }
        return Edge(start = firstEdge.start, end = firstEdge.end, attributes = mergeEdgeAttributes(firstEdge.attributes, secondEdge.attributes))
    }

    private fun inferNodesOutOfEdge(edge: Edge) {
        addOrUpdateNode(Node(unit = edge.start))
        addOrUpdateNode(Node(unit = edge.end))
        ensureNodesAndEdgesIntegrity()
    }

    private fun inferNodesOutOfEdges() {
        for (edge: Edge in edges) {
            addOrUpdateNode(Node(unit = edge.start))
            addOrUpdateNode(Node(unit = edge.end))
        }
        ensureNodesAndEdgesIntegrity()
    }

    private fun ensureNodesAndEdgesIntegrity() {
        val nodeUnits: Set<Unit> = nodes.map { it.unit }.toSet()
        val edgeUnits: Set<Unit> = edges.map { it.start }.toSet().union(edges.map { it.end }.toSet())
        check(nodeUnits.size == edgeUnits.size) { "The integrity check on the nodes and edges failed" }
    }
}
