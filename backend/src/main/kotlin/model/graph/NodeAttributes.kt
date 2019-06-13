package model.graph


data class NodeAttributes(
        var cluster: Int? = null,
        var footprint: UnitFootprint? = null
)

data class UnitFootprint(
        val byteSize: Long
)
