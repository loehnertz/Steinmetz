package controller.analysis.extraction.graph

import controller.analysis.extraction.Platform
import controller.analysis.extraction.coupling.dynamic.platforms.jvm.JfrRecordingAnalyzer
import controller.analysis.extraction.coupling.logical.VcsSystem
import controller.analysis.extraction.coupling.logical.platforms.jvm.JvmLogicalCouplingExtractor
import controller.analysis.extraction.coupling.statically.platforms.jvm.JvmBytecodeExtractor
import controller.analysis.metrics.platforms.jvm.JvmMetricsManager
import model.graph.Edge
import model.graph.Graph
import model.metrics.InputQuality
import model.metrics.Metrics
import model.resource.ProjectResponse
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import utility.Neo4jConnector
import java.io.File


class GraphInserter(
        private val projectName: String,
        private val projectPlatform: Platform,
        private val vcsSystem: VcsSystem,
        private val basePackageIdentifier: String,
        private val staticAnalysisFile: File,
        private val dynamicAnalysisFile: File,
        private val logicalAnalysisFile: File
) {
    init {
        if (projectAlreadyExists()) throw ProjectAlreadyExistsException()
    }

    fun insert(): ProjectResponse {
        val staticAnalysisGraph: Graph = processStaticAnalysisData()
        val dynamicCouplingGraph: Graph = processDynamicAnalysisData()
        val logicalCouplingGraph: Graph = processLogicalCouplingData()

        val mergedGraph: Graph = mergeGraphs(staticAnalysisGraph, dynamicCouplingGraph, logicalCouplingGraph)

        val inputQuality: InputQuality = calculateInputMetrics(staticAnalysisGraph, dynamicCouplingGraph, mergedGraph)
        val metrics = Metrics(inputQuality = inputQuality)

        insertGraphIntoDatabase(mergedGraph)
        insertMetricsIntoDatabase(metrics)

        return ProjectResponse(graph = mergedGraph, metrics = metrics)
    }

    @Throws(IllegalArgumentException::class)
    private fun processStaticAnalysisData(): Graph {
        when (projectPlatform) {
            Platform.JVM -> return JvmBytecodeExtractor(projectName, basePackageIdentifier, staticAnalysisFile).extract()
            else -> throw IllegalArgumentException()
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun processDynamicAnalysisData(): Graph {
        when (projectPlatform) {
            Platform.JVM -> return JfrRecordingAnalyzer(projectName, basePackageIdentifier, dynamicAnalysisFile).extract()
            else -> throw IllegalArgumentException()
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun processLogicalCouplingData(): Graph {
        when (projectPlatform) {
            Platform.JVM -> return JvmLogicalCouplingExtractor(vcsSystem, basePackageIdentifier, logicalAnalysisFile).extract()
            else -> throw IllegalArgumentException()
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun calculateInputMetrics(staticAnalysisGraph: Graph, dynamicAnalysisGraph: Graph, mergedGraph: Graph): InputQuality {
        when (projectPlatform) {
            Platform.JVM -> return JvmMetricsManager.calculateInputMetrics(staticAnalysisGraph, dynamicAnalysisGraph, mergedGraph)
            else -> throw IllegalArgumentException()
        }
    }

    private fun mergeGraphs(staticAnalysisGraph: Graph, dynamicCouplingGraph: Graph, logicalCouplingGraph: Graph): Graph {
        dynamicCouplingGraph.edges.forEach { staticAnalysisGraph.updateEdge(it) }
        logicalCouplingGraph.edges.forEach { staticAnalysisGraph.updateEdge(it) }
        return staticAnalysisGraph
    }

    private fun insertGraphIntoDatabase(graph: Graph) {
        for (edge: Edge in graph.edges) {
            val startUnit = model.neo4j.node.Unit.create(
                    identifier = edge.start.identifier,
                    packageIdentifier = edge.start.packageIdentifier,
                    projectName = projectName,
                    size = graph.findNodeByUnit(edge.start)?.attributes?.footprint?.byteSize ?: -1
            )
            val endUnit = model.neo4j.node.Unit.create(
                    identifier = edge.end.identifier,
                    packageIdentifier = edge.end.packageIdentifier,
                    projectName = projectName,
                    size = graph.findNodeByUnit(edge.end)?.attributes?.footprint?.byteSize ?: -1
            )

            startUnit.calls(endUnit, dynamicCouplingScore = edge.attributes.dynamicCouplingScore, logicalCouplingScore = edge.attributes.logicalCouplingScore)

            Neo4jConnector.saveEntity(startUnit)
        }
    }

    private fun insertMetricsIntoDatabase(metrics: Metrics) {
        val metricsNode = model.neo4j.node.Metrics.create(projectName = projectName, metrics = metrics)
        Neo4jConnector.saveEntity(metricsNode)
    }

    private fun projectAlreadyExists(): Boolean {
        val filter = Filter(model.neo4j.node.Unit::projectName.name, ComparisonOperator.EQUALS, projectName)
        return Neo4jConnector.retrieveEntities(model.neo4j.node.Unit::class.java, filter).isNotEmpty()
    }
}

class ProjectAlreadyExistsException(override val message: String = "A project with that name already exists") : Exception()
