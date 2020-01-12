package controller.analysis.extraction.coupling.evolutionary

import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.ExtractorCompanion


abstract class EvolutionaryCouplingExtractor : AbstractExtractor() {
    companion object : ExtractorCompanion {
        private const val EvolutionaryAnalysisDirectory = "evolutionary"

        override fun getWorkingDirectory(): String = "$ExtractionBasePath/$EvolutionaryAnalysisDirectory"
    }
}
