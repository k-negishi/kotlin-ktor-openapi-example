package com.example.framework

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

abstract class BaseController {
    suspend fun handleRequest(call: ApplicationCall, action: suspend () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Internal Server Error: ${e.message}")
        }
    }
}