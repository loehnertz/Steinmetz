package model.graph


class Graph(val nodes: MutableSet<Node> = mutableSetOf(), val edges: MutableSet<Edge>) {
    init {
        if (this.nodes.isEmpty()) inferNodesOutOfEdges()
    }

    fun updateNode(node: Node) {
        nodes.removeIf { it.identifier == node.identifier && it.packageIdentifier == node.packageIdentifier }
        nodes.add(node)
    }

    private fun inferNodesOutOfEdges() {
        for (edge in edges) {
            nodes.add(edge.start)
            nodes.add(edge.end)
        }
    }
}
