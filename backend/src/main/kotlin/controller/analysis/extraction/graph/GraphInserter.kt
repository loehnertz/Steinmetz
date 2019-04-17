package controller.analysis.extraction.graph

import controller.analysis.extraction.dynamicanalysis.jvm.JfrRecordingAnalyzer
import controller.analysis.extraction.staticanalysis.jvm.JvmBytecodeExtractor
import model.graph.Edge
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
        val staticAnalysisEdges = processStaticAnalysisData()
        val dynamicAnalysisEdges = processDynamicAnalysisData()

        val mergedGraph = mergeEdgeListsToGraph(staticAnalysisEdges, dynamicAnalysisEdges)

        insertGraphIntoDatabase(mergedGraph)

        return mergedGraph
    }

    @Throws(IllegalArgumentException::class)
    private fun processStaticAnalysisData(): List<Edge> {
        when (projectPlatform) {
            JvmProjectKey -> return JvmBytecodeExtractor(projectName, basePackageIdentifier, staticAnalysisArchive).extract()
            else -> throw IllegalArgumentException()
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun processDynamicAnalysisData(): List<Edge> {
        when (projectPlatform) {
            JvmProjectKey -> return JfrRecordingAnalyzer(projectName, basePackageIdentifier, dynamicAnalysisArchive).extract()
            else -> throw IllegalArgumentException()
        }
    }

    private fun mergeEdgeListsToGraph(vararg edgeLists: List<Edge>): Graph {
        val edges = arrayListOf<Edge>()

        for (edgeList in edgeLists) {
            for (edge in edgeList) {
                if (edges.contains(edge)) {
                    val equalEdgeIndex = edges.indexOf(edge)
                    edges[equalEdgeIndex].attributes.couplingScore += edge.attributes.couplingScore
                } else {
                    edges.add(edge)
                }
            }
        }

        return Graph(edges.toSet())
    }

    private fun insertGraphIntoDatabase(graph: Graph) {
        for (edge in graph.edges) {
            val startUnit = model.neo4j.node.Unit.create(edge.start.identifier, edge.start.packageIdentifier, projectName)
            val endUnit = model.neo4j.node.Unit.create(edge.end.identifier, edge.end.packageIdentifier, projectName)

            startUnit.calls(endUnit)

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
