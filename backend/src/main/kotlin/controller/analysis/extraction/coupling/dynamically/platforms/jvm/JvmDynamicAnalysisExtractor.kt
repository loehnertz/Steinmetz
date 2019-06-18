package controller.analysis.extraction.coupling.dynamically.platforms.jvm

import controller.analysis.extraction.coupling.dynamically.DynamicAnalysisExtractor
import model.graph.Graph
import java.io.File


class JvmDynamicAnalysisExtractor(private val projectName: String, private val basePackageIdentifier: String, private val recordingFile: File) : DynamicAnalysisExtractor() {
    override fun extract(): Graph {
        val graph: Graph = retrieveGraph()
        return scaleDynamicCouplingScores(graph)
    }

    private fun retrieveGraph(): Graph {
        return when (recordingFile.extension) {
            JfrRecordingExtension -> JfrRecordingAnalyzer(projectName = projectName, basePackageIdentifier = basePackageIdentifier, jfrRecording = recordingFile).extract()
            InstrumentationRecordingExtension -> InstrumentationRecordingAnalyzer(instrumentationRecordingFile = recordingFile).extract()
            else -> throw IllegalArgumentException()
        }
    }

    companion object {
        private const val JfrRecordingExtension = "jfr"
        private const val InstrumentationRecordingExtension = "net"
    }
}
