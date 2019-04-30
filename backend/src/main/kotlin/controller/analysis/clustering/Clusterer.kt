package controller.analysis.clustering

import controller.analysis.clustering.mcl.MclManager
import model.graph.Graph


class Clusterer(graph: Graph) {
    private val mclManager = MclManager(graph)

    fun applyMcl(clusteringInflationValue: Double?): Graph {
        return mclManager.applyMcl(clusteringInflationValue)
    }
}
