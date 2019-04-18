package controller.analysis.extraction.dynamicanalysis

import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.ExtractorCompanion


abstract class DynamicAnalysisExtractor : AbstractExtractor() {
    companion object : ExtractorCompanion {
        const val DynamicAnalysisDirectory = "dynamic"

        override fun getArchiveUploadPath(): String {
            return "$UnarchiverBasePath/$DynamicAnalysisDirectory"
        }
    }
}
