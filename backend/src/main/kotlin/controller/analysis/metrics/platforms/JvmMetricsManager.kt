package controller.analysis.metrics.platforms

import controller.analysis.metrics.clustering.ClusteringQuality
import controller.analysis.metrics.input.InputQuality
import controller.analysis.metrics.input.platforms.jvm.JvmInputQualityAnalyzer
import model.graph.Edge
import model.graph.Graph
import model.graph.Node
import model.graph.Unit


object JvmMetricsManager {
    fun calculateInputMetrics(staticAnalysisGraph: Graph, dynamicAnalysisGraph: Graph): InputQuality {
        return JvmInputQualityAnalyzer(staticAnalysisGraph, dynamicAnalysisGraph).calculate()
    }
}
