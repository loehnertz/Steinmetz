package controller.analysis.clustering

import controller.analysis.clustering.infomap.InfomapManager
import controller.analysis.clustering.mcl.MclManager
import model.graph.Graph


class Clusterer(graph: Graph) {
    private val mclManager = MclManager(graph)
    private val infomapManager = InfomapManager(graph)

    fun applyMcl(clusteringInflationValue: Double?): Graph {
        return mclManager.applyMcl(clusteringInflationValue)
    }

    fun applyInfomap(iterationAmount: Int?): Graph {
        return if (iterationAmount != null) {
            infomapManager.applyInfomap(iterationAmount)
        } else {
            infomapManager.applyInfomap()
        }
    }
}
