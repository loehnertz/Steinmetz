package model.graph


data class EdgeAttributes(
    var couplingScore: Int = 1,
    var dynamicCouplingScore: Int = 0,
    var semanticCouplingScore: Int = 0,
    var logicalCouplingScore: Int = 0
) {
    companion object {
        fun mergeEdgeAttributes(vararg edgeAttributes: EdgeAttributes): EdgeAttributes {
            return EdgeAttributes(
                dynamicCouplingScore = edgeAttributes.sumBy { it.dynamicCouplingScore },
                semanticCouplingScore = edgeAttributes.sumBy { it.semanticCouplingScore },
                logicalCouplingScore = edgeAttributes.sumBy { it.logicalCouplingScore }
            )
        }
    }
}
