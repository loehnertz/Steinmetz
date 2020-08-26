package controller.analysis.clustering

import model.graph.Graph
import java.io.File


abstract class ClusteringAlgorithmManager {
    abstract fun apply(iterations: Int): Graph

    internal fun cleanup(vararg basePaths: String) = basePaths.forEach { File(it).deleteRecursively() }
}
