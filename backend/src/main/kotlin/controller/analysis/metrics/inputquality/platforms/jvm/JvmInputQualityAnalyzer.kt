package controller.analysis.metrics.inputquality.platforms.jvm

import controller.analysis.metrics.inputquality.InputQuality
import controller.analysis.metrics.inputquality.platforms.jvm.dynamicanalysis.JfrRecordingQualityAnalyzer
import model.graph.Graph


class JvmInputQualityAnalyzer(private var staticAnalysisGraph: Graph, private var dynamicAnalysisGraph: Graph) {
    fun calculate(): InputQuality {
        val dynamicAnalysisQuality: Int = JfrRecordingQualityAnalyzer.calculateQuality(staticAnalysisGraph = staticAnalysisGraph, dynamicAnalysisGraph = dynamicAnalysisGraph)
        return InputQuality(dynamicAnalysis = dynamicAnalysisQuality)
    }
}
