package controller.analysis.extraction.coupling.statically

import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.ExtractorCompanion


abstract class AbstractStaticAnalysisExtractor : AbstractExtractor() {

    internal val responseForAClassPairMap: MutableMap<ResponseForAClassIdentifiers, ResponseForAClassPair> = mutableMapOf()

    companion object : ExtractorCompanion {
        const val StaticAnalysisDirectory = "static"

        override fun getWorkingDirectory(): String = "$ExtractionBasePath/$StaticAnalysisDirectory"
    }
}
