package controller.analysis.optimization.moea

import controller.analysis.clustering.Clusterer
import controller.analysis.clustering.ClusteringAlgorithm
import controller.analysis.metrics.clustering.ClusteringQualityAnalyzer
import model.graph.EdgeAttributeWeights
import model.graph.Graph
import model.metrics.ClusteringQuality
import org.moeaframework.core.Solution
import org.moeaframework.core.variable.EncodingUtils
import org.moeaframework.problem.AbstractProblem
import kotlin.reflect.KProperty1

class Optimizer(variableAmount: Int, objectiveAmount: Int, private val averagingClusteringMetric: KProperty1<ClusteringQuality, Double>, private val chosenClusteringMetrics: Collection<KProperty1<ClusteringQuality, Double>>, private val fitnessFunction: (Solution) -> ClusteringQuality) : AbstractProblem(variableAmount, objectiveAmount) {
    override fun evaluate(solution: Solution) {
        val result: ClusteringQuality = fitnessFunction(solution)
        solution.setAttribute(sortingMetric, averagingClusteringMetric.get(result))
        chosenClusteringMetrics.forEachIndexed { index, metric -> solution.setObjective(index, -metric.get(result)) }
    }

    override fun newSolution(): Solution {
        val solution = Solution(numberOfVariables, numberOfObjectives)
        for (index in 0 until numberOfVariables) solution.setVariable(index, EncodingUtils.newReal(0.0, 1.0))
        return solution
    }

    companion object {
        const val sortingMetric = "sortingMetric"

        val couplingModularityMetrics: List<KProperty1<ClusteringQuality, Double>> = listOf(
            ClusteringQuality::staticCouplingModularity,
            // ClusteringQuality::dynamicCouplingModularity,
            ClusteringQuality::semanticCouplingModularity,
            ClusteringQuality::evolutionaryCouplingModularity
        )
        val clusterFactorMetrics: List<KProperty1<ClusteringQuality, Double>> = listOf(
            ClusteringQuality::staticMeanClusterFactor,
            // ClusteringQuality::dynamicMeanClusterFactor,
            ClusteringQuality::semanticMeanClusterFactor,
            ClusteringQuality::evolutionaryMeanClusterFactor
        )

        val sortingComparator = Comparator<Solution> { solutionA, solutionB -> (solutionA.getAttribute(sortingMetric) as Double).compareTo((solutionB.getAttribute(sortingMetric) as Double)) }

        fun buildOptimizationFitnessFunction(projectName: String, projectGraph: Graph, chosenClusteringMetric: KProperty1<ClusteringQuality, *>, clusteringAlgorithm: ClusteringAlgorithm, maxIterations: Int): (Solution) -> ClusteringQuality {
            return ({ solution: Solution ->
                val edgeAttributeWeights = EdgeAttributeWeights(
                    staticCouplingScoreWeight = EncodingUtils.getReal(solution.getVariable(0)),
                    dynamicCouplingScoreWeight = 0.0,
                    semanticCouplingScoreWeight = EncodingUtils.getReal(solution.getVariable(1)),
                    evolutionaryCouplingScoreWeight = EncodingUtils.getReal(solution.getVariable(2))
                )
                val clusterer: Clusterer = Clusterer(projectGraph.clone(), projectName, chosenClusteringMetric, edgeAttributeWeights).also { it.applyEdgeWeighting() }
                val clusteredGraph: Graph = clusterer.applyClusteringAlgorithm(clusteringAlgorithm, maxIterations)
                ClusteringQualityAnalyzer(clusteredGraph).calculateClusteringQualityMetrics()
            })
        }
    }
}
