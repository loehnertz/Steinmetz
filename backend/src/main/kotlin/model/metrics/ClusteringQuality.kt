package model.metrics


data class ClusteringQuality(
        val accumulatedEdgeWeights: Int,
        val amountClusters: Int,
        val amountInterfaceEdges: Int,
        val accumulatedInterfaceEdgeWeights: Int,
        val graphModularity: Double
)
