package controller.analysis.metrics

import model.graph.Edge
import model.graph.Graph
import model.graph.Node
import model.graph.Unit
import model.metrics.ClusteringQuality
import model.metrics.InputQuality


abstract class MetricsManager {
    abstract fun calculateInputMetrics(staticAnalysisGraph: Graph, dynamicAnalysisGraph: Graph, mergedGraph: Graph): InputQuality

    fun calculateClusteringMetrics(clusteredGraph: Graph): ClusteringQuality {
        val amountOfClusters: Int = clusteredGraph.nodes.map { it.attributes.cluster }.toSet().size
        val interClusterEdges: Set<Edge> = clusteredGraph.edges.filter { isInterClusterEdge(clusteredGraph, it) }.toSet()
        val accumulatedInterfaceEdgeWeights: Int = interClusterEdges.sumBy { it.attributes.couplingScore }
        val graphModularity: Double = calculateGraphModularity(clusteredGraph)

        return ClusteringQuality(
                amountClusters = amountOfClusters,
                amountInterfaceEdges = interClusterEdges.size,
                accumulatedInterfaceEdgeWeights = accumulatedInterfaceEdgeWeights,
                graphModularity = graphModularity
        )
    }

    private fun isInterClusterEdge(clusteredGraph: Graph, edge: Edge): Boolean {
        return findNodeInClusteredGraph(clusteredGraph, edge.start).attributes.cluster != findNodeInClusteredGraph(clusteredGraph, edge.end).attributes.cluster
    }

    private fun findNodeInClusteredGraph(clusteredGraph: Graph, unit: Unit): Node {
        return clusteredGraph.nodes.first { it.unit == unit }
    }

    private fun calculateGraphModularity(clusteredGraph: Graph): Double {
        val modularitySummands: ArrayList<Double> = arrayListOf()
        val totalEdgeAmount: Double = clusteredGraph.edges.size.toDouble()
        val clusterMap: Map<Int, List<Node>> = buildClusterMap(clusteredGraph)

        for ((_, nodes: List<Node>) in clusterMap) {
            val unitsOfCurrentCluster: List<Unit> = nodes.map { node -> node.unit }

            val edgesStartingAndEndingInCurrentCluster: List<Edge> = clusteredGraph.edges.filter { edge -> unitsOfCurrentCluster.contains(edge.start) && unitsOfCurrentCluster.contains(edge.end) }
            val eii: Double = ((edgesStartingAndEndingInCurrentCluster.size).toDouble() / totalEdgeAmount)

            val edgesEndingInCurrentCluster: List<Edge> = clusteredGraph.edges.filter { edge -> unitsOfCurrentCluster.contains(edge.end) }
            val aiSquared: Double = Math.pow(((edgesEndingInCurrentCluster.size).toDouble() / totalEdgeAmount), 2.0)

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
}
