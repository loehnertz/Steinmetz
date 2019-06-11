package controller.analysis.extraction.coupling.logical

import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.ExtractorCompanion


abstract class LogicalCouplingExtractor : AbstractExtractor() {
    companion object : ExtractorCompanion {
        private const val LogicalAnalysisDirectory = "logical"

        override fun getWorkingDirectory(): String = "$ExtractionBasePath/$LogicalAnalysisDirectory"
    }
}
