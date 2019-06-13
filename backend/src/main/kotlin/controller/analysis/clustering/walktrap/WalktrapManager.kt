package controller.analysis.clustering.walktrap

import controller.analysis.clustering.ClusteringAlgorithmManager
import model.graph.*
import model.graph.Unit
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*


class WalktrapManager(private val graph: Graph, private val projectName: String) : ClusteringAlgorithmManager {
    private val unit2IdMap: HashMap<Unit, Int> = hashMapOf()
    private val id2UnitMap: HashMap<Int, Unit> = hashMapOf()

    override fun apply(tunableParameter: Double?): Graph {
        val inputFile: File = createInputFile()
        val process: Process = Runtime.getRuntime().exec(buildCommand(inputFile, tunableParameter?.toInt()
                ?: DefaultTunableParameter)).also { it.waitFor() }
        val output: String = Scanner(process.inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next()
        return convertOutputToGraph(output)
    }

    private fun createInputFile(): File {
        var inputFileString = ""
        var currentNodeId = 0

        @Suppress("UseWithIndex")
        for (node: Node in graph.nodes) {
            unit2IdMap[node.unit] = currentNodeId
            id2UnitMap[currentNodeId] = node.unit
            currentNodeId++
        }

        for (edge: Edge in graph.edges) {
            val inputLine: String = buildInputLine(startUnit = edge.start, endUnit = edge.end, weight = edge.attributes.couplingScore)
            inputFileString += "$inputLine\n"
        }

        return writeInputFile(inputFileString)
    }

    private fun convertOutputToGraph(output: String): Graph {
        var clusterId = 0

        val outputLines: List<String> = output.substringAfterLast(OutputPartitionKeyword).split("\n")
        for (line: String in outputLines) {
            if (!line.startsWith(OutputLinePrefix)) continue

            for (unitId: Int in convertOutputClusterLineToListOfUnitIds(line)) {
                val clusteredUnit: Unit = id2UnitMap[unitId]
                        ?: throw InternalError("Mapping the nodes to IDs for clustering failed")
                graph.addOrUpdateNode(Node(unit = clusteredUnit, attributes = NodeAttributes(cluster = clusterId)))
            }

            clusterId++
        }

        return graph
    }

    private fun writeInputFile(inputFileString: String): File {
        val inputFile = File("${constructBaseFileLocation()}.$InputFileExtension")
        inputFile.parentFile.mkdirs()
        inputFile.createNewFile()
        inputFile.bufferedWriter().use { file -> file.write(inputFileString) }
        return inputFile
    }

    private fun buildInputLine(startUnit: Unit, endUnit: Unit, weight: Int): String {
        return "${unit2IdMap[startUnit]}\t${unit2IdMap[endUnit]}\t$weight"
    }

    private fun convertOutputClusterLineToListOfUnitIds(clusterLine: String): List<Int> {
        val clusterList: String = clusterLine.substringAfter(" = ").replace("{", "").replace("}", "")
        return clusterList.split(", ").map { it.toInt() }
    }

    private fun constructBaseFileLocation(): String {
        return "$InputOutputPath/$projectName/$InputFileName"
    }

    private fun buildCommand(inputFile: File, amountOfClusters: Int): String {
        return "${retrieveExecutablePath()} ${inputFile.absolutePath} $WalktrapBaseCommand -p$amountOfClusters"
    }

    private fun retrieveExecutablePath(): String {
        return "backend/src/main/resources/$ExecutableName"
    }

    companion object {
        private const val DefaultTunableParameter = 5
        private const val ExecutableName = "walktrap"
        private const val WalktrapBaseCommand = "-s"
        private const val InputOutputPath = "/tmp/steinmetz/walktrap"
        private const val InputFileName = "steinmetz"
        private const val InputFileExtension = "net"
        private const val OutputPartitionKeyword = "Partition"
        private const val OutputLinePrefix = "community"
    }
}
