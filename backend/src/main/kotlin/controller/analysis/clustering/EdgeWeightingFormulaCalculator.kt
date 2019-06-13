package controller.analysis.clustering

import model.graph.EdgeAttributeWeights
import model.graph.EdgeAttributes


object EdgeWeightingFormulaCalculator {
    fun applyFormula(edgeAttributes: EdgeAttributes, edgeAttributeWeights: EdgeAttributeWeights): Int {
        val weightedDynamicCouplingScore: Int = (edgeAttributeWeights.dynamicCouplingScoreWeight * edgeAttributes.dynamicCouplingScore)
        val weightedSemanticCouplingScore: Int = (edgeAttributeWeights.semanticCouplingScoreWeight * edgeAttributes.semanticCouplingScore)
        val weightedLogicalCouplingScore: Int = (edgeAttributeWeights.logicalCouplingScoreWeight * edgeAttributes.logicalCouplingScore)

        // Add 1 to the final coupling score so that there are no edges that have a score of 0 which let's some graph clustering algorithms fail
        return 1 + ((weightedDynamicCouplingScore + weightedSemanticCouplingScore + weightedLogicalCouplingScore).toDouble() / edgeAttributeWeights.calculateWeightSum()).toInt()
    }
}
