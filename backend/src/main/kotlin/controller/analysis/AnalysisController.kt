package controller.analysis

import controller.analysis.extraction.graph.GraphConverter
import controller.analysis.extraction.graph.GraphInserter
import controller.analysis.extraction.staticanalysis.StaticAnalysisExtractor
import io.ktor.features.BadRequestException
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import model.graph.Graph
import model.neo4j.node.Unit
import model.resource.NewProjectRequest
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import utility.Neo4jConnector
import java.io.File


class AnalysisController {
    fun insertProject(newProjectRequest: NewProjectRequest): Graph {
        GraphInserter(
                projectName = newProjectRequest.projectName,
                projectPlatform = newProjectRequest.projectPlatform,
                basePackageIdentifier = newProjectRequest.basePackageIdentifier,
                staticAnalysisArchive = newProjectRequest.staticAnalysisArchive
        ).insert()
        return getGraph(newProjectRequest.projectName)
    }

    fun getGraph(projectName: String): Graph {
        val filter = Filter(Unit::projectName.name, ComparisonOperator.EQUALS, projectName)
        val units = Neo4jConnector.retrieveEntities(Unit::class.java, filter).map { it as Unit }
        val relationships = GraphConverter.convertUnitListToRelationships(units)
        return Graph(relationships = relationships)
    }

    suspend fun handleNewProjectUploads(multipart: MultiPartData): NewProjectRequest {
        var projectName: String? = null
        var projectPlatform: String? = null
        var basePackageIdentifier: String? = null
        var staticAnalysisArchive: File? = null
        var dynamicAnalysisArchive: File? = null

        multipart.forEachPart { part ->
            // if part is a file (could be form item)
            when (part) {
                is PartData.FileItem -> {
                    val file: File
                    when (part.name) {
                        StaticAnalysisKey -> {
                            val path = StaticAnalysisExtractor.getArchiveUploadPath()
                            file = File("$path/${part.originalFileName!!}")
                            file.parentFile.mkdirs()
                            file.createNewFile()
                            staticAnalysisArchive = file
                        }
                        else -> throw BadRequestException("File keys must be in ${listOf(StaticAnalysisKey, DynamicAnalysisKey)}")
                    }

                    // use InputStream from part to save file
                    part.streamProvider().use { upload ->
                        // copy the stream to the file with buffering
                        file.outputStream().buffered().use {
                            // note that this is blocking
                            upload.copyTo(it)
                        }
                    }
                }
                is PartData.FormItem -> {
                    when (part.name) {
                        NewProjectRequest::projectName.name -> projectName = part.value
                        NewProjectRequest::projectPlatform.name -> projectPlatform = part.value
                        NewProjectRequest::basePackageIdentifier.name -> basePackageIdentifier = part.value
                    }
                }
            }
            // make sure to dispose of the part after use to prevent leaks
            part.dispose()
        }

        return NewProjectRequest(
                projectName = projectName!!,
                projectPlatform = projectPlatform!!,
                basePackageIdentifier = basePackageIdentifier!!,
                staticAnalysisArchive = staticAnalysisArchive!!,
                dynamicAnalysisArchive = dynamicAnalysisArchive
        )
    }

    companion object {
        const val StaticAnalysisKey = "static"
        const val DynamicAnalysisKey = "dynamic"
    }
}
