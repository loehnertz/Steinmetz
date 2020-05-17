package model.metrics

import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties


data class ClusteringQuality(
    val accumulatedEdgeWeights: Long,
    val amountClusters: Int,
    val amountInterfaceEdges: Int,
    val accumulatedInterfaceEdgeWeights: Long,
    val staticCouplingModularity: Double,
    val dynamicCouplingModularity: Double,
    val semanticCouplingModularity: Double,
    val evolutionaryCouplingModularity: Double,
    val averageCouplingModularity: Double,
    val totalCouplingModularity: Double,
    val staticMeanClusterFactor: Double,
    val dynamicMeanClusterFactor: Double,
    val semanticMeanClusterFactor: Double,
    val evolutionaryMeanClusterFactor: Double,
    val averageMeanClusterFactor: Double,
    val totalMeanClusterFactor: Double
) {
    companion object {
        fun getClusteringMetricByName(name: String): KProperty1<ClusteringQuality, *> {
            return ClusteringQuality::class.declaredMemberProperties.find { it.name == name }
                   ?: throw IllegalArgumentException("Clustering metric $name does not exist")
        }
    }
}
