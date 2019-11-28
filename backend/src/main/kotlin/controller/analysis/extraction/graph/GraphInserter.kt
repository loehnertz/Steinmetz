package controller.analysis.extraction.graph

import controller.analysis.extraction.Platform
import controller.analysis.extraction.coupling.dynamically.platforms.jvm.JvmDynamicAnalysisExtractor
import controller.analysis.extraction.coupling.evolutionary.VcsSystem
import controller.analysis.extraction.coupling.evolutionary.platforms.jvm.JvmEvolutionaryCouplingExtractor
import controller.analysis.extraction.coupling.semantically.platforms.java.JavaSemanticCouplingExtractor
import controller.analysis.extraction.coupling.statically.platforms.java.JavaStaticCouplingExtractor
import controller.analysis.metrics.input.InputQualityAnalyzer
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
    private val dynamicAnalysisFile: File?,
    private val semanticAnalysisFile: File?,
    private val evolutionaryAnalysisFile: File?
) {
    init {
        if (projectAlreadyExists()) throw ProjectAlreadyExistsException()
    }

    private lateinit var staticAnalysisGraph: Graph
    private lateinit var dynamicCouplingGraph: Graph
    private lateinit var semanticCouplingGraph: Graph
    private lateinit var evolutionaryCouplingGraph: Graph

    fun insert(): ProjectResponse {
        println("Started graph construction process of project '$projectName'")

        staticAnalysisGraph = processStaticAnalysisData()

        dynamicCouplingGraph = if (dynamicAnalysisFile != null) {
            processDynamicCouplingData()
        } else {
            Graph()
        }

        val baseGraph: Graph = mergeStaticAndDynamicCouplingGraphs()

        semanticCouplingGraph = if (semanticAnalysisFile != null) {
            processSemanticCouplingData(baseGraph.edges)
        } else {
            Graph()
        }
        evolutionaryCouplingGraph = if (evolutionaryAnalysisFile != null) {
            processEvolutionaryCouplingData()
        } else {
            Graph()
        }

        val metrics: Metrics = calculateMetrics(baseGraph = baseGraph)
        val finalGraph: Graph = mergeSemanticAndEvolutionaryCouplingGraphs(baseGraph)

        insertMetricsIntoDatabase(metrics)
        insertGraphIntoDatabase(finalGraph)

        println("Finished graph construction process of project '$projectName'")

        return ProjectResponse(graph = finalGraph, metrics = metrics)
    }

    @Throws(IllegalArgumentException::class)
    private fun processStaticAnalysisData(): Graph {
        println("Processing static analysis data")
        when (projectPlatform) {
            Platform.JAVA -> return JavaStaticCouplingExtractor(projectName, basePackageIdentifier, staticAnalysisFile).extract()
            else -> throw IllegalArgumentException()
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun processDynamicCouplingData(): Graph {
        println("Processing dynamic coupling data")
        when (projectPlatform) {
            Platform.JAVA -> return JvmDynamicAnalysisExtractor(projectName, basePackageIdentifier, dynamicAnalysisFile!!).extract()
            else -> throw IllegalArgumentException()
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun processSemanticCouplingData(edgesToConsider: Set<Edge>): Graph {
        println("Processing semantic coupling data")
        when (projectPlatform) {
            Platform.JAVA -> return JavaSemanticCouplingExtractor(projectName, basePackageIdentifier, semanticAnalysisFile!!, edgesToConsider).extract()
            else -> throw IllegalArgumentException()
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun processEvolutionaryCouplingData(): Graph {
        println("Processing evolutionary coupling data")
        when (projectPlatform) {
            Platform.JAVA -> return JvmEvolutionaryCouplingExtractor(vcsSystem, basePackageIdentifier, evolutionaryAnalysisFile!!).extract()
            else -> throw IllegalArgumentException()
        }
    }

    private fun calculateMetrics(baseGraph: Graph): Metrics {
        println("Calculating metrics")
        val inputQuality: InputQuality = calculateInputMetrics(baseGraph)
        return Metrics(inputQuality = inputQuality)
    }

    private fun calculateInputMetrics(mergedStaticAndDynamicAnalysisGraph: Graph): InputQuality {
        return InputQualityAnalyzer(mergedStaticAndDynamicAnalysisGraph = mergedStaticAndDynamicAnalysisGraph, dynamicAnalysisGraph = dynamicCouplingGraph, semanticAnalysisGraph = semanticCouplingGraph, evolutionaryAnalysisGraph = evolutionaryCouplingGraph).calculateInputQualityMetrics()
    }

    private fun mergeStaticAndDynamicCouplingGraphs(): Graph {
        dynamicCouplingGraph.edges.forEach { staticAnalysisGraph.addOrUpdateEdge(it) }
        return staticAnalysisGraph
    }

    private fun mergeSemanticAndEvolutionaryCouplingGraphs(baseGraph: Graph): Graph {
        semanticCouplingGraph.edges.forEach { baseGraph.updateEdge(it) }
        evolutionaryCouplingGraph.edges.forEach { baseGraph.updateEdge(it) }
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
                evolutionaryCouplingScore = edge.attributes.evolutionaryCouplingScore
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
