package controller.analysis.clustering.mcl

import controller.analysis.clustering.ClusteringAlgorithmManager
import controller.analysis.metrics.clustering.ClusteringQualityAnalyzer
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import model.graph.Graph
import model.graph.Node
import model.graph.NodeAttributes
import model.graph.Unit
import model.metrics.ClusteringQuality
import java.math.RoundingMode
import kotlin.reflect.KProperty1


class MclManager(private val graph: Graph, private val chosenClusteringMetric: KProperty1<ClusteringQuality, *>) : ClusteringAlgorithmManager {
    override fun apply(iterations: Int): Graph {
        val clusteringInflationValues: List<Double> = buildClusteringInflationValuesList()

        val input: String = convertGraphToInput()

        val deferredExecutions: ArrayList<Deferred<String>> = arrayListOf()
        for (clusteringInflationValue: Double in clusteringInflationValues) deferredExecutions.add(GlobalScope.async { MclExecutor(input, clusteringInflationValue).execute() })

        return runBlocking {
            val clusteredGraphs: List<Graph> = deferredExecutions.map { it.await() }.map { convertOutputToGraph(it, Graph(nodes = graph.nodes.map { node -> node.copy() }.toMutableSet(), edges = graph.edges)) }
            return@runBlocking clusteredGraphs.maxBy { (chosenClusteringMetric.get(ClusteringQualityAnalyzer(it).calculateClusteringQualityMetrics()) as Number).toDouble() }!!
        }
    }

    private fun convertGraphToInput(): String {
        return graph.edges.joinToString("\n") { buildInputLine(it.start, it.end, it.attributes.couplingScore) }
    }

    private fun convertOutputToGraph(output: String, graph: Graph): Graph {
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

    private fun buildClusteringInflationValuesList(iterations: Int = 100): List<Double> {
        var currentClusteringInflationValue = 1.2  // Lowest recommended value by the MCL author
        val clusteringInflationValues: ArrayList<Double> = arrayListOf()

        for (iteration: Int in (1..iterations)) {
            clusteringInflationValues.add(currentClusteringInflationValue.toBigDecimal().setScale(1, RoundingMode.DOWN).toDouble())
            currentClusteringInflationValue += 0.1
        }

        return clusteringInflationValues.toList()
    }

    private fun buildInputLine(startUnit: Unit, endUnit: Unit, weight: Int): String {
        return "${buildNodeIdentifier(startUnit.identifier, startUnit.packageIdentifier)}\t${buildNodeIdentifier(endUnit.identifier, endUnit.packageIdentifier)}\t$weight"
    }

    private fun buildNodeIdentifier(identifier: String, packageIdentifier: String): String {
        return "$packageIdentifier.$identifier"
    }
}
