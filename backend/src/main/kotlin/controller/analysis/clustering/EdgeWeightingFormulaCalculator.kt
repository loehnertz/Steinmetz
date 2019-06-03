package controller.analysis.clustering

import model.graph.EdgeAttributes


object EdgeWeightingFormulaCalculator {
    fun applyFormula(edgeAttributes: EdgeAttributes): Int {
        val weightedDynamicCouplingScore: Double = edgeAttributes.dynamicCouplingScore.toDouble()
        val weightedSemanticCouplingScore: Double = Math.ceil(edgeAttributes.semanticCouplingScore.toDouble() / 10)
        val weightedLogicalCouplingScore: Double = Math.ceil(edgeAttributes.logicalCouplingScore.toDouble() / 10)

        return (weightedDynamicCouplingScore + weightedSemanticCouplingScore + weightedLogicalCouplingScore).toInt() + 1
    }
}
