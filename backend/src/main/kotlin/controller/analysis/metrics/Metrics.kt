package controller.analysis.metrics

import controller.analysis.metrics.clustering.ClusteringQuality
import controller.analysis.metrics.input.InputQuality


data class Metrics(
        var inputQuality: InputQuality? = null,
        var clusteringQuality: ClusteringQuality? = null
)
