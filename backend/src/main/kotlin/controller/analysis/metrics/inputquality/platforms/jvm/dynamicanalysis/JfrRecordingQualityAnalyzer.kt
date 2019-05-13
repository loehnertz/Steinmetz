package controller.analysis.metrics.inputquality.platforms.jvm.dynamicanalysis

import controller.analysis.metrics.inputquality.DynamicAnalysisInputQualityAnalyzer
import model.graph.Graph
import model.graph.Unit


object JfrRecordingQualityAnalyzer : DynamicAnalysisInputQualityAnalyzer {
    override fun calculateQuality(staticAnalysisGraph: Graph, dynamicAnalysisGraph: Graph): Int {
        val staticAnalysisCoveredUnits: Set<Unit> = staticAnalysisGraph.nodes.map { it.unit }.toSet()
        val dynamicAnalysisCoveredUnits: Set<Unit> = dynamicAnalysisGraph.nodes.map { it.unit }.toSet()
        val intersectionCoveredUnits: Set<Unit> = staticAnalysisCoveredUnits.intersect(dynamicAnalysisCoveredUnits)

        val coveredUnitsDifference: Double = Math.abs(staticAnalysisCoveredUnits.size - intersectionCoveredUnits.size).toDouble()
        return (100 - ((coveredUnitsDifference / staticAnalysisCoveredUnits.size) * 100)).toInt()
    }
}
