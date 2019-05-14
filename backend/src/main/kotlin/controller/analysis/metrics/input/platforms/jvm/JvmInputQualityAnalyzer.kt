package controller.analysis.metrics.input.platforms.jvm

import controller.analysis.metrics.input.InputQuality
import controller.analysis.metrics.input.platforms.jvm.dynamicanalysis.JfrRecordingQualityAnalyzer
import model.graph.Graph


class JvmInputQualityAnalyzer(private var staticAnalysisGraph: Graph, private var dynamicAnalysisGraph: Graph) {
    fun calculate(): InputQuality {
        val dynamicAnalysisQuality: Int = JfrRecordingQualityAnalyzer.calculateQuality(staticAnalysisGraph = staticAnalysisGraph, dynamicAnalysisGraph = dynamicAnalysisGraph)
        return InputQuality(dynamicAnalysis = dynamicAnalysisQuality)
    }
}
