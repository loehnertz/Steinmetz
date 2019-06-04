package controller.analysis

import controller.analysis.clustering.Clusterer
import controller.analysis.clustering.ClusteringAlgorithm
import controller.analysis.extraction.Platform
import controller.analysis.extraction.Platform.Companion.getPlatformByName
import controller.analysis.extraction.coupling.dynamic.DynamicAnalysisExtractor
import controller.analysis.extraction.coupling.logical.LogicalCouplingExtractor
import controller.analysis.extraction.coupling.logical.VcsSystem
import controller.analysis.extraction.coupling.logical.VcsSystem.Companion.getVcsSystemByName
import controller.analysis.extraction.coupling.semantic.SemanticCouplingExtractor
import controller.analysis.extraction.coupling.statically.StaticAnalysisExtractor
import controller.analysis.extraction.graph.GraphConverter
import controller.analysis.extraction.graph.GraphInserter
import controller.analysis.metrics.platforms.jvm.JvmMetricsManager
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import model.graph.Graph
import model.metrics.Metrics
import model.resource.ProjectRequest
import model.resource.ProjectResponse
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import utility.Neo4jConnector
import java.io.File


class AnalysisController {
    fun insertProject(projectRequest: ProjectRequest): ProjectResponse {
        return GraphInserter(
                projectName = projectRequest.projectName,
                projectPlatform = projectRequest.projectPlatform,
                vcsSystem = projectRequest.vcsSystem,
                basePackageIdentifier = projectRequest.basePackageIdentifier,
                staticAnalysisFile = projectRequest.staticAnalysisFile,
                dynamicAnalysisFile = projectRequest.dynamicAnalysisFile,
                semanticAnalysisFile = projectRequest.semanticAnalysisFile,
                logicalAnalysisFile = projectRequest.logicalAnalysisFile
        ).insert()
    }

    fun retrieveProject(projectName: String): ProjectResponse {
        return ProjectResponse(graph = retrieveGraph(projectName), metrics = retrieveMetrics(projectName))
    }

    fun clusterGraph(projectName: String, clusteringAlgorithm: ClusteringAlgorithm, tunableClusteringParameter: Double?): ProjectResponse {
        val projectGraph: Graph = retrieveGraph(projectName)
        val clusterer: Clusterer = Clusterer(projectGraph, projectName).also { it.applyEdgeWeighting() }
        val clusteredGraph: Graph = clusterer.applyClusteringAlgorithm(clusteringAlgorithm, tunableClusteringParameter)
        val clusteredGraphMetrics: Metrics = calculateClusteredGraphMetrics(clusteredGraph)
        val existingMetrics: Metrics = retrieveMetrics(projectName)

        return ProjectResponse(graph = clusteredGraph, metrics = mergeMetrics(existingMetrics, clusteredGraphMetrics))
    }

    private fun retrieveGraph(projectName: String): Graph {
        val filter = Filter(model.neo4j.node.Unit::projectName.name, ComparisonOperator.EQUALS, projectName)
        val unitNodes: List<model.neo4j.node.Unit> = Neo4jConnector.retrieveEntities(model.neo4j.node.Unit::class.java, filter).map { it as model.neo4j.node.Unit }

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
        return Metrics(clusteringQuality = JvmMetricsManager.calculateClusteringMetrics(clusteredGraph))
    }

    private fun mergeMetrics(vararg metricsList: Metrics): Metrics {
        val mergedMetrics = Metrics()

        for (metrics: Metrics in metricsList) {
            if (metrics.inputQuality != null) mergedMetrics.inputQuality = metrics.inputQuality
            if (metrics.clusteringQuality != null) mergedMetrics.clusteringQuality = metrics.clusteringQuality
        }

        return mergedMetrics
    }

    suspend fun handleNewProjectUploads(multipart: MultiPartData): ProjectRequest {
        var projectName: String? = null
        var projectPlatform: Platform? = null
        var vcsSystem: VcsSystem? = null
        var basePackageIdentifier: String? = null
        var staticAnalysisFile: File? = null
        var dynamicAnalysisFile: File? = null
        var semanticAnalysisFile: File? = null
        var logicalAnalysisFile: File? = null

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        ProjectRequest::projectName.name -> projectName = part.value
                        ProjectRequest::projectPlatform.name -> projectPlatform = getPlatformByName(part.value)
                        ProjectRequest::vcsSystem.name -> vcsSystem = getVcsSystemByName(part.value)
                        ProjectRequest::basePackageIdentifier.name -> basePackageIdentifier = part.value
                    }
                }
                is PartData.FileItem -> {
                    val file: File
                    when (part.name) {
                        ProjectRequest::staticAnalysisFile.name -> {
                            file = File("${StaticAnalysisExtractor.getWorkingDirectory()}/$projectName")
                            file.parentFile.mkdirs()
                            file.createNewFile()
                            staticAnalysisFile = file
                        }
                        ProjectRequest::dynamicAnalysisFile.name -> {
                            file = File("${DynamicAnalysisExtractor.getWorkingDirectory()}/$projectName")
                            file.parentFile.mkdirs()
                            file.createNewFile()
                            dynamicAnalysisFile = file
                        }
                        ProjectRequest::semanticAnalysisFile.name -> {
                            file = File("${SemanticCouplingExtractor.getWorkingDirectory()}/$projectName")
                            file.parentFile.mkdirs()
                            file.createNewFile()
                            semanticAnalysisFile = file
                        }
                        ProjectRequest::logicalAnalysisFile.name -> {
                            file = File("${LogicalCouplingExtractor.getWorkingDirectory()}/$projectName")
                            file.parentFile.mkdirs()
                            file.createNewFile()
                            logicalAnalysisFile = file
                        }
                        else -> throw IllegalArgumentException("File keys must be in ${listOf(ProjectRequest::staticAnalysisFile.name, ProjectRequest::dynamicAnalysisFile.name, ProjectRequest::semanticAnalysisFile.name, ProjectRequest::logicalAnalysisFile.name)}")
                    }

                    part.streamProvider().use { upload ->
                        // Copy the stream to the file with buffering
                        file.outputStream().buffered().use {
                            upload.copyTo(it)
                        }
                    }
                }
            }

            // Dispose of the part after use to prevent leaks
            part.dispose()
        }

        return ProjectRequest(
                projectName = projectName!!,
                projectPlatform = projectPlatform!!,
                vcsSystem = vcsSystem!!,
                basePackageIdentifier = basePackageIdentifier!!,
                staticAnalysisFile = staticAnalysisFile!!,
                dynamicAnalysisFile = dynamicAnalysisFile!!,
                semanticAnalysisFile = semanticAnalysisFile!!,
                logicalAnalysisFile = logicalAnalysisFile!!
        )
    }
}
