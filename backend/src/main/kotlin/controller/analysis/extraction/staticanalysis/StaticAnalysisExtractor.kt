package controller.analysis.extraction.staticanalysis

import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.ExtractorCompanion
import model.graph.Edge
import model.graph.EdgeAttributes
import model.graph.Graph
import model.skeleton.UnitContainer


abstract class StaticAnalysisExtractor : AbstractExtractor() {
    fun convertUnitContainerToGraph(unitContainer: UnitContainer, basePackageIdentifier: String): Graph {
        val edges = arrayListOf<Edge>()

        for (unit in unitContainer.units) {
            if (unit.identifier.endsWith("Test")) continue
            val startUnit = model.graph.Unit(identifier = unit.identifier, packageIdentifier = unit.packageIdentifier)

            if (unit.methods == null) continue
            for (method in unit.methods) {
                if (method.operations == null) continue
                for (operation in method.operations) {
                    if (operation.type != "call") continue

                    var targetComponents = operation.target.split('.')
                    targetComponents = targetComponents.dropLast(1)
                    val identifier = targetComponents.last()
                    targetComponents = targetComponents.dropLast(1)
                    val packageIdentifier = targetComponents.joinToString(".")

                    if (identifier.endsWith("Test")) continue
                    if (!packageIdentifier.startsWith(basePackageIdentifier)) continue

                    val endUnit = model.graph.Unit(identifier = identifier, packageIdentifier = packageIdentifier)

                    if (startUnit != endUnit) {
                        edges.add(Edge(start = startUnit, end = endUnit, attributes = EdgeAttributes(couplingScore = 1)))
                    }
                }
            }
        }

        return Graph(edges = edges.toMutableSet())
    }

    companion object : ExtractorCompanion {
        const val StaticAnalysisDirectory = "static"
        const val SkeletonDirectoryName = "skeleton"

        override fun getArchiveUploadPath(): String {
            return "$UnarchiverBasePath/$StaticAnalysisDirectory"
        }
    }
}
