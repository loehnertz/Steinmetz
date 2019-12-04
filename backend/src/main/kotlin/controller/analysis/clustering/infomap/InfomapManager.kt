package controller.analysis.clustering.infomap

import controller.analysis.clustering.ClusteringAlgorithmManager
import model.graph.*
import model.graph.Unit
import utility.Utilities
import java.io.File
import java.util.*


class InfomapManager(private val graph: Graph, private val projectName: String) : ClusteringAlgorithmManager {
    private val unit2IdMap: HashMap<Unit, Int> = hashMapOf()
    private val id2UnitMap: HashMap<Int, Unit> = hashMapOf()

    override fun apply(iterations: Int): Graph {
        createInputFile()

        Runtime.getRuntime().exec(buildCommand(iterations)).waitFor()

        val outputLines: List<String> = readOutputFileLines()
        return convertOutputToGraph(outputLines)
    }

    private fun createInputFile() {
        var inputFileString = ""
        var currentNodeId = 1

        for (node: Node in graph.nodes) {
            unit2IdMap[node.unit] = currentNodeId
            id2UnitMap[currentNodeId] = node.unit
            currentNodeId++
        }

        for (edge: Edge in graph.edges) {
            val inputLine: String = buildInputLine(startUnit = edge.start, endUnit = edge.end, weight = edge.attributes.couplingScore)
            inputFileString += "$inputLine\n"
        }

        writeInputFile(inputFileString)
    }

    private fun convertOutputToGraph(outputLines: List<String>): Graph {
        for (line: String in outputLines.filter { !it.startsWith('#') }) {
            val (nodeId, clusterId, _) = line.split(' ')

            val clusteredUnit: Unit = id2UnitMap[nodeId.toInt()]
                                      ?: throw InternalError("Mapping the nodes to IDs for clustering failed")
            graph.addOrUpdateNode(Node(unit = clusteredUnit, attributes = NodeAttributes(cluster = clusterId.toInt())))
        }

        return graph
    }

    private fun writeInputFile(inputFileString: String) {
        val inputFile = File("$InputOutputPath/$projectName/$InputFileName")
        inputFile.parentFile.mkdirs()
        inputFile.createNewFile()
        inputFile.bufferedWriter().use { file -> file.write(inputFileString) }
    }

    private fun readOutputFileLines(): List<String> {
        return File("$InputOutputPath/$projectName/$OutputDirectory/$OutputFileName").bufferedReader().readLines()
    }

    private fun buildCommand(iterationAmount: Int): String {
        return "${retrieveExecutablePath()} $InputOutputPath/$projectName/$InputFileName $InputOutputPath/$projectName/$OutputDirectory/ -i link-list -N ${if (iterationAmount <= MaxIterations) iterationAmount else MaxIterations} --directed --clu"
    }

    private fun buildInputLine(startUnit: Unit, endUnit: Unit, weight: Int): String {
        return "${unit2IdMap[startUnit]} ${unit2IdMap[endUnit]} $weight"
    }

    private fun retrieveExecutablePath(): String {
        return Utilities.getExternalExecutableAsFile(ExecutableName).absolutePath
    }

    companion object {
        private const val ExecutableName = "Infomap"
        private const val InputOutputPath = "/tmp/steinmetz/infomap"
        private const val InputFileName = "steinmetz.txt"
        private const val OutputDirectory = "."
        private const val OutputFileName = "steinmetz.clu"
        private const val MaxIterations = 60
    }
}
