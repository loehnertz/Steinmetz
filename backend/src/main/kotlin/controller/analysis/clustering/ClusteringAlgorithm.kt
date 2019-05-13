package controller.analysis.clustering


enum class ClusteringAlgorithm {
    MCL,
    INFOMAP,
    LOUVAIN,
    CLAUSET_NEWMAN_MOORE;

    companion object {
        fun getClusteringAlgorithmByName(name: String): ClusteringAlgorithm = valueOf(name.toUpperCase())
    }
}
