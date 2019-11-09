package model.metrics

import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties


data class ClusteringQuality(
    val accumulatedEdgeWeights: Int,
    val amountClusters: Int,
    val amountInterfaceEdges: Int,
    val accumulatedInterfaceEdgeWeights: Int,
    val dynamicCouplingModularity: Double,
    val semanticCouplingModularity: Double,
    val evolutionaryCouplingModularity: Double,
    val averageCouplingModularity: Double,
    val totalCouplingModularity: Double
) {
    companion object {
        fun getClusteringMetricByName(name: String): KProperty1<ClusteringQuality, *> {
            return ClusteringQuality::class.declaredMemberProperties.find { it.name == name }
                ?: throw IllegalArgumentException("Clustering metric $name does not exist")
        }
    }
}
