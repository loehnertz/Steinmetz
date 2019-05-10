package controller.analysis.metrics.inputquality

import model.graph.Graph


interface DynamicAnalysisInputQualityAnalyzer {
    fun calculateQuality(staticAnalysisGraph: Graph, dynamicAnalysisGraph: Graph): Int
}
