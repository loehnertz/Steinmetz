package controller.analysis.extraction.coupling.statically

import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.ExtractorCompanion
import model.graph.Edge
import model.graph.EdgeAttributes
import model.graph.Graph
import model.skeleton.Method
import model.skeleton.Operation
import model.skeleton.Unit
import model.skeleton.UnitContainer


abstract class StaticAnalysisExtractor : AbstractExtractor() {
    fun convertUnitContainerToGraph(unitContainer: UnitContainer, basePackageIdentifier: String): Graph {
        val graph = Graph()

        for (unit: Unit in unitContainer.units) {
            if (unit.identifier.endsWith("Test")) continue
            val startUnit = model.graph.Unit(identifier = unit.identifier, packageIdentifier = unit.packageIdentifier)

            if (unit.methods == null) continue
            for (method: Method in unit.methods) {
                if (method.operations == null) continue
                for (operation: Operation in method.operations) {
                    if (operation.type != "call") continue

                    var targetComponents: List<String> = operation.target.split('.')
                    val invokedMethod: String = targetComponents.last()
                    targetComponents = targetComponents.dropLast(1)
                    val identifier: String = targetComponents.last()
                    targetComponents = targetComponents.dropLast(1)
                    val packageIdentifier: String = targetComponents.joinToString(".")

                    val endUnit = model.graph.Unit(identifier = identifier, packageIdentifier = packageIdentifier)

                    if (identifier.endsWith("Test")) continue
                    if (!packageIdentifier.startsWith(basePackageIdentifier)) continue
                    if (startUnit == endUnit) continue

                    val edge = Edge(start = startUnit, end = endUnit, attributes = EdgeAttributes(dynamicCouplingScore = 0, logicalCouplingScore = 0))
                    graph.addOrUpdateEdge(edge)
                }
            }
        }

        return graph
    }

    companion object : ExtractorCompanion {
        const val StaticAnalysisDirectory = "static"
        const val SkeletonDirectoryName = "skeleton"

        override fun getWorkingDirectory(): String {
            return "$ExtractionBasePath/$StaticAnalysisDirectory"
        }
    }
}
