package controller.analysis.clustering.walktrap

import controller.analysis.clustering.ClusteringAlgorithmManager
import controller.analysis.metrics.clustering.ClusteringQualityAnalyzer
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import model.graph.*
import model.graph.Unit
import model.metrics.ClusteringQuality
import java.io.File
import java.util.*
import kotlin.reflect.KProperty1


class WalktrapManager(private val graph: Graph, private val chosenClusteringMetric: KProperty1<ClusteringQuality, *>, private val projectName: String) : ClusteringAlgorithmManager {
    private val unit2IdMap: HashMap<Unit, Int> = hashMapOf()
    private val id2UnitMap: HashMap<Int, Unit> = hashMapOf()

    override fun apply(iterations: Int): Graph {
        val inputFile: File = createInputFile()

        val deferredExecutions: ArrayList<Deferred<String>> = arrayListOf()
        for (clusterAmount: Int in (1..iterations)) deferredExecutions.add(GlobalScope.async { WalktrapExecutor(inputFile, clusterAmount).execute() })

        return runBlocking {
            val clusteredGraphs: List<Graph> = deferredExecutions.map { it.await() }.mapNotNull { convertOutputToGraph(it, Graph(nodes = graph.nodes.map { node -> node.copy() }.toMutableSet(), edges = graph.edges)) }
            return@runBlocking clusteredGraphs.sortedByDescending { (chosenClusteringMetric.get(ClusteringQualityAnalyzer(it).calculateClusteringQualityMetrics()) as Number).toDouble() }.first()
        }
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

    private fun convertOutputToGraph(output: String, graph: Graph): Graph? {
        var clusterId = 1

        val outputLines: List<String> = output.substringAfterLast(OutputPartitionKeyword).split("\n")
        for (line: String in outputLines) {
            if (!line.startsWith(OutputLinePrefix) || !line.contains(" = ")) continue

            for (unitId: Int in convertOutputClusterLineToListOfUnitIds(line)) {
                val clusteredUnit: Unit = id2UnitMap[unitId]
                        ?: throw InternalError("Mapping the nodes to IDs for clustering failed")
                graph.addOrUpdateNode(Node(unit = clusteredUnit, attributes = NodeAttributes(cluster = clusterId)))
            }

            clusterId++
        }

        if (graph.nodes.map { it.attributes.cluster }.contains(null)) return null

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

    companion object {
        private const val InputOutputPath = "/tmp/steinmetz/walktrap"
        private const val InputFileName = "steinmetz"
        private const val InputFileExtension = "net"
        private const val OutputPartitionKeyword = "Partition"
        private const val OutputLinePrefix = "community"
    }
}
