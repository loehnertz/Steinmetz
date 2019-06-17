package controller.analysis.clustering

import model.graph.EdgeAttributeWeights
import model.graph.EdgeAttributes


object EdgeWeightingFormulaCalculator {
    fun applyFormula(edgeAttributes: EdgeAttributes, edgeAttributeWeights: EdgeAttributeWeights): Int {
        val weightedDynamicCouplingScore: Int = edgeAttributeWeights.dynamicCouplingScoreWeight * edgeAttributes.dynamicCouplingScore
        val weightedSemanticCouplingScore: Int = edgeAttributeWeights.semanticCouplingScoreWeight * edgeAttributes.semanticCouplingScore
        val weightedLogicalCouplingScore: Int = edgeAttributeWeights.logicalCouplingScoreWeight * edgeAttributes.logicalCouplingScore

        val weightedCouplingScores: List<Int> = listOf(weightedDynamicCouplingScore, weightedSemanticCouplingScore, weightedLogicalCouplingScore)

        return (weightedCouplingScores.sum().toDouble() / sumEdgeWeightAttributes(edgeAttributeWeights)).toInt()
    }

    private fun sumEdgeWeightAttributes(edgeAttributeWeights: EdgeAttributeWeights): Int {
        return (edgeAttributeWeights.dynamicCouplingScoreWeight + edgeAttributeWeights.semanticCouplingScoreWeight + edgeAttributeWeights.logicalCouplingScoreWeight)
    }
}
