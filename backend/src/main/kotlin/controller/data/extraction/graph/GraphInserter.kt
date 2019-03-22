package controller.data.extraction.graph

import controller.data.extraction.staticanalysis.jvm.JvmBytecodeExtractor
import model.skeleton.Unit
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import utility.Neo4jConnector
import java.io.File


class GraphInserter(private val projectName: String, private val projectPlatform: String, private val basePackageIdentifier: String, private val staticAnalysisArchive: File) {
    init {
        if (projectAlreadyExists()) throw ProjectAlreadyExistsException()
    }

    fun insert(): Boolean {
        val staticAnalysisSkeletonXml = processStaticAnalysisData()
        val unitContainer = UnitContainerExtractor.extract(staticAnalysisSkeletonXml)
        return insertUnitsIntoDatabase(unitContainer.units)
    }

    @Throws(IllegalArgumentException::class)
    private fun processStaticAnalysisData(): String {
        when (projectPlatform) {
            JvmProjectKey -> return JvmBytecodeExtractor(projectName, basePackageIdentifier, staticAnalysisArchive).extract()
            else -> throw IllegalArgumentException()
        }
    }

    private fun insertUnitsIntoDatabase(units: List<Unit>): Boolean {
        if (units.isEmpty()) return false

        for (unit in units) {
            if (unit.identifier.contains("Test")) continue
            val startUnit = model.neo4j.node.Unit.create(unit.identifier, unit.packageIdentifier, projectName)

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

                    val endUnit = model.neo4j.node.Unit.create(identifier, packageIdentifier, projectName)

                    startUnit.calls(endUnit)
                }
            }

            Neo4jConnector.saveEntity(startUnit)
        }

        return true
    }

    private fun projectAlreadyExists(): Boolean {
        val filter = Filter(model.neo4j.node.Unit::projectName.name, ComparisonOperator.EQUALS, projectName)
        return Neo4jConnector.retrieveEntities(model.neo4j.node.Unit::class.java, filter).isNotEmpty()
    }

    companion object {
        const val JvmProjectKey = "jvm"
    }
}

class ProjectAlreadyExistsException(override val message: String = "A project with that name already exists") : Exception()
