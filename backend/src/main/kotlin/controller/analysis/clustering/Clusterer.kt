package controller.analysis.clustering

import controller.analysis.clustering.chinesewhispers.ChineseWhispersManager
import controller.analysis.clustering.clausetnewmanmoore.ClausetNewmanMooreManager
import controller.analysis.clustering.infomap.InfomapManager
import controller.analysis.clustering.labelpropagation.LabelPropagationManager
import controller.analysis.clustering.louvain.LouvainManager
import controller.analysis.clustering.mcl.MclManager
import controller.analysis.clustering.walktrap.WalktrapManager
import controller.analysis.clustering.weaklyconnectedcomponents.WeaklyConnectedComponentsManager
import model.graph.EdgeAttributeWeights
import model.graph.Graph
import model.metrics.ClusteringQuality
import kotlin.reflect.KProperty1


class Clusterer(private val graph: Graph, private val projectName: String, private val chosenClusteringMetric: KProperty1<ClusteringQuality, *>, private val edgeAttributeWeights: EdgeAttributeWeights) {
    fun applyEdgeWeighting() {
        graph.edges.forEach { it.attributes.couplingScore = EdgeWeightingFormulaCalculator.applyFormula(it.attributes, edgeAttributeWeights) }
    }

    fun applyClusteringAlgorithm(clusteringAlgorithm: ClusteringAlgorithm, maxIterations: Int): Graph {
        return when (clusteringAlgorithm) {
            ClusteringAlgorithm.MCL                         -> MclManager(graph, projectName, chosenClusteringMetric).apply(iterations = maxIterations)
            ClusteringAlgorithm.INFOMAP                     -> InfomapManager(graph, projectName).apply(iterations = maxIterations)
            ClusteringAlgorithm.LOUVAIN                     -> LouvainManager(graph, projectName).apply(iterations = maxIterations)
            ClusteringAlgorithm.CLAUSET_NEWMAN_MOORE        -> ClausetNewmanMooreManager(graph, projectName).apply(iterations = maxIterations)
            ClusteringAlgorithm.WALKTRAP                    -> WalktrapManager(graph, chosenClusteringMetric, projectName).apply(iterations = maxIterations)
            ClusteringAlgorithm.CHINESE_WHISPERS            -> ChineseWhispersManager(graph).apply(iterations = maxIterations)
            ClusteringAlgorithm.LABEL_PROPAGATION           -> LabelPropagationManager(graph, projectName).apply(iterations = maxIterations)
            ClusteringAlgorithm.WEAKLY_CONNECTED_COMPONENTS -> WeaklyConnectedComponentsManager(graph, projectName).apply(iterations = maxIterations)
        }
    }
}
