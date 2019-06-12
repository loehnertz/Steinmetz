package controller.analysis.metrics.input

import controller.analysis.metrics.MetricsManager
import model.graph.Edge
import model.graph.Graph
import model.graph.Unit
import model.metrics.InputQuality


class InputQualityAnalyzer(
        private val staticAnalysisGraph: Graph,
        private val dynamicAnalysisGraph: Graph,
        private val mergedStaticAndDynamicAnalysisGraph: Graph,
        private val semanticAnalysisGraph: Graph,
        private val logicalAnalysisGraph: Graph
) : MetricsManager() {
    fun calculateInputQualityMetrics(): InputQuality {
        val dynamicAnalysisQuality: Int = calculateNodeQuality(baseGraph = staticAnalysisGraph, comparisonGraph = dynamicAnalysisGraph)
        val semanticAnalysisQuality: Int = calculateEdgeQuality(baseGraph = mergedStaticAndDynamicAnalysisGraph, comparisonGraph = semanticAnalysisGraph)
        val logicalAnalysisQuality: Int = calculateEdgeQuality(baseGraph = mergedStaticAndDynamicAnalysisGraph, comparisonGraph = logicalAnalysisGraph)

        return InputQuality(
                dynamicAnalysis = dynamicAnalysisQuality,
                semanticAnalysis = semanticAnalysisQuality,
                logicalAnalysis = logicalAnalysisQuality
        )
    }

    private fun calculateNodeQuality(baseGraph: Graph, comparisonGraph: Graph): Int {
        val baseGraphCoveredUnits: Set<Unit> = baseGraph.nodes.map { it.unit }.toSet()
        val comparionGraphCoveredUnits: Set<Unit> = comparisonGraph.nodes.map { it.unit }.toSet()
        val intersectionCoveredUnits: Set<Unit> = baseGraphCoveredUnits.intersect(comparionGraphCoveredUnits)

        val coveredUnitsDifference: Double = Math.abs(baseGraphCoveredUnits.size - intersectionCoveredUnits.size).toDouble()
        return (100 - ((coveredUnitsDifference / baseGraphCoveredUnits.size) * 100)).toInt()
    }

    private fun calculateEdgeQuality(baseGraph: Graph, comparisonGraph: Graph): Int {
        val intersectionCoveredUnits: Set<Edge> = baseGraph.edges.intersect(comparisonGraph.edges)

        val coveredUnitsDifference: Double = Math.abs(baseGraph.edges.size - intersectionCoveredUnits.size).toDouble()
        return (100 - ((coveredUnitsDifference / baseGraph.edges.size) * 100)).toInt()
    }
}
