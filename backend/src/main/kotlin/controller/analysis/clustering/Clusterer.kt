package controller.analysis.clustering

import controller.analysis.clustering.infomap.InfomapManager
import controller.analysis.clustering.louvain.LouvainManager
import controller.analysis.clustering.mcl.MclManager
import model.graph.Graph


class Clusterer(graph: Graph, projectName: String) {
    private val mclManager = MclManager(graph)
    private val infomapManager = InfomapManager(graph)
    private val louvainManager = LouvainManager(graph, projectName)

    fun applyClusteringAlgorithm(clusteringAlgorithm: ClusteringAlgorithm, tunableClusteringParameter: Double?): Graph {
        return when (clusteringAlgorithm) {
            ClusteringAlgorithm.MCL -> applyMcl(tunableClusteringParameter)
            ClusteringAlgorithm.INFOMAP -> applyInfomap(tunableClusteringParameter?.toInt())
            ClusteringAlgorithm.LOUVAIN -> applyLouvain()
        }
    }

    private fun applyMcl(clusteringInflationValue: Double?): Graph {
        return mclManager.applyMcl(clusteringInflationValue)
    }

    private fun applyInfomap(iterationAmount: Int?): Graph {
        return if (iterationAmount != null) {
            infomapManager.applyInfomap(iterationAmount)
        } else {
            infomapManager.applyInfomap()
        }
    }

    private fun applyLouvain(): Graph {
        return louvainManager.applyLouvain()
    }
}
