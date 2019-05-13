package controller.analysis.extraction

import model.graph.Graph
import java.io.File


abstract class AbstractExtractor {
    abstract fun extract(): Graph

    fun buildBasePath(platformIdentifier: String, projectName: String) = "$UnarchiverBasePath/$platformIdentifier/$projectName"

    fun cleanup(basePath: String) = File(basePath).deleteRecursively()

    companion object {
        const val UnarchiverBasePath = ExtractorCompanion.ExtractionBasePath
    }
}
