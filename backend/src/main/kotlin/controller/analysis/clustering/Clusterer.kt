package controller.analysis.clustering

import controller.analysis.clustering.chinesewhispers.ChineseWhispersManager
import controller.analysis.clustering.clausetnewmanmoore.ClausetNewmanMooreManager
import controller.analysis.clustering.infomap.InfomapManager
import controller.analysis.clustering.louvain.LouvainManager
import controller.analysis.clustering.mcl.MclManager
import controller.analysis.clustering.walktrap.WalktrapManager
import model.graph.EdgeAttributeWeights
import model.graph.Graph


class Clusterer(private val graph: Graph, private val projectName: String, private val edgeAttributeWeights: EdgeAttributeWeights) {
    fun applyEdgeWeighting() {
        graph.edges.forEach { it.attributes.couplingScore = EdgeWeightingFormulaCalculator.applyFormula(it.attributes, edgeAttributeWeights) }
    }

    fun applyClusteringAlgorithm(clusteringAlgorithm: ClusteringAlgorithm, maxIterations: Int): Graph {
        return when (clusteringAlgorithm) {
            ClusteringAlgorithm.MCL -> MclManager(graph).apply(iterations = maxIterations)
            ClusteringAlgorithm.INFOMAP -> InfomapManager(graph, projectName).apply(iterations = maxIterations)
            ClusteringAlgorithm.LOUVAIN -> LouvainManager(graph, projectName).apply(iterations = maxIterations)
            ClusteringAlgorithm.CLAUSET_NEWMAN_MOORE -> ClausetNewmanMooreManager(graph, projectName).apply(iterations = maxIterations)
            ClusteringAlgorithm.WALKTRAP -> WalktrapManager(graph, projectName).apply(iterations = maxIterations)
            ClusteringAlgorithm.CHINESE_WHISPERS -> ChineseWhispersManager(graph).apply(iterations = maxIterations)
        }
    }
}
