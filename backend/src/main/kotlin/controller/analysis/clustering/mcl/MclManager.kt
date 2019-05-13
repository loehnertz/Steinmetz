package controller.analysis.clustering.mcl

import controller.analysis.clustering.ClusteringAlgorithmManager
import model.graph.Graph
import model.graph.Node
import model.graph.NodeAttributes
import model.graph.Unit
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.*


class MclManager(private val graph: Graph) : ClusteringAlgorithmManager {
    override fun apply(tunableParameter: Double?): Graph {
        val process: Process = Runtime.getRuntime().exec(buildCommand(tunableParameter))
        BufferedWriter(OutputStreamWriter(process.outputStream)).use { writer ->
            val input: String = convertGraphToInput()
            writer.write(input)
            writer.flush()
        }
        process.waitFor()
        val output: String = Scanner(process.inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next()
        return convertOutputToGraph(output)
    }

    private fun convertGraphToInput(): String {
        return graph.edges.joinToString("\n") { buildInputLine(it.start, it.end, it.attributes.couplingScore) }
    }

    private fun convertOutputToGraph(output: String): Graph {
        val lines: List<String> = output.split("\n").dropLast(1)  // The last item is always an empty line with this approach
        var clusterId = 1

        for (line: String in lines) {
            val nodes: List<String> = line.split("\t")
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

    private fun buildCommand(clusteringInflationValue: Double?): String {
        var mclCommand = "${retrieveExecutablePath()} $MclBaseCommand"
        if (clusteringInflationValue != null) mclCommand = mclCommand.plus(" -I $clusteringInflationValue")
        return mclCommand
    }

    private fun buildInputLine(startUnit: Unit, endUnit: Unit, weight: Int): String {
        return "${buildNodeIdentifier(startUnit.identifier, startUnit.packageIdentifier)}\t${buildNodeIdentifier(endUnit.identifier, endUnit.packageIdentifier)}\t$weight"
    }

    private fun buildNodeIdentifier(identifier: String, packageIdentifier: String): String {
        return "$packageIdentifier.$identifier"
    }

    private fun retrieveExecutablePath(): String {
        return "backend/src/main/resources/$ExecutableName"
    }

    companion object {
        private const val ExecutableName = "mcl"
        private const val MclBaseCommand = "- --abc -o -"
    }
}
