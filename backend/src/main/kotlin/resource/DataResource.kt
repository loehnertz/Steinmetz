package resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import controller.data.DataController
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route


fun Route.data(controller: DataController) {
    route("/data") {
        get("/{basePackageIdentifier}") {
            val basePackageIdentifier = call.parameters["basePackageIdentifier"].toString()
            call.respond(controller.getGraph(basePackageIdentifier))
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val mapper = jacksonObjectMapper().apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }
}
