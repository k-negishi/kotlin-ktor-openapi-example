package com.example

import com.example.apiSchema.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.*

/**
 * アプリケーション全体のエラーハンドリングを構成する拡張関数
 */
fun Application.configureErrorHandler() {
    install(StatusPages) {
        // 404 NotFound - リソースが見つからない場合
        exception<NotFoundException> { call, _ ->
            createErrorResponse(call, HttpStatusCode.NotFound, "The requested resource could not be found")
        }

        status(HttpStatusCode.NotFound) { call, _ ->
            createErrorResponse(call, HttpStatusCode.NotFound, "The requested resource could not be found")
        }

        // 400 BadRequest - リクエストが不正な場合
        exception<BadRequestException> { call, _ ->
            createErrorResponse(call, HttpStatusCode.BadRequest, "Invalid request")
        }

        status(HttpStatusCode.BadRequest) { call, _ ->
            createErrorResponse(call, HttpStatusCode.BadRequest, "Invalid request")
        }

        // 500 InternalServerError - 予期しないエラーが発生した場合
        exception<Throwable> { call, cause ->
            createErrorResponse(
                call,
                HttpStatusCode.InternalServerError,
                "An internal error occurred",
                mapOf("error" to (cause.message ?: "Unknown error"))
            )
        }

        // 必要に応じて他のステータスコードやエラーを追加
    }
}

/**
 * 統一されたエラーレスポンスを生成する共通関数
 */
private suspend fun createErrorResponse(
    call: ApplicationCall,
    status: HttpStatusCode,
    message: String,
    details: Map<String, String> = mapOf("path" to call.request.path())
) {
    call.respond(
        status,
        ErrorResponse(
            code = status.value,
            message = message,
            details = details
        )
    )
}