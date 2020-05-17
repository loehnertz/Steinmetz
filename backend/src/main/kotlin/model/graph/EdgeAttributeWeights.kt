package model.graph

import io.ktor.http.Parameters


data class EdgeAttributeWeights(
    val staticCouplingScoreWeight: Int = 1,
    val dynamicCouplingScoreWeight: Int = 1,
    val semanticCouplingScoreWeight: Int = 1,
    val evolutionaryCouplingScoreWeight: Int = 1
) {
    companion object {
        const val UpperBound = 8

        fun retrieveEdgeAttributeWeightsFromQueryParameters(queryParameters: Parameters): EdgeAttributeWeights {
            return EdgeAttributeWeights(
                staticCouplingScoreWeight = queryParameters[EdgeAttributeWeights::staticCouplingScoreWeight.name]?.toInt()
                                            ?: throw IllegalArgumentException("Passed query parameters do not contain the mandatory key ${EdgeAttributeWeights::staticCouplingScoreWeight.name}"),
                dynamicCouplingScoreWeight = queryParameters[EdgeAttributeWeights::dynamicCouplingScoreWeight.name]?.toInt()
                                             ?: throw IllegalArgumentException("Passed query parameters do not contain the mandatory key ${EdgeAttributeWeights::dynamicCouplingScoreWeight.name}"),
                semanticCouplingScoreWeight = queryParameters[EdgeAttributeWeights::semanticCouplingScoreWeight.name]?.toInt()
                                              ?: throw IllegalArgumentException("Passed query parameters do not contain the mandatory key ${EdgeAttributeWeights::semanticCouplingScoreWeight.name}"),
                evolutionaryCouplingScoreWeight = queryParameters[EdgeAttributeWeights::evolutionaryCouplingScoreWeight.name]?.toInt()
                                                  ?: throw IllegalArgumentException("Passed query parameters do not contain the mandatory key ${EdgeAttributeWeights::evolutionaryCouplingScoreWeight.name}")
            )
        }
    }
}
