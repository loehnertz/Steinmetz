package model.graph

import model.graph.EdgeAttributes.Companion.mergeEdgeAttributes
import model.graph.UnitFootprint.Companion.mergeUnitFootprints
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KMutableProperty1


data class Graph(val nodes: MutableSet<Node> = mutableSetOf(), val edges: MutableSet<Edge> = mutableSetOf()) {
    private val logger: Logger = LoggerFactory.getLogger(Graph::class.java)

    init {
        inferNodesOutOfEdges()
    }

    fun clone(): Graph {
        return Graph(
            nodes = this.nodes.map { node -> node.copy() }.toMutableSet(),
            edges = this.edges.map { edge -> edge.copy() }.toMutableSet()
        )
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

    fun addOrIncrementEdgeAttribute(edge: Edge, edgeAttributesType: KMutableProperty1<EdgeAttributes, Int>) {
        val existingEdge: Edge = edges.find { it == edge } ?: edge.also { edges.add(it) }.also { inferNodesOutOfEdge(it) }
        edgeAttributesType.set(existingEdge.attributes, edgeAttributesType.get(existingEdge.attributes).inc())
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
        val nodeUnits: Set<Unit> = retrieveNodeUnits()
        val edgeUnits: Set<Unit> = retrieveEdgeUnits()
        val removedNodes: Boolean = nodes.retainAll { it.unit in edgeUnits }
        if (removedNodes) logger.info("Removed the nodes ${nodeUnits.minus(edgeUnits)} from the graph")
        check(retrieveNodeUnits().size == edgeUnits.size) { "The integrity check on the nodes and edges failed" }
    }

    private fun retrieveNodeUnits(): Set<Unit> = nodes.map { it.unit }.toSet()

    private fun retrieveEdgeUnits(): Set<Unit> = edges.map { it.start }.toSet().union(edges.map { it.end }.toSet())
}
