package controller.analysis.extraction.coupling.dynamic.platforms.jvm

import controller.analysis.extraction.coupling.dynamic.DynamicAnalysisExtractor
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
    private val basePath: String = buildBasePath(platformIdentifier, projectName)
    private val dynamicAnalysisBasePath = "$basePath/$DynamicAnalysisDirectory"

    override fun extract(): Graph {
        val dynamicAnalysisGraph: Graph = analyzeRecording()
        cleanup(dynamicAnalysisBasePath)
        return dynamicAnalysisGraph
    }

    private fun analyzeRecording(): Graph {
        val edges: ArrayList<Edge> = arrayListOf()

        RecordingFile(Paths.get(jfrRecording.absolutePath)).use { file: RecordingFile ->
            while (file.hasMoreEvents()) {
                val event: RecordedEvent = file.readEvent()

                if (event.eventType.name == methodInvocationEventType) {
                    val stackTrace: RecordedStackTrace = event.stackTrace ?: continue
                    val frames: List<RecordedFrame> = stackTrace.frames.filter { it.isJavaFrame && it.method.type.name.startsWith(basePackageIdentifier) }.reversed()

                    frames.forEachIndexed { index: Int, caller: RecordedFrame ->
                        if (index + 1 < frames.size) {
                            val callee: RecordedFrame = frames[index + 1]

                            val callerIdentifier: String = caller.method.type.name.substringBefore('$').substringAfterLast('.')
                            val callerPackageIdentifier: String = caller.method.type.name.substringBefore('$').substringBeforeLast('.')

                            val calleeIdentifier: String = callee.method.type.name.substringBefore('$').substringAfterLast('.')
                            val calleePackageIdentifier: String = callee.method.type.name.substringBefore('$').substringBeforeLast('.')

                            val callerUnit = Unit(identifier = callerIdentifier, packageIdentifier = callerPackageIdentifier)
                            val calleeUnit = Unit(identifier = calleeIdentifier, packageIdentifier = calleePackageIdentifier)

                            if (callerUnit != calleeUnit) {
                                edges.add(Edge(start = callerUnit, end = calleeUnit, attributes = EdgeAttributes(dynamicCouplingScore = 1, logicalCouplingScore = 0)))
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