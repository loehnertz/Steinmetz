package controller.analysis.extraction

import model.graph.Graph
import java.io.File


abstract class AbstractExtractor {
    abstract fun extract(): Graph

    fun buildBasePath(platformIdentifier: String, projectName: String) = "$ExtractionBasePath/$platformIdentifier/$projectName"

    fun cleanup(basePath: String) = File(basePath).deleteRecursively()

    companion object {
        const val ExtractionBasePath: String = ExtractorCompanion.ExtractionBasePath
    }
}
