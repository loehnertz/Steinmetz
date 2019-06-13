package model.graph

import io.ktor.http.Parameters


data class EdgeAttributeWeights(
        val dynamicCouplingScoreWeight: Int = 1,
        val semanticCouplingScoreWeight: Int = 1,
        val logicalCouplingScoreWeight: Int = 1
) {
    fun calculateWeightSum(): Int = (dynamicCouplingScoreWeight + semanticCouplingScoreWeight + logicalCouplingScoreWeight)

    companion object {
        fun retrieveEdgeAttributeWeightsFromQueryParameters(queryParameters: Parameters): EdgeAttributeWeights {
            return EdgeAttributeWeights(
                    dynamicCouplingScoreWeight = queryParameters[EdgeAttributeWeights::dynamicCouplingScoreWeight.name]?.toInt()
                            ?: throw IllegalArgumentException("Passed query parameters do not contain the mandatory key ${EdgeAttributeWeights::dynamicCouplingScoreWeight.name}"),
                    semanticCouplingScoreWeight = queryParameters[EdgeAttributeWeights::semanticCouplingScoreWeight.name]?.toInt()
                            ?: throw IllegalArgumentException("Passed query parameters do not contain the mandatory key ${EdgeAttributeWeights::semanticCouplingScoreWeight.name}"),
                    logicalCouplingScoreWeight = queryParameters[EdgeAttributeWeights::logicalCouplingScoreWeight.name]?.toInt()
                            ?: throw IllegalArgumentException("Passed query parameters do not contain the mandatory key ${EdgeAttributeWeights::logicalCouplingScoreWeight.name}")
            )
        }
    }
}
