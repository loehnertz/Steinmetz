package controller.analysis.extraction.coupling.dynamic

import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.ExtractorCompanion


abstract class DynamicAnalysisExtractor : AbstractExtractor() {
    companion object : ExtractorCompanion {
        const val DynamicAnalysisDirectory = "dynamic"

        override fun getWorkingDirectory(): String {
            return "$ExtractionBasePath/$DynamicAnalysisDirectory"
        }
    }
}
