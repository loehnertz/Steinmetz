package controller.analysis.metrics.clustering

import model.graph.*
import model.graph.Unit
import model.metrics.ClusteringQuality
import kotlin.reflect.KMutableProperty1


class ClusteringQualityAnalyzer(private val clusteredGraph: Graph) {
    fun calculateClusteringQualityMetrics(): ClusteringQuality {
        val accumulatedEdgeWeights: Int = clusteredGraph.edges.sumBy { it.attributes.couplingScore }
        val amountOfClusters: Int = clusteredGraph.nodes.map { it.attributes.cluster }.toSet().size
        val interClusterEdges: Set<Edge> = clusteredGraph.edges.filter { isInterClusterEdge(clusteredGraph, it) }.toSet()
        val accumulatedInterfaceEdgeWeights: Int = interClusterEdges.sumBy { it.attributes.couplingScore }
        val dynamicCouplingModularity: Double = calculateGraphCouplingModularity(EdgeAttributes::dynamicCouplingScore)
        val semanticCouplingModularity: Double = calculateGraphCouplingModularity(EdgeAttributes::semanticCouplingScore)
        val logicalCouplingModularity: Double = calculateGraphCouplingModularity(EdgeAttributes::logicalCouplingScore)
        val averageCouplingModularity: Double = listOf(dynamicCouplingModularity, semanticCouplingModularity, logicalCouplingModularity).average()
        val totalCouplingModularity: Double = calculateGraphCouplingModularity(EdgeAttributes::couplingScore)

        return ClusteringQuality(
            accumulatedEdgeWeights = accumulatedEdgeWeights,
            amountClusters = amountOfClusters,
            amountInterfaceEdges = interClusterEdges.size,
            accumulatedInterfaceEdgeWeights = accumulatedInterfaceEdgeWeights,
            dynamicCouplingModularity = dynamicCouplingModularity,
            semanticCouplingModularity = semanticCouplingModularity,
            logicalCouplingModularity = logicalCouplingModularity,
            averageCouplingModularity = averageCouplingModularity,
            totalCouplingModularity = totalCouplingModularity
        )
    }

    private fun calculateGraphCouplingModularity(couplingScoreType: KMutableProperty1<EdgeAttributes, Int>): Double {
        val modularitySummands: ArrayList<Double> = arrayListOf()
        val totalCouplingScoreTypeAmount: Double = clusteredGraph.edges.sumBy { couplingScoreType.get(it.attributes) }.toDouble()
        val clusterMap: Map<Int, List<Node>> = buildClusterMap(clusteredGraph)

        for ((_, nodes: List<Node>) in clusterMap) {
            val unitsOfCurrentCluster: List<Unit> = nodes.map { it.unit }

            val edgesStartingAndEndingInCurrentCluster: List<Edge> = clusteredGraph.edges.filter { unitsOfCurrentCluster.contains(it.start) && unitsOfCurrentCluster.contains(it.end) }
            val eii: Double = (edgesStartingAndEndingInCurrentCluster.sumBy { couplingScoreType.get(it.attributes) }.toDouble() / totalCouplingScoreTypeAmount)

            val edgesEndingInCurrentCluster: List<Edge> = clusteredGraph.edges.filter { unitsOfCurrentCluster.contains(it.end) }
            val aiSquared: Double = Math.pow((edgesEndingInCurrentCluster.sumBy { couplingScoreType.get(it.attributes) }.toDouble() / totalCouplingScoreTypeAmount), 2.0)

            modularitySummands.add((eii - aiSquared))
        }

        return modularitySummands.sum()
    }

    private fun buildClusterMap(clusteredGraph: Graph): Map<Int, List<Node>> {
        val clusterMap: MutableMap<Int, ArrayList<Node>> = mutableMapOf()

        for (node: Node in clusteredGraph.nodes) {
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
