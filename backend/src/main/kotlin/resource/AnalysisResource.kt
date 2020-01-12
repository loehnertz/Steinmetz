package resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import controller.analysis.AnalysisController
import controller.analysis.ProjectAlreadyExistsException
import controller.analysis.ProjectDoesNotExistException
import controller.analysis.clustering.ClusteringAlgorithm
import controller.analysis.clustering.ClusteringAlgorithm.Companion.getClusteringAlgorithmByName
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import model.graph.EdgeAttributeWeights
import model.graph.EdgeAttributeWeights.Companion.retrieveEdgeAttributeWeightsFromQueryParameters
import model.metrics.ClusteringQuality
import model.metrics.ClusteringQuality.Companion.getClusteringMetricByName
import model.resource.ProjectRequest
import kotlin.reflect.KProperty1


fun Route.analysis(controller: AnalysisController) {
    route("/analysis") {
        post("/") {
            val request: ProjectRequest = controller.handleNewProjectUploads(call.receiveMultipart())

            try {
                call.respond(controller.insertProject(request))
            } catch (e: ProjectAlreadyExistsException) {
                call.respond(HttpStatusCode.Conflict, e.message)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "An unidentified error occurred: ${e.message}")
            }
        }

        get("/{projectName}") {
            val projectName: String = call.parameters["projectName"].toString()

            try {
                call.respond(controller.retrieveProject(projectName))
            } catch (e: ProjectDoesNotExistException) {
                call.respond(HttpStatusCode.NotFound, e.message)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "An unidentified error occurred: ${e.message}")
            }
        }

        get("/{projectName}/cluster") {
            val projectName: String = call.parameters["projectName"].toString()
            val chosenClusteringAlgorithm: ClusteringAlgorithm = getClusteringAlgorithmByName(call.request.queryParameters["clusteringAlgorithm"]!!)
            val chosenClusteringMetric: KProperty1<ClusteringQuality, *> = getClusteringMetricByName(call.request.queryParameters["clusteringMetric"]!!)
            val edgeAttributeWeights: EdgeAttributeWeights = retrieveEdgeAttributeWeightsFromQueryParameters(call.request.queryParameters)
            val maxIterations: Int = call.request.queryParameters["maxClusteringIterations"]?.toInt() ?: 100

            try {
                call.respond(controller.clusterGraph(projectName, chosenClusteringAlgorithm, chosenClusteringMetric, edgeAttributeWeights, maxIterations))
            } catch (e: ProjectDoesNotExistException) {
                call.respond(HttpStatusCode.NotFound, e.message)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "An unidentified error occurred: ${e.message}")
            }
        }

        get("/{projectName}/optimize") {
            val projectName: String = call.parameters["projectName"].toString()
            val chosenClusteringAlgorithm: ClusteringAlgorithm = getClusteringAlgorithmByName(call.request.queryParameters["clusteringAlgorithm"]!!)
            val chosenClusteringMetric: KProperty1<ClusteringQuality, *> = getClusteringMetricByName(call.request.queryParameters["clusteringMetric"]!!)
            val maxIterations: Int = call.request.queryParameters["maxClusteringIterations"]?.toInt() ?: 100

            call.respond(controller.optimizeClusteringParameters(projectName, chosenClusteringAlgorithm, chosenClusteringMetric, maxIterations))
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val mapper = jacksonObjectMapper().apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }
}
