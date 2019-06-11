package model.metrics


data class ClusteringQuality(
        val accumulatedEdgeWeights: Int,
        val amountClusters: Int,
        val amountInterfaceEdges: Int,
        val accumulatedInterfaceEdgeWeights: Int,
        val graphModularity: Double,
        val dynamicCouplingModularity: Double,
        val semanticCouplingModularity: Double,
        val logicalCouplingModularity: Double,
        val totalCouplingModularity: Double
)
