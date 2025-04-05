package com.example.application

import com.example.domain.HealthCheckService
import com.example.domain.SystemStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GetHealthcheckInteractorTest : FunSpec({
    // コルーチンのディスパッチャーを設定
    beforeSpec {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }
    
    afterSpec {
        Dispatchers.resetMain()
    }
    
    // テスト対象のインタラクター
    val healthCheckService = mockk<HealthCheckService>()
    val interactor = GetHealthcheckInteractor(healthCheckService)
    
    test("すべてのサービスが正常な場合、正しいHealthCheckResultを返す") {
        // モックの設定
        coEvery { healthCheckService.checkDatabaseConnection() } returns true
        coEvery { healthCheckService.checkCacheService() } returns true
        coEvery { healthCheckService.checkExternalApiService() } returns true
        every { 
            healthCheckService.determineSystemStatus(any(), any()) 
        } returns SystemStatus.Healthy(150L)
        every { 
            healthCheckService.generateStatusMessage(any()) 
        } returns "All systems are operational"
        
        // テスト実行
        val result = interactor.invoke()
        
        // 検証
        result.message shouldBe "All systems are operational"
        result.status.shouldBeInstanceOf<SystemStatus.Healthy>()
        result.services.size shouldBe 3
        result.services shouldContainAll mapOf(
            "database" to true,
            "cache" to true,
            "api" to true
        )
        
        // タイムスタンプは正しいISO-8601フォーマットか確認
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        LocalDateTime.parse(result.timestamp, formatter)
        
        // モックの検証
        coVerify { 
            healthCheckService.checkDatabaseConnection()
            healthCheckService.checkCacheService()
            healthCheckService.checkExternalApiService()
            healthCheckService.determineSystemStatus(any(), any())
            healthCheckService.generateStatusMessage(any())
        }
    }
    
    test("一部のサービスが異常な場合、正しいHealthCheckResultを返す") {
        // モックの設定
        coEvery { healthCheckService.checkDatabaseConnection() } returns true
        coEvery { healthCheckService.checkCacheService() } returns false
        coEvery { healthCheckService.checkExternalApiService() } returns true
        
        val degradedStatus = SystemStatus.Degraded(
            workingServicesCount = 2,
            totalServicesCount = 3,
            estimatedRecoveryTimeMinutes = 10
        )
        
        every { 
            healthCheckService.determineSystemStatus(any(), any()) 
        } returns degradedStatus
        
        every { 
            healthCheckService.generateStatusMessage(any()) 
        } returns "Some services are degraded"
        
        // テスト実行
        val result = interactor.invoke()
        
        // 検証
        result.message shouldBe "Some services are degraded"
        result.status.shouldBeInstanceOf<SystemStatus.Degraded>()
        result.services.size shouldBe 3
        result.services shouldContainAll mapOf(
            "database" to true,
            "cache" to false,
            "api" to true
        )
        
        // モックの検証
        verify { 
            healthCheckService.determineSystemStatus(
                match<Map<String, Boolean>> { 
                    it["cache"] == false && 
                    it["database"] == true && 
                    it["api"] == true
                }, 
                any()
            )
        }
    }
    
    test("すべてのサービスが異常な場合、正しいHealthCheckResultを返す") {
        // モックの設定
        coEvery { healthCheckService.checkDatabaseConnection() } returns false
        coEvery { healthCheckService.checkCacheService() } returns false
        coEvery { healthCheckService.checkExternalApiService() } returns false
        
        val unhealthyStatus = SystemStatus.Unhealthy(
            failedServicesCount = 3,
            totalServicesCount = 3,
            errorCodes = listOf("ERR_DATABASE", "ERR_CACHE", "ERR_API"),
            criticalFailure = true
        )
        
        every { 
            healthCheckService.determineSystemStatus(any(), any()) 
        } returns unhealthyStatus
        
        every { 
            healthCheckService.generateStatusMessage(any()) 
        } returns "System is down"
        
        // テスト実行
        val result = interactor.invoke()
        
        // 検証
        result.message shouldBe "System is down"
        result.status.shouldBeInstanceOf<SystemStatus.Unhealthy>()
        result.services.size shouldBe 3
        result.services shouldContainAll mapOf(
            "database" to false,
            "cache" to false,
            "api" to false
        )
        
        val details = result.getDetailsMap()
        details["failedServices"] shouldBe "3/3"
        details["critical"] shouldBe "true"
    }
})