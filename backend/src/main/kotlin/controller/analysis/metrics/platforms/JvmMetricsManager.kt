package controller.analysis.metrics.platforms

import controller.analysis.metrics.input.InputQuality
import controller.analysis.metrics.input.platforms.jvm.JvmInputQualityAnalyzer
import model.graph.Graph


object JvmMetricsManager : MetricsManager() {
    override fun calculateInputMetrics(staticAnalysisGraph: Graph, dynamicAnalysisGraph: Graph, mergedGraph: Graph): InputQuality {
        return JvmInputQualityAnalyzer(staticAnalysisGraph, dynamicAnalysisGraph, mergedGraph).calculate()
    }
}
