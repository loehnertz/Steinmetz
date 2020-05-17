package controller.analysis.clustering

import model.graph.EdgeAttributeWeights
import model.graph.EdgeAttributes


object EdgeWeightingFormulaCalculator {
    fun applyFormula(edgeAttributes: EdgeAttributes, edgeAttributeWeights: EdgeAttributeWeights): Int {
        val weightedStaticCouplingScore: Int = edgeAttributeWeights.staticCouplingScoreWeight * edgeAttributes.staticCouplingScore
        val weightedDynamicCouplingScore: Int = edgeAttributeWeights.dynamicCouplingScoreWeight * edgeAttributes.dynamicCouplingScore
        val weightedSemanticCouplingScore: Int = edgeAttributeWeights.semanticCouplingScoreWeight * edgeAttributes.semanticCouplingScore
        val weightedEvolutionaryCouplingScore: Int = edgeAttributeWeights.evolutionaryCouplingScoreWeight * edgeAttributes.evolutionaryCouplingScore

        val weightedCouplingScores: List<Int> = listOf(weightedStaticCouplingScore, weightedDynamicCouplingScore, weightedSemanticCouplingScore, weightedEvolutionaryCouplingScore)

        val couplingScore: Int = (weightedCouplingScores.sum().toDouble() / sumEdgeWeightAttributes(edgeAttributeWeights)).toInt()
        return if (couplingScore == 0) {
            1  // Return 1 so that there are no edges that have a score of 0 which lets some graph clustering algorithms fail
        } else {
            couplingScore
        }
    }

    private fun sumEdgeWeightAttributes(edgeAttributeWeights: EdgeAttributeWeights): Int {
        return (edgeAttributeWeights.staticCouplingScoreWeight + edgeAttributeWeights.dynamicCouplingScoreWeight + edgeAttributeWeights.semanticCouplingScoreWeight + edgeAttributeWeights.evolutionaryCouplingScoreWeight)
    }
}
