package controller.analysis.extraction.coupling.logical

import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.ExtractorCompanion


abstract class LogicalAnalysisExtractor : AbstractExtractor() {
    companion object : ExtractorCompanion {
        private const val LogicalAnalysisDirectory = "logical"

        override fun getWorkingDirectory(): String {
            return "$ExtractionBasePath/$LogicalAnalysisDirectory"
        }
    }
}
