package model.graph


data class NodeAttributes(
        var cluster: Int? = null,
        var footprint: UnitFootprint? = null
)

data class UnitFootprint(
        val byteSize: Long
) {
    companion object {
        fun mergeUnitFootprints(vararg unitFootprints: UnitFootprint): UnitFootprint {
            var byteSizeSum = 0L
            unitFootprints.forEach { byteSizeSum += it.byteSize }

            return UnitFootprint(
                    byteSize = byteSizeSum
            )
        }
    }
}
