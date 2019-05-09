package model.graph

import model.graph.EdgeAttributes.Companion.mergeEdgeAttributes


class Graph(val nodes: MutableSet<Node> = mutableSetOf(), val edges: MutableSet<Edge> = mutableSetOf()) {
    init {
        if (this.nodes.isEmpty()) inferNodesOutOfEdges()
    }

    fun addOrUpdateNode(node: Node) {
        nodes.removeIf { it == node }
        nodes.add(node)
    }

    fun addOrUpdateEdge(edge: Edge) {
        val existingEdge: Edge? = edges.find { it == edge }
        if (edges.removeIf { it == edge } && existingEdge != null) {
            edges.add(mergeEqualEdges(existingEdge, edge))
        } else {
            edges.add(edge)
        }
        inferNodesOutOfEdges()
    }

    private fun mergeEqualEdges(firstEdge: Edge, secondEdge: Edge): Edge {
        if (firstEdge != secondEdge) throw IllegalArgumentException("The two passed edges are not equal")
        return Edge(start = firstEdge.start, end = firstEdge.end, attributes = mergeEdgeAttributes(firstEdge.attributes, secondEdge.attributes))
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
        if (nodeUnits.size != edgeUnits.size) throw IllegalStateException("The integrity check on the nodes and edges failed")
    }
}
