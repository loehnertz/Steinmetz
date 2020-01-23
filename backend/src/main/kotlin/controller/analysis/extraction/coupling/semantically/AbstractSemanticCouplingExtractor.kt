package controller.analysis.extraction.coupling.semantically

import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.ExtractorCompanion


abstract class AbstractSemanticCouplingExtractor : AbstractExtractor() {
    companion object : ExtractorCompanion {
        private const val SemanticAnalysisDirectory = "semantic"

        override fun getWorkingDirectory(): String = "$ExtractionBasePath/$SemanticAnalysisDirectory"
    }
}
