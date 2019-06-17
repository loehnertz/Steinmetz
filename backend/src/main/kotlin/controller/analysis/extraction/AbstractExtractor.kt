package controller.analysis.extraction

import model.graph.Edge
import model.graph.EdgeAttributes
import model.graph.Graph
import model.graph.Unit
import java.io.File


abstract class AbstractExtractor {
    abstract fun extract(): Graph

    fun buildBasePath(platformIdentifier: String, projectName: String) = "$ExtractionBasePath/$platformIdentifier/$projectName"

    fun cleanup(vararg basePaths: String) = basePaths.forEach { File(it).deleteRecursively() }

    fun convertInvokationPairsToGraph(invokations: List<Pair<String, String>>): Graph {
        val graph = Graph()

        for (invokation: Pair<String, String> in invokations) {
            val startUnit = Unit(identifier = invokation.first.substringAfterLast('.'), packageIdentifier = invokation.first.substringBeforeLast('.'))
            val endUnit = Unit(identifier = invokation.second.substringAfterLast('.'), packageIdentifier = invokation.second.substringBeforeLast('.'))
            val edge = Edge(start = startUnit, end = endUnit, attributes = EdgeAttributes())

            graph.addOrUpdateEdge(edge)
        }

        return graph
    }

    companion object {
        const val ExtractionBasePath: String = ExtractorCompanion.ExtractionBasePath
    }
}
