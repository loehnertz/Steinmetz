package controller.analysis.extraction.dynamicanalysis.jvm

import controller.analysis.extraction.dynamicanalysis.DynamicAnalysisExtractor
import controller.analysis.extraction.graph.GraphInserter
import jdk.jfr.consumer.RecordedEvent
import jdk.jfr.consumer.RecordingFile
import model.graph.Attributes
import model.graph.Edge
import model.graph.Node
import java.io.File
import java.nio.file.Paths


class JfrRecordingAnalyzer(projectName: String, private val basePackageIdentifier: String, private val jfrRecording: File) : DynamicAnalysisExtractor() {
    private val basePath = buildBasePath(platformIdentifier, projectName)
    private val dynamicAnalysisBasePath = "$basePath/$DynamicAnalysisDirectory"

    override fun extract(): List<Edge> {
        val dynamicAnalysisGraph = analyzeRecording()
        cleanup(dynamicAnalysisBasePath)
        return dynamicAnalysisGraph
    }

    private fun analyzeRecording(): List<Edge> {
        val histogram = mutableMapOf<Pair<Node, Node>, Int>()

        RecordingFile(Paths.get(jfrRecording.absolutePath)).use { f ->
            while (f.hasMoreEvents()) {
                val event: RecordedEvent = f.readEvent()

                if (event.eventType.name == methodInvocationEventType) {
                    val stackTrace = event.stackTrace ?: continue
                    val frames = stackTrace.frames.filter { it.isJavaFrame && it.method.type.name.startsWith(basePackageIdentifier) }.reversed()

                    frames.forEachIndexed { index, caller ->
                        if (index + 1 < frames.size) {
                            val callee = frames[index + 1]

                            val callerIdentifier = caller.method.type.name.substringBefore('$').substringAfterLast('.')
                            val callerPackageIdentifier = caller.method.type.name.substringBefore('$').substringBeforeLast('.')

                            val calleeIdentifier = callee.method.type.name.substringBefore('$').substringAfterLast('.')
                            val calleePackageIdentifier = callee.method.type.name.substringBefore('$').substringBeforeLast('.')

                            val callerUnit = Node(identifier = callerIdentifier, packageIdentifier = callerPackageIdentifier)
                            val calleeUnit = Node(identifier = calleeIdentifier, packageIdentifier = calleePackageIdentifier)

                            if (callerUnit != calleeUnit) {
                                val relationship = Pair(callerUnit, calleeUnit)
                                histogram.putIfAbsent(relationship, 0)
                                histogram[relationship] = histogram[relationship]!! + 1
                            }
                        }
                    }
                }
            }
        }

        return histogram.map { (edge, weight) -> Edge(start = edge.first, end = edge.second, attributes = Attributes(couplingScore = weight)) }
    }

    companion object {
        private const val platformIdentifier = GraphInserter.JvmProjectKey
        private const val methodInvocationEventType = "jdk.ExecutionSample"
    }
}
