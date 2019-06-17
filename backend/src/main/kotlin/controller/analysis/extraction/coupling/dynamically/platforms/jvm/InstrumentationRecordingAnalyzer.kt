package controller.analysis.extraction.coupling.dynamically.platforms.jvm

import controller.analysis.extraction.coupling.dynamically.DynamicAnalysisExtractor
import model.graph.Graph
import utility.Utilities
import java.io.File


class InstrumentationRecordingAnalyzer(private val instrumentationRecordingFile: File) : DynamicAnalysisExtractor() {
    override fun extract(): Graph {
        val invokations: List<Pair<String, String>> = Utilities.extractTsv(instrumentationRecordingFile).map { Pair(it.first(), it.last()) }
        return convertInvokationPairsToGraph(invokations)
    }
}
