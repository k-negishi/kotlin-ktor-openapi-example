package com.example.apiSchema

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class HealthcheckMessageTest : StringSpec({
    val json = Json { prettyPrint = true }
    
    "HealthcheckMessageを正しく作成できる" {
        val message = HealthcheckMessage(
            message = "All systems are operational",
            timestamp = "2025-04-06T12:00:00Z",
            status = HealthcheckMessage.Status.healthy,
            services = mapOf(
                "database" to true,
                "cache" to true,
                "api" to true
            ),
            details = mapOf(
                "responseTimeMs" to "150"
            )
        )
        
        message.message shouldBe "All systems are operational"
        message.timestamp shouldBe "2025-04-06T12:00:00Z"
        message.status shouldBe HealthcheckMessage.Status.healthy
        message.services?.size shouldBe 3
        message.services?.get("database") shouldBe true
        message.details?.get("responseTimeMs") shouldBe "150"
    }
    
    "HealthcheckMessage.Statusの値が正しく設定されている" {
        HealthcheckMessage.Status.healthy.value shouldBe "healthy"
        HealthcheckMessage.Status.degraded.value shouldBe "degraded"
        HealthcheckMessage.Status.unhealthy.value shouldBe "unhealthy"
    }
    
    "JSONシリアライズが正しく機能する" {
        val message = HealthcheckMessage(
            message = "Some services are degraded",
            timestamp = "2025-04-06T12:30:00Z",
            status = HealthcheckMessage.Status.degraded,
            services = mapOf(
                "database" to true,
                "cache" to false,
                "api" to true
            ),
            details = mapOf(
                "workingServices" to "2/3",
                "estimatedRecoveryMinutes" to "15"
            )
        )
        
        val serialized = json.encodeToString(message)
        
        serialized.contains("\"message\"") shouldBe true
        serialized.contains("\"timestamp\"") shouldBe true
        serialized.contains("\"status\"") shouldBe true
        serialized.contains("\"services\"") shouldBe true
        serialized.contains("\"details\"") shouldBe true
        
        serialized.contains("Some services are degraded") shouldBe true
        serialized.contains("degraded") shouldBe true
        serialized.contains("false") shouldBe true
        serialized.contains("2/3") shouldBe true
    }
    
    "JSONデシリアライズが正しく機能する" {
        val jsonString = """
        {
            "message": "System is experiencing issues",
            "timestamp": "2025-04-06T13:00:00Z",
            "status": "unhealthy",
            "services": {
                "database": false,
                "cache": false,
                "api": true
            },
            "details": {
                "failedServices": "2/3",
                "errorCodes": "ERR_DATABASE,ERR_CACHE",
                "critical": "true"
            }
        }
        """.trimIndent()
        
        val deserialized = json.decodeFromString<HealthcheckMessage>(jsonString)
        
        deserialized.message shouldBe "System is experiencing issues"
        deserialized.timestamp shouldBe "2025-04-06T13:00:00Z"
        deserialized.status shouldBe HealthcheckMessage.Status.unhealthy
        deserialized.services?.get("database") shouldBe false
        deserialized.services?.get("cache") shouldBe false
        deserialized.services?.get("api") shouldBe true
        deserialized.details?.get("failedServices") shouldBe "2/3"
        deserialized.details?.get("errorCodes") shouldBe "ERR_DATABASE,ERR_CACHE"
        deserialized.details?.get("critical") shouldBe "true"
    }
})