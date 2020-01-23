package controller.analysis.extraction.coupling.dynamically.platforms.jvm

import controller.analysis.extraction.coupling.dynamically.AbstractDynamicAnalysisExtractor
import model.graph.Edge
import model.graph.EdgeAttributes
import model.graph.Graph
import model.graph.Unit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utility.Utilities
import java.io.File


class InstrumentationRecordingAnalyzer(private val instrumentationRecordingFile: File) : AbstractDynamicAnalysisExtractor() {
    private val logger: Logger = LoggerFactory.getLogger(InstrumentationRecordingAnalyzer::class.java)

    override fun extract(): Graph {
        val invocations: List<Pair<String, String>> = retrieveInvocations().also { logger.info("Retrieved ${it.size} dynamic coupling pairs") }
        val graph: Graph = convertInvocationPairsToGraph(invocations)
        return mergeInnerUnitNodesWithParentNodes(graph).also { logger.info("Constructed dynamic coupling graph") }
    }

    override fun normalizeUnit(unit: Unit): Unit = Unit(identifier = unit.identifier.substringBeforeLast(InnerUnitDelimiter), packageIdentifier = unit.packageIdentifier)

    private fun convertInvocationPairsToGraph(invocations: List<Pair<String, String>>): Graph {
        val graph = Graph()

        for (invocation in invocations) {
            val startUnit = Unit(identifier = invocation.first.substringAfterLast('.'), packageIdentifier = invocation.first.substringBeforeLast('.'))
            val endUnit = Unit(identifier = invocation.second.substringAfterLast('.'), packageIdentifier = invocation.second.substringBeforeLast('.'))
            val edge = Edge(start = startUnit, end = endUnit, attributes = EdgeAttributes(dynamicCouplingScore = 1))

            if (startUnit == endUnit) continue

            graph.addOrUpdateEdge(edge)
        }

        return graph
    }

    private fun retrieveInvocations(): List<Pair<String, String>> {
        return Utilities.extractTsv(instrumentationRecordingFile).map { stripMethodIdentifier(it.first()) to stripMethodIdentifier(it.last()) }
    }

    private fun stripMethodIdentifier(identifier: String): String {
        return identifier.substringBeforeLast(':')
    }

    companion object {
        private const val InnerUnitDelimiter = '$'
    }
}
