package controller.analysis

import controller.analysis.clustering.Clusterer
import controller.analysis.clustering.ClusteringAlgorithm
import controller.analysis.extraction.Platform
import controller.analysis.extraction.Platform.Companion.getPlatformByName
import controller.analysis.extraction.coupling.dynamically.AbstractDynamicAnalysisExtractor
import controller.analysis.extraction.coupling.evolutionary.AbstractEvolutionaryCouplingExtractor
import controller.analysis.extraction.coupling.evolutionary.VcsSystem
import controller.analysis.extraction.coupling.evolutionary.VcsSystem.Companion.getVcsSystemByName
import controller.analysis.extraction.coupling.semantically.AbstractSemanticCouplingExtractor
import controller.analysis.extraction.coupling.statically.AbstractStaticAnalysisExtractor
import controller.analysis.extraction.graph.GraphConverter
import controller.analysis.extraction.graph.GraphInserter
import controller.analysis.metrics.clustering.ClusteringQualityAnalyzer
import controller.analysis.optimization.moea.Optimizer
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.graph.EdgeAttributeWeights
import model.graph.Graph
import model.metrics.ClusteringQuality
import model.metrics.Metrics
import model.resource.OptimizationResponse
import model.resource.ProjectRequest
import model.resource.ProjectResponse
import org.moeaframework.Executor
import org.moeaframework.core.NondominatedPopulation
import org.moeaframework.core.Solution
import org.moeaframework.core.variable.EncodingUtils
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.model.Result
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utility.HeapSizeRecorder
import utility.Neo4jConnector
import utility.Utilities
import java.io.File
import java.time.Duration
import kotlin.reflect.KProperty1


class AnalysisController {
    private val logger: Logger = LoggerFactory.getLogger(AnalysisController::class.java)

    fun insertProject(projectRequest: ProjectRequest): ProjectResponse {
        val startTime: Long = System.currentTimeMillis()
        val heapSizeRecorder: HeapSizeRecorder = HeapSizeRecorder(Duration.ofSeconds(15)).apply { start() }
        return GraphInserter(
            projectName = projectRequest.projectName,
            projectPlatform = projectRequest.projectPlatform,
            vcsSystem = projectRequest.vcsSystem,
            basePackageIdentifier = projectRequest.basePackageIdentifier,
            staticAnalysisFile = projectRequest.staticAnalysisFile,
            dynamicAnalysisFile = projectRequest.dynamicAnalysisFile,
            semanticAnalysisFile = projectRequest.semanticAnalysisFile,
            evolutionaryAnalysisFile = projectRequest.evolutionaryAnalysisFile
        ).insert().also {
            logger.info("The analysis of project '${projectRequest.projectName}' took ${Utilities.calculateRuntimeDuration(startTime)} seconds and allocated a maximum of ${heapSizeRecorder.stopAndRetrieveMax()} megabytes of heap space.")
        }
    }

    fun retrieveAllProjectNames(): List<String> {
        val result: Result = Neo4jConnector.executeCypher("MATCH (u:${model.neo4j.node.Unit::class.simpleName}) RETURN DISTINCT u.${model.neo4j.node.Unit::projectName.name}")
        return result.map { it.values.first().toString() }
    }

    fun retrieveProject(projectName: String): ProjectResponse {
        return ProjectResponse(graph = retrieveGraph(projectName), metrics = retrieveMetrics(projectName))
    }

    fun deleteProject(projectName: String): String {
        val result: Result = Neo4jConnector.executeCypher("MATCH (n { projectName: '$projectName' }) DETACH DELETE n")
        return result.firstOrNull()?.values?.first().toString()
    }

    fun clusterGraph(projectName: String, clusteringAlgorithm: ClusteringAlgorithm, chosenClusteringMetric: KProperty1<ClusteringQuality, *>, edgeAttributeWeights: EdgeAttributeWeights, maxIterations: Int): ProjectResponse {
        val projectGraph: Graph = retrieveGraph(projectName)
        val clusterer: Clusterer = Clusterer(projectGraph, projectName, chosenClusteringMetric, edgeAttributeWeights).also { it.applyEdgeWeighting() }
        val clusteredGraph: Graph = clusterer.applyClusteringAlgorithm(clusteringAlgorithm, maxIterations)
        val clusteredGraphMetrics: Metrics = calculateClusteredGraphMetrics(clusteredGraph)
        val existingMetrics: Metrics = retrieveMetrics(projectName)

        return ProjectResponse(graph = clusteredGraph, metrics = mergeMetrics(existingMetrics, clusteredGraphMetrics))
    }

