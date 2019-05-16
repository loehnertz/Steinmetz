package controller.analysis.metrics.clustering


data class ClusteringQuality(
        val amountClusters: Int,
        val amountInterfaceEdges: Int,
        val accumulatedInterfaceEdgeWeights: Int,
        val graphModularity: Double
)
