package controller.analysis.extraction.coupling.dynamically.platforms.jvm

import controller.analysis.extraction.Platform
import controller.analysis.extraction.coupling.dynamically.DynamicAnalysisExtractor
import jdk.jfr.consumer.RecordedEvent
import jdk.jfr.consumer.RecordedFrame
import jdk.jfr.consumer.RecordedStackTrace
import jdk.jfr.consumer.RecordingFile
import model.graph.Edge
import model.graph.EdgeAttributes
import model.graph.Graph
import model.graph.Unit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths


class JfrRecordingAnalyzer(projectName: String, private val basePackageIdentifier: String, private val jfrRecording: File) : DynamicAnalysisExtractor() {
    private val logger: Logger = LoggerFactory.getLogger(JfrRecordingAnalyzer::class.java)

    private val basePath: String = buildBasePath(PlatformIdentifier, projectName)
    private val dynamicAnalysisBasePath = "$basePath/$DynamicAnalysisDirectory"

    override fun extract(): Graph {
        val dynamicAnalysisGraph: Graph = analyzeRecording().also { logger.info("\tRetrieved ${it.edges.size} dynamic coupling pairs") }
        cleanup(basePath, dynamicAnalysisBasePath)
        return mergeInnerUnitNodesWithParentNodes(dynamicAnalysisGraph).also { logger.info("\tConstructed dynamic coupling graph") }
    }

    override fun normalizeUnit(unit: Unit): Unit = Unit(identifier = unit.identifier.substringBeforeLast(InnerUnitDelimiter), packageIdentifier = unit.packageIdentifier)

    private fun analyzeRecording(): Graph {
        val graph = Graph()

        RecordingFile(Paths.get(jfrRecording.absolutePath)).use { file: RecordingFile ->
            while (file.hasMoreEvents()) {
                val event: RecordedEvent = file.readEvent()

                if (event.eventType.name == MethodInvocationEventType) {
                    val stackTrace: RecordedStackTrace = event.stackTrace ?: continue
                    val frames: List<RecordedFrame> = stackTrace.frames.filter { it.isJavaFrame && isLegalUnit(it.method.type.name, basePackageIdentifier) }.reversed()

                    frames.forEachIndexed { index: Int, caller: RecordedFrame ->
                        if (index + 1 < frames.size) {
                            val callee: RecordedFrame = frames[index + 1]

                            val callerIdentifier: String = caller.method.type.name.substringBefore('$').substringAfterLast('.')
                            val callerPackageIdentifier: String = caller.method.type.name.substringBefore('$').substringBeforeLast('.')

                            val calleeIdentifier: String = callee.method.type.name.substringBefore('$').substringAfterLast('.')
                            val calleePackageIdentifier: String = callee.method.type.name.substringBefore('$').substringBeforeLast('.')

                            val callerUnit = Unit(identifier = callerIdentifier, packageIdentifier = callerPackageIdentifier)
                            val calleeUnit = Unit(identifier = calleeIdentifier, packageIdentifier = calleePackageIdentifier)

                            val edge = Edge(start = callerUnit, end = calleeUnit, attributes = EdgeAttributes(dynamicCouplingScore = 1))

                            if (callerUnit != calleeUnit) graph.addOrUpdateEdge(edge)
                        }
                    }
                }
            }
        }

        return graph
    }

    companion object {
        private val PlatformIdentifier: String = Platform.JAVA.toString().toLowerCase()
        private const val MethodInvocationEventType = "jdk.ExecutionSample"
        private const val InnerUnitDelimiter = '$'
    }
}
