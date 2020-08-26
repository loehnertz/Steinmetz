package model.graph

import io.ktor.http.Parameters


data class EdgeAttributeWeights(
    val staticCouplingScoreWeight: Double = 1.0,
    val dynamicCouplingScoreWeight: Double = 1.0,
    val semanticCouplingScoreWeight: Double = 1.0,
    val evolutionaryCouplingScoreWeight: Double = 1.0
) {
    fun sum() = this.staticCouplingScoreWeight + this.dynamicCouplingScoreWeight + this.semanticCouplingScoreWeight + this.evolutionaryCouplingScoreWeight

    companion object {
        fun retrieveEdgeAttributeWeightsFromQueryParameters(queryParameters: Parameters): EdgeAttributeWeights {
            return EdgeAttributeWeights(
                staticCouplingScoreWeight = queryParameters[EdgeAttributeWeights::staticCouplingScoreWeight.name]?.toDouble()
                                            ?: throw IllegalArgumentException("Passed query parameters do not contain the mandatory key ${EdgeAttributeWeights::staticCouplingScoreWeight.name}"),
                dynamicCouplingScoreWeight = queryParameters[EdgeAttributeWeights::dynamicCouplingScoreWeight.name]?.toDouble()
                                             ?: throw IllegalArgumentException("Passed query parameters do not contain the mandatory key ${EdgeAttributeWeights::dynamicCouplingScoreWeight.name}"),
                semanticCouplingScoreWeight = queryParameters[EdgeAttributeWeights::semanticCouplingScoreWeight.name]?.toDouble()
                                              ?: throw IllegalArgumentException("Passed query parameters do not contain the mandatory key ${EdgeAttributeWeights::semanticCouplingScoreWeight.name}"),
                evolutionaryCouplingScoreWeight = queryParameters[EdgeAttributeWeights::evolutionaryCouplingScoreWeight.name]?.toDouble()
                                                  ?: throw IllegalArgumentException("Passed query parameters do not contain the mandatory key ${EdgeAttributeWeights::evolutionaryCouplingScoreWeight.name}")
            )
        }
    }
}
