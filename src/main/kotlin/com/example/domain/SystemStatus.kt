package com.example.domain

/**
 * システムの健康状態を表すシールドクラス階層
 */
sealed class SystemStatus {
    // システム全体が正常
    data class Healthy(val responseTimeMs: Long) : SystemStatus()
    
    // 一部サービスに問題がある
    data class Degraded(
        val workingServicesCount: Int, 
        val totalServicesCount: Int,
        val estimatedRecoveryTimeMinutes: Int?
    ) : SystemStatus()
    
    // 重大な問題が発生している
    data class Unhealthy(
        val failedServicesCount: Int,
        val totalServicesCount: Int,
        val errorCodes: List<String>,
        val criticalFailure: Boolean
    ) : SystemStatus()
    
    // システム状態をStringに変換
    fun toStatusString(): String = when(this) {
        is Healthy -> "healthy"
        is Degraded -> "degraded"
        is Unhealthy -> "unhealthy"
    }
}