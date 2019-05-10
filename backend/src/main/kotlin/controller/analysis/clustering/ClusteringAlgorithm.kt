package controller.analysis.clustering


enum class ClusteringAlgorithm {
    MCL,
    INFOMAP,
    LOUVAIN;

    companion object {
        fun getClusteringAlgorithmByName(name: String): ClusteringAlgorithm = valueOf(name.toUpperCase())
    }
}
