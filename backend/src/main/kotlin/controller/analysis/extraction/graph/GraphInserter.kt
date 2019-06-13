package controller.analysis.extraction.graph

import controller.analysis.extraction.Platform
import controller.analysis.extraction.coupling.dynamic.platforms.jvm.JfrRecordingAnalyzer
import controller.analysis.extraction.coupling.logical.VcsSystem
import controller.analysis.extraction.coupling.logical.platforms.jvm.JvmLogicalCouplingExtractor
import controller.analysis.extraction.coupling.semantic.platforms.java.JavaSemanticCouplingExtractor
import controller.analysis.extraction.coupling.statically.platforms.jvm.JvmBytecodeExtractor
import controller.analysis.metrics.input.InputQualityAnalyzer
import model.graph.Edge
import model.graph.Graph
import model.graph.Unit
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
        private val semanticAnalysisFile: File,
        private val logicalAnalysisFile: File
) {
    init {
        if (projectAlreadyExists()) throw ProjectAlreadyExistsException()
    }

    private lateinit var staticAnalysisGraph: Graph
    private lateinit var dynamicCouplingGraph: Graph
    private lateinit var semanticCouplingGraph: Graph
    private lateinit var logicalCouplingGraph: Graph

    fun insert(): ProjectResponse {
        staticAnalysisGraph = processStaticAnalysisData()
        dynamicCouplingGraph = processDynamicCouplingData()

        val baseGraph: Graph = mergeStaticAndDynamicCouplingGraphs()

        semanticCouplingGraph = processSemanticCouplingData(baseGraph.edges)
        logicalCouplingGraph = processLogicalCouplingData()

        val finalGraph: Graph = mergeSemanticAndLogicalCouplingGraphs(baseGraph)

        val metrics: Metrics = calculateMetrics(baseGraph = baseGraph)

        insertGraphIntoDatabase(finalGraph)
        insertMetricsIntoDatabase(metrics)

        return ProjectResponse(graph = finalGraph, metrics = metrics)
    }

    @Throws(IllegalArgumentException::class)
    private fun processStaticAnalysisData(): Graph {
        when (projectPlatform) {
            Platform.JAVA -> return JvmBytecodeExtractor(projectName, basePackageIdentifier, staticAnalysisFile).extract()
            else -> throw IllegalArgumentException()
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun processDynamicCouplingData(): Graph {
        when (projectPlatform) {
            Platform.JAVA -> return JfrRecordingAnalyzer(projectName, basePackageIdentifier, dynamicAnalysisFile).extract()
            else -> throw IllegalArgumentException()
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun processSemanticCouplingData(edgesToConsider: Set<Edge>): Graph {
        when (projectPlatform) {
            Platform.JAVA -> return JavaSemanticCouplingExtractor(projectName, basePackageIdentifier, semanticAnalysisFile, edgesToConsider).extract()
            else -> throw IllegalArgumentException()
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun processLogicalCouplingData(): Graph {
        when (projectPlatform) {
            Platform.JAVA -> return JvmLogicalCouplingExtractor(vcsSystem, basePackageIdentifier, logicalAnalysisFile).extract()
            else -> throw IllegalArgumentException()
        }
    }

    private fun calculateMetrics(baseGraph: Graph): Metrics {
        val inputQuality: InputQuality = calculateInputMetrics(baseGraph)

        return Metrics(inputQuality = inputQuality)
    }

    private fun calculateInputMetrics(mergedStaticAndDynamicAnalysisGraph: Graph): InputQuality {
        return InputQualityAnalyzer(staticAnalysisGraph = staticAnalysisGraph, dynamicAnalysisGraph = dynamicCouplingGraph, mergedStaticAndDynamicAnalysisGraph = mergedStaticAndDynamicAnalysisGraph, semanticAnalysisGraph = semanticCouplingGraph, logicalAnalysisGraph = logicalCouplingGraph).calculateInputQualityMetrics()
    }

    private fun mergeStaticAndDynamicCouplingGraphs(): Graph {
        dynamicCouplingGraph.edges.forEach { staticAnalysisGraph.addOrUpdateEdge(it) }
        return mergeInnerUnitNodesWithParentNodes(staticAnalysisGraph)
    }

    private fun mergeInnerUnitNodesWithParentNodes(baseGraph: Graph): Graph {
        val graph = Graph()

        for (edge: Edge in baseGraph.edges) {
            val startUnit: Unit = normalizeUnit(edge.start)
            val endUnit: Unit = normalizeUnit(edge.end)
            val newEdge = Edge(start = startUnit, end = endUnit, attributes = edge.attributes)

            graph.addOrUpdateEdge(newEdge)
        }

        return graph
    }

    @Throws(IllegalArgumentException::class)
    private fun normalizeUnit(unit: Unit): Unit {
        when (projectPlatform) {
            Platform.JAVA -> return JvmBytecodeExtractor.normalizeUnit(unit)
            else -> throw IllegalArgumentException()
        }
    }

    private fun mergeSemanticAndLogicalCouplingGraphs(baseGraph: Graph): Graph {
        semanticCouplingGraph.edges.forEach { baseGraph.updateEdge(it) }
        logicalCouplingGraph.edges.forEach { baseGraph.updateEdge(it) }
        return baseGraph
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

            startUnit.calls(
                    callee = endUnit,
                    dynamicCouplingScore = edge.attributes.dynamicCouplingScore,
                    semanticCouplingScore = edge.attributes.semanticCouplingScore,
                    logicalCouplingScore = edge.attributes.logicalCouplingScore
            )

            Neo4jConnector.saveEntity(startUnit)
        }
    }

    private fun insertMetricsIntoDatabase(metrics: Metrics) {
        val metricsNode: model.neo4j.node.Metrics = model.neo4j.node.Metrics.create(projectName = projectName, metrics = metrics)
        Neo4jConnector.saveEntity(metricsNode)
    }

    private fun projectAlreadyExists(): Boolean {
        val filter = Filter(model.neo4j.node.Unit::projectName.name, ComparisonOperator.EQUALS, projectName)
        return Neo4jConnector.retrieveEntities(model.neo4j.node.Unit::class.java, filter).isNotEmpty()
    }
}

class ProjectAlreadyExistsException(override val message: String = "A project with that name already exists") : Exception()
