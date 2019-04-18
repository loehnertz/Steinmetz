package controller.analysis.extraction

import model.graph.Edge
import java.io.File


abstract class AbstractExtractor {
    abstract fun extract(): List<Edge>

    fun buildBasePath(platformIdentifier: String, projectName: String) = "$UnarchiverBasePath/$platformIdentifier/$projectName"

    fun cleanup(basePath: String) = File(basePath).deleteRecursively()

    companion object {
        const val UnarchiverBasePath = ExtractorCompanion.ExtractionBasePath
    }
}
