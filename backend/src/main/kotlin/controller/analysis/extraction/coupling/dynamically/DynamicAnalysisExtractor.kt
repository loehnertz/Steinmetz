package controller.analysis.extraction.coupling.dynamically

import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.ExtractorCompanion
import model.graph.Graph


abstract class DynamicAnalysisExtractor : AbstractExtractor() {
    internal fun scaleDynamicCouplingScores(graph: Graph): Graph {
        val maximumDynamicCouplingScore: Int = graph.edges.map { it.attributes.dynamicCouplingScore }.max()!!
        graph.edges.forEach { it.attributes.dynamicCouplingScore = ((it.attributes.dynamicCouplingScore.toDouble() / maximumDynamicCouplingScore) * 100).toInt() }
        return graph
    }

    companion object : ExtractorCompanion {
        const val DynamicAnalysisDirectory = "dynamic"

        override fun getWorkingDirectory(): String = "$ExtractionBasePath/$DynamicAnalysisDirectory"
    }
}
