package controller.analysis.metrics.clustering

import model.graph.*
import model.graph.Unit
import model.metrics.ClusteringQuality
import utility.sumByLong
import kotlin.math.pow
import kotlin.reflect.KMutableProperty1


class ClusteringQualityAnalyzer(private val clusteredGraph: Graph) {
    fun calculateClusteringQualityMetrics(): ClusteringQuality {
        val accumulatedEdgeWeights: Long = clusteredGraph.edges.sumByLong { it.attributes.couplingScore }
        val amountOfClusters: Int = clusteredGraph.nodes.map { it.attributes.cluster }.toSet().size
        val interClusterEdges: Set<Edge> = clusteredGraph.edges.filter { isInterClusterEdge(clusteredGraph, it) }.toSet()
        val accumulatedInterfaceEdgeWeights: Long = interClusterEdges.sumByLong { it.attributes.couplingScore }
        val staticCouplingModularity: Double = calculateGraphCouplingModularity(EdgeAttributes::staticCouplingScore)
        val dynamicCouplingModularity: Double = calculateGraphCouplingModularity(EdgeAttributes::dynamicCouplingScore)
        val semanticCouplingModularity: Double = calculateGraphCouplingModularity(EdgeAttributes::semanticCouplingScore)
        val evolutionaryCouplingModularity: Double = calculateGraphCouplingModularity(EdgeAttributes::evolutionaryCouplingScore)
        val averageCouplingModularity: Double = listOf(staticCouplingModularity, dynamicCouplingModularity, semanticCouplingModularity, evolutionaryCouplingModularity).filter { !it.isNaN() && it != 0.0 }.average()
        val totalCouplingModularity: Double = calculateGraphCouplingModularity(EdgeAttributes::couplingScore)
        val staticMeanClusterFactor: Double = calculateGraphMeanClusterFactor(EdgeAttributes::staticCouplingScore)
        val dynamicMeanClusterFactor: Double = calculateGraphMeanClusterFactor(EdgeAttributes::dynamicCouplingScore)
        val semanticMeanClusterFactor: Double = calculateGraphMeanClusterFactor(EdgeAttributes::semanticCouplingScore)
        val evolutionaryMeanClusterFactor: Double = calculateGraphMeanClusterFactor(EdgeAttributes::evolutionaryCouplingScore)
        val averageMeanClusterFactor: Double = listOf(staticMeanClusterFactor, dynamicMeanClusterFactor, semanticMeanClusterFactor, evolutionaryMeanClusterFactor).filter { !it.isNaN() && it != 0.0 }.average()
        val totalMeanClusterFactor: Double = calculateGraphMeanClusterFactor(EdgeAttributes::couplingScore)

        return ClusteringQuality(
            accumulatedEdgeWeights = accumulatedEdgeWeights,
            amountClusters = amountOfClusters,
            amountInterfaceEdges = interClusterEdges.size,
            accumulatedInterfaceEdgeWeights = accumulatedInterfaceEdgeWeights,
            dynamicCouplingModularity = dynamicCouplingModularity,
            staticCouplingModularity = staticCouplingModularity,
            semanticCouplingModularity = semanticCouplingModularity,
            evolutionaryCouplingModularity = evolutionaryCouplingModularity,
            averageCouplingModularity = averageCouplingModularity,
            totalCouplingModularity = totalCouplingModularity,
            dynamicMeanClusterFactor = dynamicMeanClusterFactor,
            staticMeanClusterFactor = staticMeanClusterFactor,
            semanticMeanClusterFactor = semanticMeanClusterFactor,
            evolutionaryMeanClusterFactor = evolutionaryMeanClusterFactor,
            averageMeanClusterFactor = averageMeanClusterFactor,
            totalMeanClusterFactor = totalMeanClusterFactor
        )
    }

    private fun calculateGraphCouplingModularity(couplingScoreType: KMutableProperty1<EdgeAttributes, Int>): Double {
        val modularitySummands: ArrayList<Double> = arrayListOf()
        val totalCouplingScoreTypeAmount: Long = clusteredGraph.edges.sumByLong { couplingScoreType.get(it.attributes) }

        val clusterMap: Map<Int, List<Node>> = buildClusterMap(clusteredGraph)
        for ((_, nodes: List<Node>) in clusterMap) {
            val unitsOfCurrentCluster: List<Unit> = nodes.map { it.unit }

            val edgesStartingAndEndingInCurrentCluster: List<Edge> = clusteredGraph.edges.filter { unitsOfCurrentCluster.contains(it.start) && unitsOfCurrentCluster.contains(it.end) }
            val eii: Double = (edgesStartingAndEndingInCurrentCluster.sumByLong { couplingScoreType.get(it.attributes) }.toDouble() / totalCouplingScoreTypeAmount)

            val edgesEndingInCurrentCluster: List<Edge> = clusteredGraph.edges.filter { unitsOfCurrentCluster.contains(it.end) }
            val aiSquared: Double = (edgesEndingInCurrentCluster.sumByLong { couplingScoreType.get(it.attributes) }.toDouble() / totalCouplingScoreTypeAmount).pow(2.0)

            modularitySummands.add((eii - aiSquared))
        }

        return modularitySummands.sum()
    }

    private fun calculateGraphMeanClusterFactor(couplingScoreType: KMutableProperty1<EdgeAttributes, Int>): Double {
        val clusterFactorSummands: ArrayList<Double> = arrayListOf()

        val clusterMap: Map<Int, List<Node>> = buildClusterMap(clusteredGraph)
        for ((_, nodes: List<Node>) in clusterMap) {
            val unitsOfCurrentCluster: List<Unit> = nodes.map { it.unit }

            val intraEdges: List<Edge> = clusteredGraph.edges.filter { unitsOfCurrentCluster.contains(it.start) and unitsOfCurrentCluster.contains(it.end) }
            val intraEdgesWeight: Double = intraEdges.sumByLong { couplingScoreType.get(it.attributes) }.toDouble()

            val interEdges: List<Edge> = clusteredGraph.edges.filter { unitsOfCurrentCluster.contains(it.start) xor unitsOfCurrentCluster.contains(it.end) }
            val interEdgesWeight: Double = interEdges.sumByLong { couplingScoreType.get(it.attributes) }.toDouble()

            if (intraEdgesWeight == 0.0) {
                clusterFactorSummands.add(0.0)
            } else {
                clusterFactorSummands.add((intraEdgesWeight / (intraEdgesWeight + 0.5 * interEdgesWeight)))
            }
        }

        return clusterFactorSummands.average()
    }

    private fun buildClusterMap(clusteredGraph: Graph): Map<Int, List<Node>> {
        val clusterMap: MutableMap<Int, ArrayList<Node>> = mutableMapOf()

        for (node: Node in clusteredGraph.nodes) {
            if (node.attributes.cluster == null) continue
            var clusterList: ArrayList<Node>? = clusterMap[node.attributes.cluster!!]
            if (clusterList == null) clusterList = arrayListOf()
            clusterList.add(node)
            clusterMap[node.attributes.cluster!!] = clusterList
        }

        return clusterMap.toMap()
    }

    private fun isInterClusterEdge(clusteredGraph: Graph, edge: Edge): Boolean {
        return findNodeInClusteredGraph(clusteredGraph, edge.start).attributes.cluster != findNodeInClusteredGraph(clusteredGraph, edge.end).attributes.cluster
    }

    private fun findNodeInClusteredGraph(clusteredGraph: Graph, unit: Unit): Node {
        return clusteredGraph.nodes.first { it.unit == unit }
    }
}