    fun optimizeClusteringParameters(projectName: String, clusteringAlgorithm: ClusteringAlgorithm, averagingClusteringMetric: KProperty1<ClusteringQuality, *>, maxIterations: Int): OptimizationResponse {
        val chosenClusteringMetrics: List<KProperty1<ClusteringQuality, Double>> = when (averagingClusteringMetric) {
            ClusteringQuality::averageCouplingModularity -> Optimizer.couplingModularityMetrics
            ClusteringQuality::averageMeanClusterFactor -> Optimizer.clusterFactorMetrics
            else -> throw OptimizationMetricNotSupported()
        }

        val projectGraph: Graph = retrieveGraph(projectName)
        val variableAmount: Int = 3
        val objectiveAmount: Int = variableAmount
        val fitnessFunction: (Solution) -> ClusteringQuality = Optimizer.buildOptimizationFitnessFunction(projectName, projectGraph, averagingClusteringMetric, clusteringAlgorithm, maxIterations)

        val result: NondominatedPopulation = Executor()
            .withAlgorithm("NSGAII")
            .withProblemClass(Optimizer::class.java, variableAmount, objectiveAmount, averagingClusteringMetric, chosenClusteringMetrics, fitnessFunction)
            .withMaxEvaluations(10000)
            .withProperty("bisections", 6) // If left at default, some algorithms just fail due to numeric overflows
            .distributeOnAllCores()
            .run()

        val sortedSolutions: List<Solution> = result.sortedWith(Optimizer.sortingComparator)
        val bestSolution: Solution = sortedSolutions.first()
        val edgeAttributeWeights: EdgeAttributeWeights = EdgeAttributeWeights(
            EncodingUtils.getReal(bestSolution.getVariable(0)),
            0.0,
            EncodingUtils.getReal(bestSolution.getVariable(1)),
            EncodingUtils.getReal(bestSolution.getVariable(2))
        ).also { println("Optimized solution: $it") }

        val resultDirectory = File("/tmp/results/NSGAII/").also { it.mkdirs() }
        val resultFile = File(resultDirectory.absolutePath + "/" + "${projectName}.tsv").also { it.createNewFile() }
        val resultString = "staticWeight\tsemanticWeight\tevolutionaryWeight\tstaticModularity\tsemanticModularity\tevolutionaryModularity\taverageModularity\tcrowdingDistance" + "\n" + result
            .map { s ->
                Triple(
                    (0 until variableAmount)
                        .map { i -> s.getVariable(i) },
                    s.objectives
                        .filter { o -> !o.isNaN() }
                        .map { o -> -o },
                    s.attributes
                )
            }
            .joinToString("\n") { "${it.first[0]}\t${it.first[1]}\t${it.first[2]}\t${it.second[0]}\t${it.second[1]}\t${it.second[2]}\t${it.third[Optimizer.sortingMetric]}\t${it.third["crowdingDistance"]}" }
        resultFile.writeText(resultString)

        return OptimizationResponse(edgeAttributeWeights)
    }

