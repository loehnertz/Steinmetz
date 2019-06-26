package controller.analysis.extraction.coupling.dynamically.platforms.jvm

import controller.analysis.extraction.coupling.dynamically.DynamicAnalysisExtractor
import controller.analysis.extraction.coupling.statically.platforms.jvm.JvmBytecodeExtractor
import model.graph.Edge
import model.graph.EdgeAttributes
import model.graph.Graph
import model.graph.Unit
import utility.Utilities
import java.io.File


class InstrumentationRecordingAnalyzer(private val instrumentationRecordingFile: File) : DynamicAnalysisExtractor() {
    override fun extract(): Graph {
        val invokations: List<Pair<String, String>> = retrieveInvokations()
        val graph: Graph = convertInvokationPairsToGraph(invokations)
        return mergeInnerUnitNodesWithParentNodes(graph)
    }

    override fun normalizeUnit(unit: Unit): Unit = JvmBytecodeExtractor.normalizeUnit(unit)

    private fun convertInvokationPairsToGraph(invokations: List<Pair<String, String>>): Graph {
        val graph = Graph()

        for (invokation: Pair<String, String> in invokations) {
            val startUnit = Unit(identifier = invokation.first.substringAfterLast('.'), packageIdentifier = invokation.first.substringBeforeLast('.'))
            val endUnit = Unit(identifier = invokation.second.substringAfterLast('.'), packageIdentifier = invokation.second.substringBeforeLast('.'))
            val edge = Edge(start = startUnit, end = endUnit, attributes = EdgeAttributes(dynamicCouplingScore = 1))

            if (startUnit == endUnit) continue

            graph.addOrUpdateEdge(edge)
        }

        return graph
    }

    private fun retrieveInvokations(): List<Pair<String, String>> {
        return Utilities.extractTsv(instrumentationRecordingFile).map { Pair(stripMethodIdentifier(it.first()), stripMethodIdentifier(it.last())) }
    }

    private fun stripMethodIdentifier(identifier: String): String {
        return identifier.substringBeforeLast(':')
    }
}
