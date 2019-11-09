package controller.analysis.clustering

import model.graph.EdgeAttributeWeights
import model.graph.EdgeAttributes


object EdgeWeightingFormulaCalculator {
    fun applyFormula(edgeAttributes: EdgeAttributes, edgeAttributeWeights: EdgeAttributeWeights): Int {
        val weightedDynamicCouplingScore: Int = edgeAttributeWeights.dynamicCouplingScoreWeight * edgeAttributes.dynamicCouplingScore
        val weightedSemanticCouplingScore: Int = edgeAttributeWeights.semanticCouplingScoreWeight * edgeAttributes.semanticCouplingScore
        val weightedEvolutionaryCouplingScore: Int = edgeAttributeWeights.evolutionaryCouplingScoreWeight * edgeAttributes.evolutionaryCouplingScore

        val weightedCouplingScores: List<Int> = listOf(weightedDynamicCouplingScore, weightedSemanticCouplingScore, weightedEvolutionaryCouplingScore)

        // Add 1 to the final coupling score so that there are no edges that have a score of 0 which let's some graph clustering algorithms fail
        return 1 + (weightedCouplingScores.sum().toDouble() / sumEdgeWeightAttributes(edgeAttributeWeights)).toInt()
    }

    private fun sumEdgeWeightAttributes(edgeAttributeWeights: EdgeAttributeWeights): Int {
        return (edgeAttributeWeights.dynamicCouplingScoreWeight + edgeAttributeWeights.semanticCouplingScoreWeight + edgeAttributeWeights.evolutionaryCouplingScoreWeight)
    }
}
