package controller.analysis.clustering

import model.graph.EdgeAttributeWeights
import model.graph.EdgeAttributes


object EdgeWeightingFormulaCalculator {
    fun applyFormula(edgeAttributes: EdgeAttributes, edgeAttributeWeights: EdgeAttributeWeights): Int {
        val weightedStaticCouplingScore: Double = edgeAttributeWeights.staticCouplingScoreWeight * edgeAttributes.staticCouplingScore
        val weightedDynamicCouplingScore: Double = edgeAttributeWeights.dynamicCouplingScoreWeight * edgeAttributes.dynamicCouplingScore
        val weightedSemanticCouplingScore: Double = edgeAttributeWeights.semanticCouplingScoreWeight * edgeAttributes.semanticCouplingScore
        val weightedEvolutionaryCouplingScore: Double = edgeAttributeWeights.evolutionaryCouplingScoreWeight * edgeAttributes.evolutionaryCouplingScore

        val weightedCouplingScores: List<Double> = listOf(weightedStaticCouplingScore, weightedDynamicCouplingScore, weightedSemanticCouplingScore, weightedEvolutionaryCouplingScore)

        val couplingScore: Int = ((weightedCouplingScores.sum() / edgeAttributeWeights.sum()) * 100).toInt()
        return if (couplingScore == 0) {
            1  // Return 1 so that there are no edges that have a score of 0 which lets some graph clustering algorithms fail
        } else {
            couplingScore
        }
    }
}
