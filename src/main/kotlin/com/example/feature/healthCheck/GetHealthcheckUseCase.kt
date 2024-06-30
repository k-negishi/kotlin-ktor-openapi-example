package com.example.feature.healthCheck

import com.example.apiSchema.HealthcheckMessage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

class GetHealthcheckUseCase {

    suspend fun handle(call: ApplicationCall) {
        val interactor = GetHealthcheckInteractor()
        val result = interactor.invoke()

        call.respond(
            status = HttpStatusCode.OK,
            message = HealthcheckMessage(
                message = result.message
            )
        )
    }
}
