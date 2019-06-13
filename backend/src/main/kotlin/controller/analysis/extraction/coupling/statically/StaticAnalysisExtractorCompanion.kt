package controller.analysis.extraction.coupling.statically

import model.graph.Unit


interface StaticAnalysisExtractorCompanion {
    fun normalizeUnit(unit: Unit): Unit
}
