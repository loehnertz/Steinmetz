package controller.analysis.metrics.platforms.jvm

import controller.analysis.metrics.MetricsManager
import controller.analysis.metrics.input.platforms.jvm.JvmInputQualityAnalyzer
import model.graph.Graph
import model.metrics.InputQuality


object JvmMetricsManager : MetricsManager() {
    override fun calculateInputMetrics(staticAnalysisGraph: Graph, dynamicAnalysisGraph: Graph, mergedGraph: Graph): InputQuality {
        return JvmInputQualityAnalyzer(staticAnalysisGraph, dynamicAnalysisGraph, mergedGraph).calculate()
    }
}
