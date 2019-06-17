package controller.analysis.extraction.coupling.statically

import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.ExtractorCompanion


abstract class StaticAnalysisExtractor : AbstractExtractor() {
    companion object : ExtractorCompanion {
        const val StaticAnalysisDirectory = "static"

        override fun getWorkingDirectory(): String = "$ExtractionBasePath/$StaticAnalysisDirectory"
    }
}
