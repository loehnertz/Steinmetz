package model.graph


data class Node(
        val identifier: String,
        val packageIdentifier: String,
        val attributes: NodeAttributes = NodeAttributes()
)

data class NodeAttributes(
        var cluster: Int? = null
)
