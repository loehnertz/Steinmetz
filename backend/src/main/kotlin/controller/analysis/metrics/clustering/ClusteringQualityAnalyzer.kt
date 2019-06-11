package controller.analysis.metrics.clustering

import controller.analysis.metrics.MetricsManager
import model.graph.*
import model.graph.Unit
import model.metrics.ClusteringQuality
import kotlin.reflect.KMutableProperty1


class ClusteringQualityAnalyzer(private val clusteredGraph: Graph) : MetricsManager() {
    fun calculateClusteringQualityMetrics(): ClusteringQuality {
        val accumulatedEdgeWeights: Int = clusteredGraph.edges.sumBy { it.attributes.couplingScore }
        val amountOfClusters: Int = clusteredGraph.nodes.map { it.attributes.cluster }.toSet().size
        val interClusterEdges: Set<Edge> = clusteredGraph.edges.filter { isInterClusterEdge(clusteredGraph, it) }.toSet()
        val accumulatedInterfaceEdgeWeights: Int = interClusterEdges.sumBy { it.attributes.couplingScore }
        val graphModularity: Double = calculateGraphModularity(clusteredGraph)
        val dynamicCouplingModularity: Double = calculateGraphCouplingModularity(EdgeAttributes::dynamicCouplingScore)
        val semanticCouplingModularity: Double = calculateGraphCouplingModularity(EdgeAttributes::semanticCouplingScore)
        val logicalCouplingModularity: Double = calculateGraphCouplingModularity(EdgeAttributes::logicalCouplingScore)
        val totalCouplingModularity: Double = calculateGraphCouplingModularity(EdgeAttributes::couplingScore)

        return ClusteringQuality(
                accumulatedEdgeWeights = accumulatedEdgeWeights,
                amountClusters = amountOfClusters,
                amountInterfaceEdges = interClusterEdges.size,
                accumulatedInterfaceEdgeWeights = accumulatedInterfaceEdgeWeights,
                graphModularity = graphModularity,
                dynamicCouplingModularity = dynamicCouplingModularity,
                semanticCouplingModularity = semanticCouplingModularity,
                logicalCouplingModularity = logicalCouplingModularity,
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
}
