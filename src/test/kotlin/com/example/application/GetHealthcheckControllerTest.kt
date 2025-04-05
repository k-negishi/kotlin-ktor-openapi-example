package com.example.application

import com.example.apiSchema.HealthcheckMessage
import com.example.domain.HealthCheckResult
import com.example.domain.SystemStatus
import io.kotest.core.spec.style.FunSpec
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

class GetHealthcheckControllerTest : FunSpec({
    // すべてのテストをコルーチンコンテキストで実行する設定
    coroutineTestScope = true
    // 並行実行を無効化して1つずつ実行
    concurrency = 1
    // コルーチンのためのディスパッチャーを設定
    beforeSpec {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    // テスト対象
    val useCase = GetHealthcheckController()

    // モック
    val call = mockk<ApplicationCall>(relaxed = true)
    
    context("正常なシステム状態の場合") {
        val healthyResult = HealthCheckResult(
            message = "All systems are operational",
            timestamp = "2025-04-06T12:00:00Z",
            status = SystemStatus.Healthy(responseTimeMs = 150L),
            services = mapOf(
                "database" to true,
                "cache" to true,
                "api" to true
            )
        )

        beforeTest {
            // GetHealthcheckInteractorのモックを用意
            mockkConstructor(GetHealthcheckInteractor::class)
            coEvery { anyConstructed<GetHealthcheckInteractor>().invoke() } returns healthyResult
            
            // respond関数のモックを設定
            coEvery { call.respond(any<HttpStatusCode>(), any<HealthcheckMessage>()) } just Runs
        }

        afterTest {
            unmockkAll()
        }

        test("ヘルスチェック実行時に正常なレスポンスが返される") {
            // suspend関数をコルーチンスコープで実行
            useCase.handle(call)
            
            // 検証
            coVerify { 
                call.respond(
                    HttpStatusCode.OK, 
                    match<HealthcheckMessage> { message ->
                        message.status == HealthcheckMessage.Status.healthy &&
                        message.message == "All systems are operational" &&
                        message.timestamp == "2025-04-06T12:00:00Z" &&
                        message.services?.size == 3 &&
                        message.details?.get("responseTimeMs") == "150"
                    }
                ) 
            }
        }
    }
    
    context("劣化したシステム状態の場合") {
        val degradedResult = HealthCheckResult(
            message = "Some services are degraded",
            timestamp = "2025-04-06T12:30:00Z",
            status = SystemStatus.Degraded(
                workingServicesCount = 2,
                totalServicesCount = 3,
                estimatedRecoveryTimeMinutes = 15
            ),
            services = mapOf(
                "database" to true,
                "cache" to false,
                "api" to true
            )
        )

        beforeTest {
            mockkConstructor(GetHealthcheckInteractor::class)
            coEvery { anyConstructed<GetHealthcheckInteractor>().invoke() } returns degradedResult
            
            coEvery { call.respond(any<HttpStatusCode>(), any<HealthcheckMessage>()) } just Runs
        }

        afterTest {
            unmockkAll()
        }

        test("ヘルスチェック実行時に劣化状態のレスポンスが返される") {
            // suspend関数をコルーチンスコープで実行
            useCase.handle(call)
            
            // 検証
            coVerify { 
                call.respond(
                    HttpStatusCode.OK, 
                    match<HealthcheckMessage> { message ->
                        message.status == HealthcheckMessage.Status.degraded &&
                        message.message == "Some services are degraded" &&
                        message.timestamp == "2025-04-06T12:30:00Z" &&
                        message.services?.get("cache") == false &&
                        message.details?.get("workingServices") == "2/3" &&
                        message.details?.get("estimatedRecoveryMinutes") == "15"
                    }
                ) 
            }
        }
    }
    
    context("異常なシステム状態の場合") {
        val unhealthyResult = HealthCheckResult(
            message = "System is experiencing critical issues",
            timestamp = "2025-04-06T13:00:00Z",
            status = SystemStatus.Unhealthy(
                failedServicesCount = 2,
                totalServicesCount = 3,
                errorCodes = listOf("ERR_DATABASE", "ERR_CACHE"),
                criticalFailure = true
            ),
            services = mapOf(
                "database" to false,
                "cache" to false,
                "api" to true
            )
        )

        beforeTest {
            mockkConstructor(GetHealthcheckInteractor::class)
            coEvery { anyConstructed<GetHealthcheckInteractor>().invoke() } returns unhealthyResult
            
            coEvery { call.respond(any<HttpStatusCode>(), any<HealthcheckMessage>()) } just Runs
        }

        afterTest {
            unmockkAll()
        }

        test("ヘルスチェック実行時に異常状態のレスポンスが返される") {
            // suspend関数をコルーチンスコープで実行
            useCase.handle(call)
            
            // 検証
            coVerify { 
                call.respond(
                    HttpStatusCode.OK, 
                    match<HealthcheckMessage> { message ->
                        message.status == HealthcheckMessage.Status.unhealthy &&
                        message.message == "System is experiencing critical issues" &&
                        message.timestamp == "2025-04-06T13:00:00Z" &&
                        message.services?.get("database") == false &&
                        message.services?.get("cache") == false &&
                        message.details?.get("failedServices") == "2/3" &&
                        message.details?.get("errorCodes") == "ERR_DATABASE,ERR_CACHE" &&
                        message.details?.get("critical") == "true"
                    }
                ) 
            }
        }
    }
})