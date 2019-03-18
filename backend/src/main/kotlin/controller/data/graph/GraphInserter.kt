package controller.data.graph

import controller.data.skeleton.jvm.JvmBytecodeExtractor
import model.skeleton.Unit
import utility.Neo4jConnector
import java.io.File


class GraphInserter(private val projectName: String, private val projectPlatform: String, private val staticAnalysisArchive: File) {
    fun insert(): Boolean {
        val staticAnalysisSkeletonFile = processStaticAnalysisData()
        val unitContainer = UnitContainerExtractor.extract(staticAnalysisSkeletonFile.absolutePath)
        return insertUnitsIntoDatabase(unitContainer.units)
    }

    @Throws(IllegalArgumentException::class)
    private fun processStaticAnalysisData(): File {
        when (projectPlatform) {
            JvmProjectKey -> return JvmBytecodeExtractor(projectName, staticAnalysisArchive).extract()
            else -> throw IllegalArgumentException()
        }
    }

    private fun insertUnitsIntoDatabase(units: List<Unit>): Boolean {
        if (units.isEmpty()) return false

        for (unit in units) {
            if (unit.identifier.contains("Test")) continue
            val startUnit = model.neo4j.node.Unit.create(unit.identifier, unit.packageIdentifier)

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

                    if (identifier.contains("Test")) continue
                    if (!packageIdentifier.startsWith(projectName)) continue

                    val endUnit = model.neo4j.node.Unit.create(identifier, packageIdentifier)

                    startUnit.calls(endUnit)
                }
            }

            Neo4jConnector.saveEntity(startUnit)
        }

        return true
    }

    companion object {
        private const val JvmProjectKey = "jvm"
    }
}
