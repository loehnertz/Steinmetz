package controller.analysis.clustering.clausetnewmanmoore

import controller.analysis.clustering.ClusteringAlgorithmManager
import model.graph.*
import model.graph.Unit
import utility.Utilities
import java.io.File
import java.util.*


class ClausetNewmanMooreManager(private val graph: Graph, private val projectName: String) : ClusteringAlgorithmManager {
    private val unit2IdMap: HashMap<Unit, Int> = hashMapOf()
    private val id2UnitMap: HashMap<Int, Unit> = hashMapOf()

    override fun apply(iterations: Int): Graph {
        createInputFile()

        Runtime.getRuntime().exec(buildFirstRunCommand()).waitFor()
        val maximumModularity: Int = retrieveMaxModularityAfterFirstRun()
        Runtime.getRuntime().exec(buildSecondRunCommand(maximumModularity)).waitFor()

        val outputLines: List<String> = readOutputFileLines()
        return convertOutputToGraph(outputLines)
    }

    private fun createInputFile() {
        var inputFileString = ""
        var currentNodeId = 0

        @Suppress("UseWithIndex")
        for (node: Node in graph.nodes) {
            unit2IdMap[node.unit] = currentNodeId
            id2UnitMap[currentNodeId] = node.unit
            currentNodeId++
        }

        for (edge: Edge in graph.edges) {
            val inputLine = buildInputLine(startUnit = edge.start, endUnit = edge.end, weight = edge.attributes.couplingScore)
            inputFileString += "$inputLine\n"
        }

        writeInputFile(inputFileString)
    }

    private fun retrieveMaxModularityAfterFirstRun(): Int {
        val outputFile: List<String> = File("${constructBaseFileLocation()}-fc_$FirstRunIdentifier.info").bufferedReader().readLines()
        return outputFile.find { it.startsWith(InfoOutputFileMaxModularityPrefix) }!!.split("\t").last().toInt()
    }

    private fun convertOutputToGraph(outputLines: List<String>): Graph {
        var clusterId = 0

        for (line: String in outputLines) {
            if (line.startsWith(GroupOutputFileNewClusterPrefix)) {
                clusterId++
                continue
            }

            val clusteredUnit: Unit = id2UnitMap[line.toInt()]
                ?: throw InternalError("Mapping the nodes to IDs for clustering failed")
            graph.addOrUpdateNode(Node(unit = clusteredUnit, attributes = NodeAttributes(cluster = clusterId)))
        }

        return graph
    }

    private fun writeInputFile(inputFileString: String) {
        val inputFile = File("${constructBaseFileLocation()}.$InputFileExtension")
        inputFile.parentFile.mkdirs()
        inputFile.createNewFile()
        inputFile.bufferedWriter().use { file -> file.write(inputFileString) }
    }

    private fun readOutputFileLines(): List<String> {
        return File("${constructBaseFileLocation()}-fc_$SecondRunIdentifier.groups").bufferedReader().readLines()
    }

    private fun buildFirstRunCommand(): String {
        return "${retrieveExecutablePath()} -f ${constructBaseFileLocation()}.$InputFileExtension -l $FirstRunIdentifier"
    }

    private fun buildSecondRunCommand(maxModularity: Int): String {
        return "${retrieveExecutablePath()} -f ${constructBaseFileLocation()}.$InputFileExtension -l $SecondRunIdentifier -c $maxModularity"
    }

    private fun buildInputLine(startUnit: Unit, endUnit: Unit, weight: Int): String {
        return "${unit2IdMap[startUnit]}\t${unit2IdMap[endUnit]}\t$weight"
    }

    private fun constructBaseFileLocation(): String {
        return "$InputOutputPath/$projectName/$InputFileName"
    }

    private fun retrieveExecutablePath(): String {
        return Utilities.getExternalExecutableAsFile(ExecutableName).absolutePath
    }

    companion object {
        private const val ExecutableName = "FastCommunity_wMH"
        private const val InputOutputPath = "/tmp/steinmetz/clausetnewmanmoore"
        private const val InputFileName = "steinmetz"
        private const val InputFileExtension = "wpairs"
        private const val FirstRunIdentifier = "first"
        private const val SecondRunIdentifier = "second"
        private const val InfoOutputFileMaxModularityPrefix = "STEP------:"
        private const val GroupOutputFileNewClusterPrefix = "GROUP"
    }
}
