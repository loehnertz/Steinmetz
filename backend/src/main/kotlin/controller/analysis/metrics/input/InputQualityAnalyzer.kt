package controller.analysis.metrics.input

import model.graph.Edge
import model.graph.Graph
import model.metrics.InputQuality


class InputQualityAnalyzer(
    private val mergedStaticAndDynamicAnalysisGraph: Graph,
    private val dynamicAnalysisGraph: Graph,
    private val semanticAnalysisGraph: Graph,
    private val evolutionaryAnalysisGraph: Graph
) {
    fun calculateInputQualityMetrics(): InputQuality {
        val dynamicAnalysisQuality: Int = calculateEdgeQuality(baseGraph = mergedStaticAndDynamicAnalysisGraph, comparisonGraph = dynamicAnalysisGraph)
        val semanticAnalysisQuality: Int = calculateEdgeQuality(baseGraph = mergedStaticAndDynamicAnalysisGraph, comparisonGraph = semanticAnalysisGraph)
        val evolutionaryAnalysisQuality: Int = calculateEdgeQuality(baseGraph = mergedStaticAndDynamicAnalysisGraph, comparisonGraph = evolutionaryAnalysisGraph)

        return InputQuality(
            dynamicAnalysis = dynamicAnalysisQuality,
            semanticAnalysis = semanticAnalysisQuality,
            evolutionaryAnalysis = evolutionaryAnalysisQuality
        )
    }

    private fun calculateEdgeQuality(baseGraph: Graph, comparisonGraph: Graph): Int {
        val intersectionCoveredUnits: Set<Edge> = baseGraph.edges intersect comparisonGraph.edges
        return ((intersectionCoveredUnits.size.toDouble() / baseGraph.edges.size.toDouble()) * 100).toInt()
    }
}
