package controller.data.extraction.staticanalysis

import controller.data.extraction.Extractor
import controller.data.extraction.Extractor.Companion.ExtractionBasePath
import java.io.File


abstract class StaticAnalysisExtractor {
    fun buildBasePath(platformIdentifier: String, projectName: String) = "$UnarchiverBasePath/$platformIdentifier/$projectName"

    fun cleanup(basePath: String) = File(basePath).deleteRecursively()

    abstract fun extract(): String

    companion object: Extractor {
        const val StaticAnalysisDirectory = "static"
        const val SkeletonDirectoryName = "skeleton"
        const val UnarchiverBasePath = ExtractionBasePath

        override fun getArchiveUploadPath(): String {
            return "$UnarchiverBasePath/$StaticAnalysisDirectory"
        }
    }
}
