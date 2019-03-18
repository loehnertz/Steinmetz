package model.graph

data class Edge(
        val start: Node,
        val end: Node,
        val attributes: Attributes
)