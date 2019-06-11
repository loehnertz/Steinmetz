package controller.analysis.metrics

import model.graph.Edge
import model.graph.Graph
import model.graph.Node
import model.graph.Unit


abstract class MetricsManager {
    fun isInterClusterEdge(clusteredGraph: Graph, edge: Edge): Boolean {
        return findNodeInClusteredGraph(clusteredGraph, edge.start).attributes.cluster != findNodeInClusteredGraph(clusteredGraph, edge.end).attributes.cluster
    }

    fun calculateGraphModularity(clusteredGraph: Graph): Double {
        val modularitySummands: ArrayList<Double> = arrayListOf()
        val totalEdgeAmount: Double = clusteredGraph.edges.size.toDouble()
        val clusterMap: Map<Int, List<Node>> = buildClusterMap(clusteredGraph)

        for ((_, nodes: List<Node>) in clusterMap) {
            val unitsOfCurrentCluster: List<Unit> = nodes.map { it.unit }

            val edgesStartingAndEndingInCurrentCluster: List<Edge> = clusteredGraph.edges.filter { unitsOfCurrentCluster.contains(it.start) && unitsOfCurrentCluster.contains(it.end) }
            val eii: Double = (edgesStartingAndEndingInCurrentCluster.size.toDouble() / totalEdgeAmount)

            val edgesEndingInCurrentCluster: List<Edge> = clusteredGraph.edges.filter { unitsOfCurrentCluster.contains(it.end) }
            val aiSquared: Double = Math.pow((edgesEndingInCurrentCluster.size.toDouble() / totalEdgeAmount), 2.0)

            modularitySummands.add((eii - aiSquared))
        }

        return modularitySummands.sum()
    }

    fun buildClusterMap(clusteredGraph: Graph): Map<Int, List<Node>> {
        val clusterMap: MutableMap<Int, ArrayList<Node>> = mutableMapOf()

        for (node: Node in clusteredGraph.nodes) {
            var clusterList: ArrayList<Node>? = clusterMap[node.attributes.cluster!!]
            if (clusterList == null) clusterList = arrayListOf()
            clusterList.add(node)
            clusterMap[node.attributes.cluster!!] = clusterList
        }

        return clusterMap.toMap()
    }

    private fun findNodeInClusteredGraph(clusteredGraph: Graph, unit: Unit): Node {
        return clusteredGraph.nodes.first { it.unit == unit }
    }
}
