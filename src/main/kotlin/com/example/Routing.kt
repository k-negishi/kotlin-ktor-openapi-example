package com.example

import com.example.feature.healthCheck.GetHealthcheckUseCase
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        healthCheckRoutes()
    }
}

fun Route.healthCheckRoutes() {
    get("/healthcheck") {
        val useCase = GetHealthcheckUseCase()
        useCase.handle(call)
    }
}
