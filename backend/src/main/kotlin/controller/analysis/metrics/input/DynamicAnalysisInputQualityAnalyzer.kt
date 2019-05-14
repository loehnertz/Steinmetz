package controller.analysis.metrics.input

import model.graph.Graph


interface DynamicAnalysisInputQualityAnalyzer {
    fun calculateQuality(staticAnalysisGraph: Graph, dynamicAnalysisGraph: Graph): Int
}
