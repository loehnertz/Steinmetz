package controller.analysis.extraction.coupling.semantic

import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.ExtractorCompanion


abstract class SemanticCouplingExtractor : AbstractExtractor() {
    companion object : ExtractorCompanion {
        private const val SemanticAnalysisDirectory = "semantic"

        override fun getWorkingDirectory(): String = "$ExtractionBasePath/$SemanticAnalysisDirectory"
    }
}
