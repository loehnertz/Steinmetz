package controller.analysis.clustering.chinesewhispers

import controller.analysis.clustering.ClusteringAlgorithmManager
import de.tudarmstadt.lt.cw.CW
import de.tudarmstadt.lt.cw.global.CWGlobal
import model.graph.Graph
import model.graph.Node
import model.graph.NodeAttributes
import model.graph.Unit
import java.io.BufferedWriter
import java.io.StringReader
import java.io.StringWriter
import java.util.*


class ChineseWhispersManager(private val graph: Graph) : ClusteringAlgorithmManager {
    override fun apply(iterations: Int): Graph {
        val inputReader = StringReader(convertGraphToInput())
        val outputWriter = StringWriter()

        CWGlobal.findAndWriteClusters(inputReader, BufferedWriter(outputWriter), 0.0F, if (iterations <= MaxIterations) iterations else MaxIterations, Random(), CW.Option.TOP)

        return convertOutputToGraph(outputWriter.toString())
    }

    private fun convertGraphToInput(): String {
        return graph.edges.joinToString("\n") { buildInputLine(it.start, it.end, it.attributes.couplingScore) }
    }

    private fun convertOutputToGraph(output: String): Graph {
        val lines: List<String> = output.split("\n").dropLast(1)  // The last item is always an empty line with this approach
        var clusterId = 1

        for (line: String in lines) {
            val nodes: List<String> = line.substringAfterLast("\t").split(", ").filter { it != "" }
            for (nodeIdentifierString: String in nodes) {
                val nodeIdentifier: String = nodeIdentifierString.substringAfterLast('.')
                val nodePackageIdentifier: String = nodeIdentifierString.substringBeforeLast('.')
                val clusteredNode = Node(unit = Unit(identifier = nodeIdentifier, packageIdentifier = nodePackageIdentifier), attributes = NodeAttributes(cluster = clusterId))
                graph.addOrUpdateNode(clusteredNode)
            }
            clusterId++
        }

        return graph
    }

    private fun buildInputLine(startUnit: Unit, endUnit: Unit, weight: Int): String {
        return "${buildNodeIdentifier(startUnit.identifier, startUnit.packageIdentifier)}\t${buildNodeIdentifier(endUnit.identifier, endUnit.packageIdentifier)}\t$weight"
    }

    private fun buildNodeIdentifier(identifier: String, packageIdentifier: String): String {
        return "$packageIdentifier.$identifier"
    }

    companion object {
        private const val MaxIterations = 55
    }
}
