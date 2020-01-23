package controller.analysis.extraction.coupling.statically

import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.ExtractorCompanion


abstract class AbstractStaticAnalysisExtractor : AbstractExtractor() {
    companion object : ExtractorCompanion {
        const val StaticAnalysisDirectory = "static"
        const val DefaultUnitFootprintByteSize = 1024L

        override fun getWorkingDirectory(): String = "$ExtractionBasePath/$StaticAnalysisDirectory"
    }
}
