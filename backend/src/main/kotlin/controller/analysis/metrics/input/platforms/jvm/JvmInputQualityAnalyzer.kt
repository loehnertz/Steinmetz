package controller.analysis.metrics.input.platforms.jvm

import controller.analysis.metrics.input.InputQuality
import controller.analysis.metrics.input.platforms.jvm.dynamicanalysis.JfrRecordingQualityAnalyzer
import model.graph.Graph


class JvmInputQualityAnalyzer(private val staticAnalysisGraph: Graph, private val dynamicAnalysisGraph: Graph, private val mergedGraph: Graph) {
    fun calculate(): InputQuality {
        val dynamicAnalysisQuality: Int = JfrRecordingQualityAnalyzer.calculateQuality(staticAnalysisGraph = staticAnalysisGraph, dynamicAnalysisGraph = dynamicAnalysisGraph)
        val accumulatedEdgeWeight: Int = calculateAccumulatedEdgeWeight()

        return InputQuality(dynamicAnalysis = dynamicAnalysisQuality, accumulatedEdgeWeight = accumulatedEdgeWeight)
    }

    private fun calculateAccumulatedEdgeWeight(): Int {
        return mergedGraph.edges.sumBy { it.attributes.couplingScore }
    }
}
