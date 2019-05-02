package controller.analysis.clustering.infomap

import model.graph.Graph
import model.graph.Node
import java.io.File
import java.util.*


class InfomapManager(private val graph: Graph) {
    private val node2IdMap: HashMap<Node, Int> = hashMapOf()
    private val id2NodeMap: HashMap<Int, Node> = hashMapOf()

    fun applyInfomap(iterationAmount: Int = 10): Graph {
        createInputFile()

        Runtime.getRuntime().exec(buildCommand(iterationAmount)).waitFor()

        val outputLines = readOutputFileLines()
        return convertInfomapOutputToGraph(outputLines)
    }

    private fun createInputFile() {
        var inputFileString = ""
        var currentNodeId = 1

        for (node in graph.nodes) {
            node2IdMap[node] = currentNodeId
            id2NodeMap[currentNodeId] = node
            currentNodeId++
        }

        for (edge in graph.edges) {
            val inputLine = buildInfomapInputLine(startNode = edge.start, endNode = edge.end, weight = edge.attributes.couplingScore)
            inputFileString += "$inputLine\n"
        }

        writeInputFile(inputFileString)
    }

    private fun convertInfomapOutputToGraph(outputLines: List<String>): Graph {
        for (line in outputLines.filter { !it.startsWith('#') }) {
            val (nodeId, clusterId, _flowAmount) = line.split(' ')

            val clusteredNode = id2NodeMap[nodeId.toInt()]
                    ?: throw InternalError("Mapping the nodes to IDs for clustering failed")
            clusteredNode.attributes.cluster = clusterId.toInt()
            graph.updateNode(clusteredNode)
        }

        return graph
    }

    private fun writeInputFile(inputFileString: String) {
        val inputFile = File("$InputOutputPath/$InputFileName")
        inputFile.parentFile.mkdirs()
        inputFile.createNewFile()
        inputFile.bufferedWriter().use { file -> file.write(inputFileString) }
    }

    private fun readOutputFileLines(): List<String> {
        return File("$InputOutputPath/$OutputDirectory/$OutputFileName").bufferedReader().readLines()
    }

    private fun buildCommand(iterationAmount: Int): String {
        return "${retrieveInfomapExecutablePath()} $InputOutputPath/$InputFileName $InputOutputPath/$OutputDirectory/ -i link-list -N $iterationAmount --directed --clu"
    }

    private fun buildInfomapInputLine(startNode: Node, endNode: Node, weight: Int): String {
        return "${node2IdMap[startNode]} ${node2IdMap[endNode]} $weight"
    }

    private fun retrieveInfomapExecutablePath(): String {
        return "backend/src/main/resources/Infomap"
    }

    companion object {
        private const val InputOutputPath = "/tmp/steinmetz/infomap"
        private const val InputFileName = "steinmetz.txt"
        private const val OutputDirectory = "."
        private const val OutputFileName = "steinmetz.clu"
    }
}
