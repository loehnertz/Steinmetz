package controller.analysis.extraction.dynamicanalysis.jvm

import controller.analysis.extraction.dynamicanalysis.DynamicAnalysisExtractor
import controller.analysis.extraction.graph.GraphInserter
import jdk.jfr.consumer.RecordedEvent
import jdk.jfr.consumer.RecordedFrame
import jdk.jfr.consumer.RecordedStackTrace
import jdk.jfr.consumer.RecordingFile
import model.graph.Edge
import model.graph.EdgeAttributes
import model.graph.Graph
import model.graph.Unit
import java.io.File
import java.nio.file.Paths


class JfrRecordingAnalyzer(projectName: String, private val basePackageIdentifier: String, private val jfrRecording: File) : DynamicAnalysisExtractor() {
    private val basePath = buildBasePath(platformIdentifier, projectName)
    private val dynamicAnalysisBasePath = "$basePath/$DynamicAnalysisDirectory"

    override fun extract(): Graph {
        val dynamicAnalysisGraph: Graph = analyzeRecording()
        cleanup(dynamicAnalysisBasePath)
        return dynamicAnalysisGraph
    }

    private fun analyzeRecording(): Graph {
        val edges: ArrayList<Edge> = arrayListOf()

        RecordingFile(Paths.get(jfrRecording.absolutePath)).use { f ->
            while (f.hasMoreEvents()) {
                val event: RecordedEvent = f.readEvent()

                if (event.eventType.name == methodInvocationEventType) {
                    val stackTrace: RecordedStackTrace = event.stackTrace ?: continue
                    val frames: List<RecordedFrame> = stackTrace.frames.filter { it.isJavaFrame && it.method.type.name.startsWith(basePackageIdentifier) }.reversed()

                    frames.forEachIndexed { index, caller ->
                        if (index + 1 < frames.size) {
                            val callee = frames[index + 1]

                            val callerIdentifier = caller.method.type.name.substringBefore('$').substringAfterLast('.')
                            val callerPackageIdentifier = caller.method.type.name.substringBefore('$').substringBeforeLast('.')

                            val calleeIdentifier = callee.method.type.name.substringBefore('$').substringAfterLast('.')
                            val calleePackageIdentifier = callee.method.type.name.substringBefore('$').substringBeforeLast('.')

                            val callerUnit = Unit(identifier = callerIdentifier, packageIdentifier = callerPackageIdentifier)
                            val calleeUnit = Unit(identifier = calleeIdentifier, packageIdentifier = calleePackageIdentifier)

                            if (callerUnit != calleeUnit) {
                                edges.add(Edge(start = callerUnit, end = calleeUnit, attributes = EdgeAttributes(couplingScore = 1)))
                            }
                        }
                    }
                }
            }
        }

        return Graph(edges = edges.toMutableSet())
    }

    companion object {
        private const val platformIdentifier = GraphInserter.JvmProjectKey
        private const val methodInvocationEventType = "jdk.ExecutionSample"
    }
}
