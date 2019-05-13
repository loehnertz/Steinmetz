package controller.analysis.clustering

import controller.analysis.clustering.clausetnewmanmoore.ClausetNewmanMooreManager
import controller.analysis.clustering.infomap.InfomapManager
import controller.analysis.clustering.louvain.LouvainManager
import controller.analysis.clustering.mcl.MclManager
import model.graph.Graph


class Clusterer(private val graph: Graph, private val projectName: String) {
    fun applyClusteringAlgorithm(clusteringAlgorithm: ClusteringAlgorithm, tunableClusteringParameter: Double?): Graph {
        return when (clusteringAlgorithm) {
            ClusteringAlgorithm.MCL -> MclManager(graph).apply(tunableParameter = tunableClusteringParameter)
            ClusteringAlgorithm.INFOMAP -> InfomapManager(graph, projectName).apply(tunableParameter = tunableClusteringParameter)
            ClusteringAlgorithm.LOUVAIN -> LouvainManager(graph, projectName).apply(tunableParameter = tunableClusteringParameter)
            ClusteringAlgorithm.CLAUSET_NEWMAN_MOORE -> ClausetNewmanMooreManager(graph, projectName).apply(tunableParameter = tunableClusteringParameter)
        }
    }
}
