package com.example.feature.healthCheck.domain

/**
 * ヘルスチェックの結果を表すドメインモデル
 */
data class HealthCheckResult(
    val message: String,
    val timestamp: String,
    val status: SystemStatus,
    val services: Map<String, Boolean>
) {
    /**
     * ステータスに応じた詳細情報を返す
     */
    fun getDetailsMap(): Map<String, String> = when (val systemStatus = status) {
        is SystemStatus.Healthy -> mapOf(
            "responseTimeMs" to systemStatus.responseTimeMs.toString()
        )
        
        is SystemStatus.Degraded -> {
            val info = mutableMapOf(
                "workingServices" to "${systemStatus.workingServicesCount}/${systemStatus.totalServicesCount}"
            )
            systemStatus.estimatedRecoveryTimeMinutes?.let {
                info["estimatedRecoveryMinutes"] = it.toString()
            }
            info
        }
        
        is SystemStatus.Unhealthy -> mapOf(
            "failedServices" to "${systemStatus.failedServicesCount}/${systemStatus.totalServicesCount}",
            "errorCodes" to systemStatus.errorCodes.joinToString(","),
            "critical" to systemStatus.criticalFailure.toString()
        )
    }
}