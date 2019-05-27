package controller.analysis.metrics.input.platforms.jvm

import controller.analysis.metrics.input.platforms.jvm.dynamicanalysis.JfrRecordingQualityAnalyzer
import model.graph.Graph
import model.metrics.InputQuality


class JvmInputQualityAnalyzer(private val staticAnalysisGraph: Graph, private val dynamicAnalysisGraph: Graph, private val mergedGraph: Graph) {
    fun calculate(): InputQuality {
        val dynamicAnalysisQuality: Int = JfrRecordingQualityAnalyzer.calculateQuality(staticAnalysisGraph = staticAnalysisGraph, dynamicAnalysisGraph = dynamicAnalysisGraph)

        return InputQuality(dynamicAnalysis = dynamicAnalysisQuality)
    }
}
