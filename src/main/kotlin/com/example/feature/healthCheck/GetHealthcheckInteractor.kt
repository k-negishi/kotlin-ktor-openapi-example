package com.example.feature.healthCheck

import com.example.feature.healthCheck.domain.HealthCheckResult
import com.example.feature.healthCheck.domain.HealthCheckService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ヘルスチェック機能のインタラクター
 * ビジネスロジックを実装し、ドメインサービスを利用してヘルスチェックを実行
 */
class GetHealthcheckInteractor(private val healthCheckService: HealthCheckService) {

    /**
     * ヘルスチェック処理を実行する
     */
    suspend fun invoke(): HealthCheckResult = coroutineScope {
        val startTime = System.currentTimeMillis()
        
        // サービス状態のチェックを並行して実行
        val databaseCheck = async { healthCheckService.checkDatabaseConnection() }
        val cacheCheck = async { healthCheckService.checkCacheService() }
        val apiCheck = async { healthCheckService.checkExternalApiService() }
        
        // 非同期タスクを開始（結果は待たない）
        val loggingJob = launch {
            logHealthCheckExecution()
        }
        
        // 現在の日時を取得
        val currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        
        // 各サービスのチェック結果を待機
        val serviceStatuses = mapOf(
            "database" to databaseCheck.await(),
            "cache" to cacheCheck.await(),
            "api" to apiCheck.await()
        )
        
        // ログ記録が完了するのを待機
        loggingJob.join()
        
        // 応答時間を計算
        val responseTime = System.currentTimeMillis() - startTime
        
        // ドメインサービスを使って状態を判定
        val systemStatus = healthCheckService.determineSystemStatus(
            serviceStatuses = serviceStatuses,
            responseTimeMs = responseTime
        )
        
        // 状態に応じたメッセージを生成
        val statusMessage = healthCheckService.generateStatusMessage(systemStatus)
        
        // 結果を返却
        HealthCheckResult(
            message = statusMessage,
            timestamp = currentTime,
            status = systemStatus,
            services = serviceStatuses
        )
    }
    
    /**
     * ヘルスチェック実行のログを記録
     */
    private suspend fun logHealthCheckExecution() {
        delay(30)
        println("Health check executed at ${LocalDateTime.now()}")
    }
}