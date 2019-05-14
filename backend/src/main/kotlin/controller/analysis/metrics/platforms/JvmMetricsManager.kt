package controller.analysis.metrics.platforms

import controller.analysis.metrics.clustering.ClusteringQuality
import controller.analysis.metrics.input.InputQuality
import controller.analysis.metrics.input.platforms.jvm.JvmInputQualityAnalyzer
import model.graph.Edge
import model.graph.Graph
import model.graph.Node
import model.graph.Unit


object JvmMetricsManager {
    fun calculateInputMetrics(staticAnalysisGraph: Graph, dynamicAnalysisGraph: Graph): InputQuality {
        return JvmInputQualityAnalyzer(staticAnalysisGraph, dynamicAnalysisGraph).calculate()
    }

    fun calculateClusteringMetrics(clusteredGraph: Graph): ClusteringQuality {
        val amountOfClusters: Int = clusteredGraph.nodes.map { it.attributes.cluster }.toSet().size
        val amountOfInterfaceEdges: Int = clusteredGraph.edges.filter { isInterClusterEdge(clusteredGraph, it) }.toSet().size
        return ClusteringQuality(amountClusters = amountOfClusters, amountInterfaceEdges = amountOfInterfaceEdges)
    }

    private fun isInterClusterEdge(clusteredGraph: Graph, edge: Edge): Boolean {
        return findNodeInClusteredGraph(clusteredGraph, edge.start).attributes.cluster != findNodeInClusteredGraph(clusteredGraph, edge.end).attributes.cluster
    }

    private fun findNodeInClusteredGraph(clusteredGraph: Graph, unit: Unit): Node {
        return clusteredGraph.nodes.first { it.unit == unit }
    }
}
