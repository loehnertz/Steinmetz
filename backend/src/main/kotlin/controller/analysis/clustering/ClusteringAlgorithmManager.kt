package controller.analysis.clustering

import model.graph.Graph


interface ClusteringAlgorithmManager {
    fun apply(iterations: Int): Graph
}
