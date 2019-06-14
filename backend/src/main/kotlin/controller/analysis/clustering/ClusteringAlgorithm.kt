package controller.analysis.clustering


enum class ClusteringAlgorithm {
    MCL,
    INFOMAP,
    LOUVAIN,
    CLAUSET_NEWMAN_MOORE,
    WALKTRAP,
    CHINESE_WHISPERS;

    companion object {
        fun getClusteringAlgorithmByName(name: String): ClusteringAlgorithm = valueOf(name.toUpperCase())
    }
}
