package controller.analysis.metrics.input

import model.graph.Edge
import model.graph.Graph
import model.metrics.InputQuality


class InputQualityAnalyzer(
        private val mergedStaticAndDynamicAnalysisGraph: Graph,
        private val dynamicAnalysisGraph: Graph,
        private val semanticAnalysisGraph: Graph,
        private val logicalAnalysisGraph: Graph
) {
    fun calculateInputQualityMetrics(): InputQuality {
        val dynamicAnalysisQuality: Int = calculateEdgeQuality(baseGraph = mergedStaticAndDynamicAnalysisGraph, comparisonGraph = dynamicAnalysisGraph)
        val semanticAnalysisQuality: Int = calculateEdgeQuality(baseGraph = mergedStaticAndDynamicAnalysisGraph, comparisonGraph = semanticAnalysisGraph)
        val logicalAnalysisQuality: Int = calculateEdgeQuality(baseGraph = mergedStaticAndDynamicAnalysisGraph, comparisonGraph = logicalAnalysisGraph)

        return InputQuality(
                dynamicAnalysis = dynamicAnalysisQuality,
                semanticAnalysis = semanticAnalysisQuality,
                logicalAnalysis = logicalAnalysisQuality
        )
    }

    private fun calculateEdgeQuality(baseGraph: Graph, comparisonGraph: Graph): Int {
        val intersectionCoveredUnits: Set<Edge> = baseGraph.edges.intersect(comparisonGraph.edges)
        val coveredUnitsDifference: Double = Math.abs(baseGraph.edges.size - intersectionCoveredUnits.size).toDouble()
        return (100 - ((coveredUnitsDifference / baseGraph.edges.size) * 100)).toInt()
    }
}