    suspend fun handleNewProjectUploads(multipart: MultiPartData): ProjectRequest {
        var projectName: String? = null
        var projectPlatform: Platform? = null
        var vcsSystem: VcsSystem? = null
        var basePackageIdentifier: String? = null
        var staticAnalysisFile: File? = null
        var dynamicAnalysisFile: File? = null
        var semanticAnalysisFile: File? = null
        var evolutionaryAnalysisFile: File? = null

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        ProjectRequest::projectName.name -> projectName = formatProjectName(part.value)
                        ProjectRequest::projectPlatform.name -> projectPlatform = getPlatformByName(part.value)
                        ProjectRequest::vcsSystem.name -> vcsSystem = getVcsSystemByName(part.value)
                        ProjectRequest::basePackageIdentifier.name -> basePackageIdentifier = part.value
                    }
                }
                is PartData.FileItem -> {
                    val file: File
                    when (part.name) {
                        ProjectRequest::staticAnalysisFile.name -> {
                            file = File("${AbstractStaticAnalysisExtractor.getWorkingDirectory()}/$projectName.${part.originalFileName?.substringAfterLast('.')}")
                            file.parentFile.mkdirs()
                            withContext(Dispatchers.IO) { file.createNewFile() }
                            staticAnalysisFile = file
                        }
                        ProjectRequest::dynamicAnalysisFile.name -> {
                            file = File("${AbstractDynamicAnalysisExtractor.getWorkingDirectory()}/$projectName.${part.originalFileName?.substringAfterLast('.')}")
                            file.parentFile.mkdirs()
                            withContext(Dispatchers.IO) { file.createNewFile() }
                            dynamicAnalysisFile = file
                        }
                        ProjectRequest::semanticAnalysisFile.name -> {
                            file = File("${AbstractSemanticCouplingExtractor.getWorkingDirectory()}/$projectName.${part.originalFileName?.substringAfterLast('.')}")
                            file.parentFile.mkdirs()
                            withContext(Dispatchers.IO) { file.createNewFile() }
                            semanticAnalysisFile = file
                        }
                        ProjectRequest::evolutionaryAnalysisFile.name -> {
                            file = File("${AbstractEvolutionaryCouplingExtractor.getWorkingDirectory()}/$projectName.${part.originalFileName?.substringAfterLast('.')}")
                            file.parentFile.mkdirs()
                            withContext(Dispatchers.IO) { file.createNewFile() }
                            evolutionaryAnalysisFile = file
                        }
                        else -> throw IllegalArgumentException("File keys must be in ${listOf(ProjectRequest::staticAnalysisFile.name, ProjectRequest::dynamicAnalysisFile.name, ProjectRequest::semanticAnalysisFile.name, ProjectRequest::evolutionaryAnalysisFile.name)}")
                    }

                    part.streamProvider().use { uploadStream ->
                        // Copy the stream to the file with buffering
                        file.outputStream().buffered().use { fileStream ->
                            uploadStream.copyTo(fileStream)
                        }
                    }
                }
            }

            // Dispose of the part after use, to prevent leaks
            part.dispose()
        }

        require(staticAnalysisFile != null) { "A static analysis file has to be supplied." }
        require(listOf(dynamicAnalysisFile, semanticAnalysisFile, evolutionaryAnalysisFile).count { it == null } <= 1) { "Only one of the optional analysis files can be left out." }

        return ProjectRequest(
            projectName = projectName!!,
            projectPlatform = projectPlatform!!,
            vcsSystem = vcsSystem!!,
            basePackageIdentifier = basePackageIdentifier!!,
            staticAnalysisFile = staticAnalysisFile!!,
            dynamicAnalysisFile = dynamicAnalysisFile,
            semanticAnalysisFile = semanticAnalysisFile,
            evolutionaryAnalysisFile = evolutionaryAnalysisFile
        )
    }

    private fun retrieveGraph(projectName: String): Graph {
        val filter = Filter(model.neo4j.node.Unit::projectName.name, ComparisonOperator.EQUALS, projectName)
        val unitNodes: List<model.neo4j.node.Unit> = Neo4jConnector.retrieveEntities(model.neo4j.node.Unit::class.java, filter).map { it as model.neo4j.node.Unit }

        if (unitNodes.isEmpty()) throw ProjectDoesNotExistException()

        return GraphConverter(unitNodes).convertUnitListToGraph()
    }

    private fun retrieveMetrics(projectName: String): Metrics {
        val filter = Filter(model.neo4j.node.Metrics::projectName.name, ComparisonOperator.EQUALS, projectName)
        val metricsNode: model.neo4j.node.Metrics? = Neo4jConnector.retrieveEntities(model.neo4j.node.Metrics::class.java, filter).map { it as model.neo4j.node.Metrics }.firstOrNull()

        return if (metricsNode != null) {
            model.neo4j.node.Metrics.convertToDataClass(metricsNode)
        } else {
            Metrics()
        }
    }

    private fun calculateClusteredGraphMetrics(clusteredGraph: Graph): Metrics {
        return Metrics(clusteringQuality = ClusteringQualityAnalyzer(clusteredGraph).calculateClusteringQualityMetrics())
    }

    private fun mergeMetrics(vararg metricsList: Metrics): Metrics {
        val mergedMetrics = Metrics()

        for (metrics: Metrics in metricsList) {
            if (metrics.inputQuality != null) mergedMetrics.inputQuality = metrics.inputQuality
            if (metrics.clusteringQuality != null) mergedMetrics.clusteringQuality = metrics.clusteringQuality
        }

        return mergedMetrics
    }

    private fun formatProjectName(projectName: String): String = projectName.replace(Regex("[^\\x00-\\x7F]"), "").replace(Regex("\\s"), "-").toLowerCase()
}

class ProjectAlreadyExistsException(override val message: String = "A project with that name already exists") : Exception()

class ProjectDoesNotExistException(override val message: String = "A project with that name does not exist") : Exception()

class OptimizationMetricNotSupported(override val message: String = "The chosen optimization metric is not supported") : Exception()
