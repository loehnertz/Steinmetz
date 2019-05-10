package controller.analysis

import controller.analysis.clustering.Clusterer
import controller.analysis.clustering.ClusteringAlgorithm
import controller.analysis.extraction.dynamicanalysis.DynamicAnalysisExtractor
import controller.analysis.extraction.graph.GraphConverter
import controller.analysis.extraction.graph.GraphInserter
import controller.analysis.extraction.staticanalysis.StaticAnalysisExtractor
import controller.analysis.metrics.Metrics
import controller.analysis.metrics.inputquality.InputQuality
import io.ktor.features.BadRequestException
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import model.graph.Edge
import model.graph.Graph
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
                basePackageIdentifier = projectRequest.basePackageIdentifier,
                staticAnalysisArchive = projectRequest.staticAnalysisArchive,
                dynamicAnalysisArchive = projectRequest.dynamicAnalysisArchive
        ).insert()
    }

    fun retrieveProject(projectName: String): ProjectResponse {
        return ProjectResponse(graph = retrieveGraph(projectName), metrics = retrieveMetrics(projectName))
    }

    fun clusterGraph(projectName: String, clusteringAlgorithm: ClusteringAlgorithm, tunableClusteringParameter: Double?): ProjectResponse {
        val projectGraph: Graph = retrieveGraph(projectName)
        val clusteredGraph: Graph = Clusterer(projectGraph, projectName).applyClusteringAlgorithm(clusteringAlgorithm, tunableClusteringParameter)

        return ProjectResponse(graph = clusteredGraph, metrics = retrieveMetrics(projectName))
    }

    private fun retrieveGraph(projectName: String): Graph {
        val filter = Filter(model.neo4j.node.Unit::projectName.name, ComparisonOperator.EQUALS, projectName)
        val unitNodes: List<model.neo4j.node.Unit> = Neo4jConnector.retrieveEntities(model.neo4j.node.Unit::class.java, filter).map { it as model.neo4j.node.Unit }
        val relationships: ArrayList<Edge> = GraphConverter(unitNodes).convertUnitListToRelationships()

        return Graph(edges = relationships.toMutableSet())
    }

    private fun retrieveMetrics(projectName: String): Metrics {
        val filter = Filter(model.neo4j.node.Metrics::projectName.name, ComparisonOperator.EQUALS, projectName)
        val metricsNode: model.neo4j.node.Metrics? = Neo4jConnector.retrieveEntities(model.neo4j.node.Metrics::class.java, filter).map { it as model.neo4j.node.Metrics }.firstOrNull()

        return if (metricsNode != null) {
            model.neo4j.node.Metrics.convertToDataClass(metricsNode)
        } else {
            Metrics(inputQuality = InputQuality(dynamicAnalysis = -1))
        }
    }

    suspend fun handleNewProjectUploads(multipart: MultiPartData): ProjectRequest {
        var projectName: String? = null
        var projectPlatform: String? = null
        var basePackageIdentifier: String? = null
        var staticAnalysisArchive: File? = null
        var dynamicAnalysisArchive: File? = null

        multipart.forEachPart { part ->
            // Only continue if the part is a file (it could be form item)
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        ProjectRequest::projectName.name -> projectName = part.value
                        ProjectRequest::projectPlatform.name -> projectPlatform = part.value
                        ProjectRequest::basePackageIdentifier.name -> basePackageIdentifier = part.value
                    }
                }
                is PartData.FileItem -> {
                    val file: File
                    when (part.name) {
                        ProjectRequest::staticAnalysisArchive.name -> {
                            file = File("${StaticAnalysisExtractor.getArchiveUploadPath()}/$projectName")
                            file.parentFile.mkdirs()
                            file.createNewFile()
                            staticAnalysisArchive = file
                        }
                        ProjectRequest::dynamicAnalysisArchive.name -> {
                            file = File("${DynamicAnalysisExtractor.getArchiveUploadPath()}/$projectName")
                            file.parentFile.mkdirs()
                            file.createNewFile()
                            dynamicAnalysisArchive = file
                        }
                        else -> throw BadRequestException("File keys must be in ${listOf(ProjectRequest::staticAnalysisArchive.name, ProjectRequest::dynamicAnalysisArchive.name)}")
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
                basePackageIdentifier = basePackageIdentifier!!,
                staticAnalysisArchive = staticAnalysisArchive!!,
                dynamicAnalysisArchive = dynamicAnalysisArchive!!
        )
    }
}
