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

    fun applyClusteringAlgorithm(clusteringAlgorithm: ClusteringAlgorithm, tunableClusteringParameter: Double?): Graph {
        return when (clusteringAlgorithm) {
            ClusteringAlgorithm.MCL -> MclManager(graph).apply(tunableParameter = tunableClusteringParameter)
            ClusteringAlgorithm.INFOMAP -> InfomapManager(graph, projectName).apply(tunableParameter = tunableClusteringParameter)
            ClusteringAlgorithm.LOUVAIN -> LouvainManager(graph, projectName).apply(tunableParameter = tunableClusteringParameter)
            ClusteringAlgorithm.CLAUSET_NEWMAN_MOORE -> ClausetNewmanMooreManager(graph, projectName).apply(tunableParameter = tunableClusteringParameter)
            ClusteringAlgorithm.WALKTRAP -> WalktrapManager(graph, projectName).apply(tunableParameter = tunableClusteringParameter)
            ClusteringAlgorithm.CHINESE_WHISPERS -> ChineseWhispersManager(graph).apply(tunableParameter = tunableClusteringParameter)
        }
    }
}
