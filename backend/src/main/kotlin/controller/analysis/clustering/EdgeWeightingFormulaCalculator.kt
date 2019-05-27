package controller.analysis.clustering

import model.graph.EdgeAttributes


object EdgeWeightingFormulaCalculator {
    fun applyFormula(edgeAttributes: EdgeAttributes): Int {
        val weightedDynamicCouplingScore: Double = edgeAttributes.dynamicCouplingScore.toDouble()
        val weightedLogicalCouplingScore: Double = Math.ceil(edgeAttributes.logicalCouplingScore.toDouble() / 10)
        return (weightedDynamicCouplingScore + weightedLogicalCouplingScore).toInt() + 1
    }
}
