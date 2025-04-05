package com.example.domain

import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * 各種サービスの健康状態をチェックするドメインサービス
 */
class HealthCheckService {

    /**
     * データベース接続をチェックする
     */
    suspend fun checkDatabaseConnection(): Boolean {
        delay(100) // 実際のDBチェックを模擬
        return Random.nextInt(10) > 1 // 90%の確率で成功
    }
    
    /**
     * キャッシュサービスをチェックする
     */
    suspend fun checkCacheService(): Boolean {
        delay(50) // キャッシュチェックは比較的高速
        return Random.nextInt(10) > 0 // 高い確率で成功
    }
    
    /**
     * 外部APIサービスをチェックする
     */
    suspend fun checkExternalApiService(): Boolean {
        delay(200) // 外部API呼び出しを模擬（最も時間がかかる）
        return Random.nextInt(10) > 2 // 80%の確率で成功
    }
    
    /**
     * サービスの状態からシステム全体の状態を判定
     */
    fun determineSystemStatus(
        serviceStatuses: Map<String, Boolean>,
        responseTimeMs: Long
    ): SystemStatus {
        val isAllHealthy = serviceStatuses.values.all { it }
        val workingServicesCount = serviceStatuses.values.count { it }
        val failedServicesCount = serviceStatuses.size - workingServicesCount
        
        return if (isAllHealthy) {
            // 全て正常な場合
            SystemStatus.Healthy(responseTimeMs)
        } else if (workingServicesCount >= serviceStatuses.size / 2) {
            // 半数以上のサービスが正常なら劣化状態
            val estimatedRecovery = if (Random.nextBoolean()) Random.nextInt(5, 30) else null
            SystemStatus.Degraded(
                workingServicesCount = workingServicesCount,
                totalServicesCount = serviceStatuses.size,
                estimatedRecoveryTimeMinutes = estimatedRecovery
            )
        } else {
            // 半数以上のサービスが異常なら重大な障害
            val errorCodes = serviceStatuses.filterValues { !it }.keys
                .map { "ERR_${it.uppercase()}" }
            val isCritical = failedServicesCount > serviceStatuses.size * 0.7
            
            SystemStatus.Unhealthy(
                failedServicesCount = failedServicesCount,
                totalServicesCount = serviceStatuses.size,
                errorCodes = errorCodes,
                criticalFailure = isCritical
            )
        }
    }
    
    /**
     * システム状態に応じたメッセージを生成
     */
    fun generateStatusMessage(status: SystemStatus): String = when (status) {
        is SystemStatus.Healthy -> 
            "All systems operational (response time: ${status.responseTimeMs}ms)"
            
        is SystemStatus.Degraded -> {
            val recoveryMsg = status.estimatedRecoveryTimeMinutes?.let { 
                "Estimated recovery in $it minutes" 
            } ?: "Recovery time unknown"
            
            "${status.workingServicesCount}/${status.totalServicesCount} " +
            "services operational. $recoveryMsg"
        }
        
        is SystemStatus.Unhealthy -> {
            val criticalMsg = if (status.criticalFailure) "CRITICAL FAILURE: " else ""
            val errorsList = status.errorCodes.joinToString(", ")
            
            "${criticalMsg}${status.failedServicesCount}/${status.totalServicesCount} " +
            "services down. Error codes: $errorsList"
        }
    }
}