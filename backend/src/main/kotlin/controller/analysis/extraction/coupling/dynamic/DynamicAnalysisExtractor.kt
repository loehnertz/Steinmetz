package controller.analysis.extraction.coupling.dynamic

import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.ExtractorCompanion
import model.graph.Graph


abstract class DynamicAnalysisExtractor : AbstractExtractor() {
    internal fun weightDynamicCouplingScores(graph: Graph): Graph {
        val totalDynamicCoupling: Int = graph.edges.sumBy { it.attributes.dynamicCouplingScore }
        graph.edges.forEach { it.attributes.dynamicCouplingScore = ((it.attributes.dynamicCouplingScore.toDouble() / totalDynamicCoupling) * 100).toInt() }
        return graph
    }

    companion object : ExtractorCompanion {
        const val DynamicAnalysisDirectory = "dynamic"

        override fun getWorkingDirectory(): String = "$ExtractionBasePath/$DynamicAnalysisDirectory"
    }
}
