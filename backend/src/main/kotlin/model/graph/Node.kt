package model.graph


data class Node(
        val unit: Unit,
        val attributes: NodeAttributes = NodeAttributes()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        if (unit != other.unit) return false

        return true
    }

    override fun hashCode(): Int {
        return 31 * unit.hashCode()
    }
}

data class NodeAttributes(
        var cluster: Int? = null,
        var footprint: UnitFootprint? = null
)

data class UnitFootprint(
        val byteSize: Long
)
