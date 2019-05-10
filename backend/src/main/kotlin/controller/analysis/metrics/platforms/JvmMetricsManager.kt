package controller.analysis.metrics.platforms

import controller.analysis.metrics.Metrics
import controller.analysis.metrics.inputquality.InputQuality
import controller.analysis.metrics.inputquality.platforms.jvm.JvmInputQualityAnalyzer
import model.graph.Graph


class JvmMetricsManager(private var staticAnalysisGraph: Graph, private var dynamicAnalysisGraph: Graph) {
    fun generateMetrics(): Metrics {
        val inputQuality: InputQuality = JvmInputQualityAnalyzer(staticAnalysisGraph, dynamicAnalysisGraph).calculate()

        return Metrics(inputQuality = inputQuality)
    }
}
