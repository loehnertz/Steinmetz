package controller.analysis.metrics.platforms.jvm

import controller.analysis.metrics.MetricsManager
import controller.analysis.metrics.input.InputQualityAnalyzer
import model.graph.Graph
import model.metrics.InputQuality


object JvmMetricsManager : MetricsManager() {
    override fun calculateInputMetrics(staticAnalysisGraph: Graph, dynamicAnalysisGraph: Graph, mergedStaticAndDynamicAnalysisGraph: Graph, semanticCouplingGraph: Graph, logicalCouplingGraph: Graph): InputQuality {
        return InputQualityAnalyzer(
                staticAnalysisGraph = staticAnalysisGraph,
                dynamicAnalysisGraph = dynamicAnalysisGraph,
                mergedStaticAndDynamicAnalysisGraph = mergedStaticAndDynamicAnalysisGraph,
                semanticAnalysisGraph = semanticCouplingGraph,
                logicalAnalysisGraph = logicalCouplingGraph
        ).calculate()
    }
}
