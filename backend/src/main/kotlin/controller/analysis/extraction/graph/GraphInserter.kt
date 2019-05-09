package controller.analysis.extraction.graph

import controller.analysis.extraction.dynamicanalysis.jvm.JfrRecordingAnalyzer
import controller.analysis.extraction.staticanalysis.jvm.JvmBytecodeExtractor
import model.graph.Graph
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import utility.Neo4jConnector
import java.io.File


class GraphInserter(
        private val projectName: String,
        private val projectPlatform: String,
        private val basePackageIdentifier: String,
        private val staticAnalysisArchive: File,
        private val dynamicAnalysisArchive: File
) {
    init {
        if (projectAlreadyExists()) throw ProjectAlreadyExistsException()
    }

    fun insert(): Graph {
        val staticAnalysisGraph: Graph = processStaticAnalysisData()
        val dynamicAnalysisGraph: Graph = processDynamicAnalysisData()

        val mergedGraph: Graph = mergeGraphs(staticAnalysisGraph, dynamicAnalysisGraph)

        insertGraphIntoDatabase(mergedGraph)

        return mergedGraph
    }

    @Throws(IllegalArgumentException::class)
    private fun processStaticAnalysisData(): Graph {
        when (projectPlatform) {
            JvmProjectKey -> return JvmBytecodeExtractor(projectName, basePackageIdentifier, staticAnalysisArchive).extract()
            else -> throw IllegalArgumentException()
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun processDynamicAnalysisData(): Graph {
        when (projectPlatform) {
            JvmProjectKey -> return JfrRecordingAnalyzer(projectName, basePackageIdentifier, dynamicAnalysisArchive).extract()
            else -> throw IllegalArgumentException()
        }
    }

    private fun mergeGraphs(vararg graphs: Graph): Graph {
        val mergedGraph = Graph()

        for (graph: Graph in graphs) {
            graph.edges.forEach(mergedGraph::addOrUpdateEdge)
            graph.nodes.forEach(mergedGraph::addOrUpdateNode)
        }

        return mergedGraph
    }

    private fun insertGraphIntoDatabase(graph: Graph) {
        for (edge in graph.edges) {
            val startUnit = model.neo4j.node.Unit.create(edge.start.identifier, edge.start.packageIdentifier, projectName)
            val endUnit = model.neo4j.node.Unit.create(edge.end.identifier, edge.end.packageIdentifier, projectName)

            startUnit.calls(endUnit, edge.attributes.couplingScore)

            Neo4jConnector.saveEntity(startUnit)
        }
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
