package controller.analysis.clustering

import model.graph.EdgeAttributeWeights
import model.graph.EdgeAttributes


object EdgeWeightingFormulaCalculator {
    fun applyFormula(edgeAttributes: EdgeAttributes, edgeAttributeWeights: EdgeAttributeWeights): Int {
        val weightedDynamicCouplingScore: Pair<Int, Int> = Pair(edgeAttributeWeights.dynamicCouplingScoreWeight, edgeAttributes.dynamicCouplingScore)
        val weightedSemanticCouplingScore: Pair<Int, Int> = Pair(edgeAttributeWeights.semanticCouplingScoreWeight, edgeAttributes.semanticCouplingScore)
        val weightedLogicalCouplingScore: Pair<Int, Int> = Pair(edgeAttributeWeights.logicalCouplingScoreWeight, edgeAttributes.logicalCouplingScore)

        // Filter out the non-zero scores as the final, weighted coupling score is otherwise skewed
        val nonZeroCouplingScores: List<Pair<Int, Int>> = listOf(weightedDynamicCouplingScore, weightedSemanticCouplingScore, weightedLogicalCouplingScore).filter { it.second != 0 }

        return (nonZeroCouplingScores.sumBy { it.first * it.second }.toDouble() / nonZeroCouplingScores.sumBy { it.first }).toInt()
    }
}
