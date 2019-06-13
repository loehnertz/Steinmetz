package resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import controller.analysis.AnalysisController
import controller.analysis.clustering.ClusteringAlgorithm
import controller.analysis.clustering.ClusteringAlgorithm.Companion.getClusteringAlgorithmByName
import io.ktor.application.call
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import model.graph.EdgeAttributeWeights
import model.graph.EdgeAttributeWeights.Companion.retrieveEdgeAttributeWeightsFromQueryParameters
import model.resource.ProjectRequest


fun Route.analysis(controller: AnalysisController) {
    route("/analysis") {
        post("/") {
            val request: ProjectRequest = controller.handleNewProjectUploads(call.receiveMultipart())

            call.respond(controller.insertProject(request))
        }

        get("/{projectName}") {
            val projectName: String = call.parameters["projectName"].toString()

            call.respond(controller.retrieveProject(projectName))
        }

        get("/{projectName}/cluster") {
            val projectName: String = call.parameters["projectName"].toString()
            val chosenClusteringAlgorithm: ClusteringAlgorithm = getClusteringAlgorithmByName(call.request.queryParameters["clusteringAlgorithm"]!!)
            val edgeAttributeWeights: EdgeAttributeWeights = retrieveEdgeAttributeWeightsFromQueryParameters(call.request.queryParameters)
            val tunableClusteringParameter: Double? = call.request.queryParameters["tunableClusteringParameterValue"]?.toDoubleOrNull()

            call.respond(controller.clusterGraph(projectName, chosenClusteringAlgorithm, edgeAttributeWeights, tunableClusteringParameter))
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val mapper = jacksonObjectMapper().apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }
}
