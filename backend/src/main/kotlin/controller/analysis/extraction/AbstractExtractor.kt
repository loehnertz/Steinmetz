package controller.analysis.extraction

import model.graph.Edge
import model.graph.Graph
import model.graph.Node
import model.graph.Unit
import java.io.File


abstract class AbstractExtractor {
    abstract fun extract(): Graph

    internal abstract fun normalizeUnit(unit: Unit): Unit

    internal fun buildBasePath(platformIdentifier: String, projectName: String) = "$ExtractionBasePath/$platformIdentifier/$projectName"

    internal fun cleanup(vararg basePaths: String) = basePaths.forEach { File(it).deleteRecursively() }

    internal fun mergeInnerUnitNodesWithParentNodes(graph: Graph): Graph {
        val mergedGraph = Graph()

        for (edge: Edge in graph.edges) {
            val startUnit: Unit = normalizeUnit(edge.start)
            val endUnit: Unit = normalizeUnit(edge.end)
            val newEdge = Edge(start = startUnit, end = endUnit, attributes = edge.attributes)

            mergedGraph.addOrUpdateEdge(newEdge)
        }

        for (node: Node in graph.nodes) {
            val newNode = Node(unit = normalizeUnit(node.unit), attributes = node.attributes)

            mergedGraph.addOrUpdateNode(newNode)
        }

        return mergedGraph
    }

    companion object {
        const val ExtractionBasePath: String = ExtractorCompanion.ExtractionBasePath

        fun normalizeUnit(unit: Unit): Unit = unit
    }
}
