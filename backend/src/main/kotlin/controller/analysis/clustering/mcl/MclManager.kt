package controller.analysis.clustering.mcl

import model.graph.Graph
import model.graph.Node
import model.graph.NodeAttributes
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.*


class MclManager(private val graph: Graph) {
    fun applyMcl(): Graph {
        val process = Runtime.getRuntime().exec(MclCommand)
        BufferedWriter(OutputStreamWriter(process.outputStream)).use { writer ->
            val input = convertGraphToMclInput()
            writer.write(input)
            writer.flush()
        }
        process.waitFor()
        val output = Scanner(process.inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next()
        return convertMclOutputToGraph(output)
    }

    private fun convertGraphToMclInput(): String {
        return graph.edges.joinToString("\n") { buildMclInputLine(it.start, it.end, it.attributes.couplingScore) }
    }

    private fun convertMclOutputToGraph(output: String): Graph {
        val lines = output.split("\n").dropLast(1)  // The last item is always an empty line with this approach
        var clusterId = 1

        for (line in lines) {
            val nodes = line.split("\t")
            for (nodeIdentifierString in nodes) {
                val nodeIdentifier = nodeIdentifierString.substringAfterLast('.')
                val nodePackageIdentifier = nodeIdentifierString.substringBeforeLast('.')
                val clusteredNode = Node(identifier = nodeIdentifier, packageIdentifier = nodePackageIdentifier, attributes = NodeAttributes(cluster = clusterId))
                graph.updateNode(clusteredNode)
            }
            clusterId++
        }

        return graph
    }

    private fun buildMclInputLine(startNode: Node, endNode: Node, weight: Int): String {
        return "${buildNodeIdentifier(startNode.identifier, startNode.packageIdentifier)}\t${buildNodeIdentifier(endNode.identifier, endNode.packageIdentifier)}\t$weight"
    }

    private fun buildNodeIdentifier(identifier: String, packageIdentifier: String): String {
        return "$packageIdentifier.$identifier"
    }

    companion object {
        private const val MclCommand = "mcl - --abc -o -"
    }
}
