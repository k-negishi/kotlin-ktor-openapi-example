package com.example.domain

import com.example.domain.HealthCheckResult
import com.example.domain.SystemStatus
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe

class HealthCheckResultTest : FeatureSpec({
    feature("HealthCheckResult.getDetailsMap") {
        scenario("Healthy状態の場合、正しい詳細情報を返す") {
            // Arrange
            val healthyStatus = SystemStatus.Healthy(responseTimeMs = 150L)
            val result = HealthCheckResult(
                message = "All systems operational",
                timestamp = "2023-10-20T12:00:00Z",
                status = healthyStatus,
                services = mapOf("database" to true, "cache" to true, "api" to true)
            )

            // Act
            val details = result.getDetailsMap()

            // Assert
            details shouldHaveSize 1
            details["responseTimeMs"] shouldBe "150"
        }

        scenario("Degraded状態で復旧時間が指定されている場合、正しい詳細情報を返す") {
            // Arrange
            val degradedStatusWithRecovery = SystemStatus.Degraded(
                workingServicesCount = 2,
                totalServicesCount = 3,
                estimatedRecoveryTimeMinutes = 10
            )
            val resultWithRecovery = HealthCheckResult(
                message = "Partial system failure",
                timestamp = "2023-10-20T12:05:00Z",
                status = degradedStatusWithRecovery,
                services = mapOf("database" to true, "cache" to true, "api" to false)
            )

            // Act
            val details = resultWithRecovery.getDetailsMap()

            // Assert
            details shouldHaveSize 2
            details["workingServices"] shouldBe "2/3"
            details["estimatedRecoveryMinutes"] shouldBe "10"
        }

        scenario("Degraded状態で復旧時間が指定されていない場合、復旧時間なしの詳細情報を返す") {
            // Arrange
            val degradedStatusWithoutRecovery = SystemStatus.Degraded(
                workingServicesCount = 2,
                totalServicesCount = 3,
                estimatedRecoveryTimeMinutes = null
            )
            val resultWithoutRecovery = HealthCheckResult(
                message = "Partial system failure",
                timestamp = "2023-10-20T12:05:00Z",
                status = degradedStatusWithoutRecovery,
                services = mapOf("database" to true, "cache" to true, "api" to false)
            )

            // Act
            val details = resultWithoutRecovery.getDetailsMap()

            // Assert
            details shouldHaveSize 1
            details["workingServices"] shouldBe "2/3"
            details.containsKey("estimatedRecoveryMinutes") shouldBe false
        }

        scenario("Unhealthy状態の場合、正しい詳細情報を返す") {
            // Arrange
            val unhealthyStatus = SystemStatus.Unhealthy(
                failedServicesCount = 2,
                totalServicesCount = 3,
                errorCodes = listOf("ERR_DATABASE", "ERR_API"),
                criticalFailure = true
            )
            val result = HealthCheckResult(
                message = "Major system failure",
                timestamp = "2023-10-20T12:10:00Z",
                status = unhealthyStatus,
                services = mapOf("database" to false, "cache" to true, "api" to false)
            )

            // Act
            val details = result.getDetailsMap()

            // Assert
            details shouldHaveSize 3
            details["failedServices"] shouldBe "2/3"
            details["errorCodes"] shouldBe "ERR_DATABASE,ERR_API"
            details["critical"] shouldBe "true"
        }
    }
})