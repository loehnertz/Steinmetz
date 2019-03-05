package main

import com.fasterxml.jackson.databind.SerializationFeature
import controller.user.UserController
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.jackson.jackson
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import resource.user


fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
        }
    }

    install(Routing) {
        user(UserController())
    }
}

fun main() {
    embeddedServer(Netty, 5656, watchPaths = listOf("MainKt"), module = Application::module).start()
}
