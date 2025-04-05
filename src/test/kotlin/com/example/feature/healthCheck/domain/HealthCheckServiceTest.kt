package com.example.feature.healthCheck.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

class HealthCheckServiceTest : FunSpec({
    val service = HealthCheckService()
    
    context("determineSystemStatus") {
        test("すべてのサービスが正常な場合、Healthyが返される") {
            val serviceStatuses = mapOf(
                "database" to true,
                "cache" to true,
                "api" to true
            )
            val responseTimeMs = 150L
            
            val result = service.determineSystemStatus(serviceStatuses, responseTimeMs)
            
            result.shouldBeInstanceOf<SystemStatus.Healthy>()
            result.responseTimeMs shouldBe 150L
        }
        
        test("一部のサービスが異常な場合、Degradedが返される") {
            val serviceStatuses = mapOf(
                "database" to true,
                "cache" to false,
                "api" to true
            )
            val responseTimeMs = 200L
            
            val result = service.determineSystemStatus(serviceStatuses, responseTimeMs)
            
            result.shouldBeInstanceOf<SystemStatus.Degraded>()
            result.workingServicesCount shouldBe 2
            result.totalServicesCount shouldBe 3
        }
        
        test("すべてのサービスが異常な場合、システム状態が返される") {
            val serviceStatuses = mapOf(
                "database" to false,
                "cache" to false,
                "api" to false
            )
            val responseTimeMs = 300L
            
            val result = service.determineSystemStatus(serviceStatuses, responseTimeMs)

            result.shouldBeInstanceOf<SystemStatus.Unhealthy>()
            result.failedServicesCount shouldBe 3
            result.totalServicesCount shouldBe 3
            result.criticalFailure shouldBe true
        }
    }
    
    context("generateStatusMessage") {
        test("Healthy状態の場合、適切なメッセージを生成する") {
            val status = SystemStatus.Healthy(responseTimeMs = 100L)
            
            val message = service.generateStatusMessage(status)
            
            message.shouldContain("All systems operational")
            message.shouldContain("100ms")
        }
        
        test("Degraded状態の場合、サービス状態と復旧時間を含むメッセージを生成する") {
            // 復旧時間あり
            val statusWithRecovery = SystemStatus.Degraded(2, 3, 15)
            val messageWithRecovery = service.generateStatusMessage(statusWithRecovery)
            
            messageWithRecovery.shouldContain("2/3 services operational")
            messageWithRecovery.shouldContain("15 minutes")
            
            // 復旧時間なし
            val statusWithoutRecovery = SystemStatus.Degraded(1, 3, null)
            val messageWithoutRecovery = service.generateStatusMessage(statusWithoutRecovery)
            
            messageWithoutRecovery.shouldContain("1/3 services operational")
            messageWithoutRecovery.shouldContain("Recovery time unknown")
        }
        
        test("Unhealthy状態の場合、エラー情報を含むメッセージを生成する") {
            val criticalStatus = SystemStatus.Unhealthy(
                failedServicesCount = 3,
                totalServicesCount = 3,
                errorCodes = listOf("ERR_API", "ERR_CACHE", "ERR_DATABASE"),
                criticalFailure = true
            )
            
            val nonCriticalStatus = SystemStatus.Unhealthy(
                failedServicesCount = 2,
                totalServicesCount = 3,
                errorCodes = listOf("ERR_CACHE", "ERR_DATABASE"),
                criticalFailure = false
            )
            
            val criticalMessage = service.generateStatusMessage(criticalStatus)
            criticalMessage.shouldContain("CRITICAL FAILURE")
            criticalMessage.shouldContain("3/3 services down")
            
            val nonCriticalMessage = service.generateStatusMessage(nonCriticalStatus)
            nonCriticalMessage.shouldContain("2/3 services down")
            // エラーコードは含まれるが、順序は不定
            nonCriticalMessage.shouldContain("Error codes:")
        }
    }
})