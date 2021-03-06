package model.metrics


data class InputQuality(
    val dynamicAnalysis: Int,
    val semanticAnalysis: Int,
    val evolutionaryAnalysis: Int
)
