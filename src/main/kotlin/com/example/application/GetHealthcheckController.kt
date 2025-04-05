package com.example.application

import com.example.apiSchema.HealthcheckMessage
import com.example.domain.HealthCheckService
import com.example.domain.SystemStatus
import com.example.framework.BaseController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

/**
 * シンプルなヘルスチェックユースケース
 */
class GetHealthcheckController : BaseController() {
    private val healthCheckService = HealthCheckService()
    
    /**
     * リクエストを処理してレスポンスを返す
     */
    suspend fun handle(call: ApplicationCall) {
        handleRequest(call) {
            val interactor = GetHealthcheckInteractor(healthCheckService)
            val result = interactor.invoke()

            val responseMessage = HealthcheckMessage(
                message = result.message,
                timestamp = result.timestamp,
                status = when (result.status) {
                    is SystemStatus.Healthy -> HealthcheckMessage.Status.healthy
                    is SystemStatus.Degraded -> HealthcheckMessage.Status.degraded
                    is SystemStatus.Unhealthy -> HealthcheckMessage.Status.unhealthy
                },
                services = result.services,
                details = result.getDetailsMap()
            )

            call.respond(HttpStatusCode.OK, responseMessage)
        }
    }
}
