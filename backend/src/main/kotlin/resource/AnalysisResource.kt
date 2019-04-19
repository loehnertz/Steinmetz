package resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import controller.analysis.AnalysisController
import io.ktor.application.call
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route


fun Route.analysis(controller: AnalysisController) {
    route("/analysis") {
        post("/") {
            val request = controller.handleNewProjectUploads(call.receiveMultipart())
            call.respond(controller.insertProject(request))
        }

        get("/{projectName}") {
            val projectName = call.parameters["projectName"].toString()
            call.respond(controller.getGraph(projectName))
        }

        get("/{projectName}/cluster") {
            val projectName = call.parameters["projectName"].toString()
            call.respond(controller.clusterGraph(projectName))
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val mapper = jacksonObjectMapper().apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }
}
