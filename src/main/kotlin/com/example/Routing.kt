package com.example

import com.example.api.ApiPaths
import com.example.application.GetHealthcheckController
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.resources.Resources
import io.ktor.server.resources.get
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    install(Resources)

    configureErrorHandler()

    routing {
        healthCheckRoutes()
    }
}

fun Route.healthCheckRoutes() {
    get<ApiPaths.healthcheck> {
        val useCase = GetHealthcheckController()
        useCase.handle(call)
    }
}