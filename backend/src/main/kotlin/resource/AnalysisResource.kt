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
import model.resource.NewProjectRequest


fun Route.analysis(controller: AnalysisController) {
    route("/analysis") {
        post("/") {
            val request: NewProjectRequest = controller.handleNewProjectUploads(call.receiveMultipart())

            call.respond(controller.insertProject(request))
        }

        get("/{projectName}") {
            val projectName: String = call.parameters["projectName"].toString()

            call.respond(controller.getGraph(projectName))
        }

        get("/{projectName}/cluster") {
            val projectName: String = call.parameters["projectName"].toString()
            val chosenClusteringAlgorithm: ClusteringAlgorithm = getClusteringAlgorithmByName(call.request.queryParameters["clusteringAlgorithm"]!!)
            val tunableClusteringParameter: Double? = call.request.queryParameters["tunableClusteringParameter"]?.toDoubleOrNull()

            call.respond(controller.clusterGraph(projectName, chosenClusteringAlgorithm, tunableClusteringParameter))
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val mapper = jacksonObjectMapper().apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }
}
