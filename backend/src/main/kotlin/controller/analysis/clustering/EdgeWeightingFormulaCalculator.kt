package controller.analysis.clustering

import model.graph.EdgeAttributes


object EdgeWeightingFormulaCalculator {
    fun applyFormula(edgeAttributes: EdgeAttributes): Int {
        val weightedDynamicCouplingScore = edgeAttributes.dynamicCouplingScore
        val weightedSemanticCouplingScore = edgeAttributes.semanticCouplingScore
        val weightedLogicalCouplingScore = edgeAttributes.logicalCouplingScore

        return (weightedDynamicCouplingScore + weightedSemanticCouplingScore + weightedLogicalCouplingScore) + 1
    }
}
