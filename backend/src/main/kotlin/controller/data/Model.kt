package controller.data


data class Edge(
        val start: Node,
        val end: Node,
        val attributes: Attributes
)

data class Node(
        val identifier: String,
        val packageIdentifier: String
)

data class Attributes(
        val couplingScore: Int
)

data class Graph(
        val relationships: List<Edge>
)
