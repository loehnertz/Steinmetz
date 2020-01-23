package controller.analysis.extraction.coupling.dynamically.platforms.jvm

import controller.analysis.extraction.coupling.dynamically.AbstractDynamicAnalysisExtractor
import model.graph.Graph
import model.graph.Unit
import java.io.File


class JvmDynamicAnalysisExtractor(private val projectName: String, private val basePackageIdentifier: String, private val recordingFile: File) : AbstractDynamicAnalysisExtractor() {
    override fun extract(): Graph {
        val graph: Graph = retrieveGraph()
        return scaleDynamicCouplingScores(graph)
    }

    override fun normalizeUnit(unit: Unit): Unit = Unit(identifier = unit.identifier.substringBeforeLast(InnerUnitDelimiter), packageIdentifier = unit.packageIdentifier)

    private fun retrieveGraph(): Graph {
        return when (recordingFile.extension) {
            JfrRecordingExtension             -> JfrRecordingAnalyzer(projectName = projectName, basePackageIdentifier = basePackageIdentifier, jfrRecording = recordingFile).extract()
            InstrumentationRecordingExtension -> InstrumentationRecordingAnalyzer(instrumentationRecordingFile = recordingFile).extract()
            else                              -> throw IllegalArgumentException()
        }
    }

    companion object {
        private const val JfrRecordingExtension = "jfr"
        private const val InstrumentationRecordingExtension = "net"
        private const val InnerUnitDelimiter = '$'
    }
}
