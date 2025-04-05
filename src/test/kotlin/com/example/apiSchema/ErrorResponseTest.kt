package com.example.apiSchema

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ErrorResponseTest : StringSpec({
    val json = Json { prettyPrint = true }
    
    "ErrorResponseを正しくシリアライズできる" {
        val errorResponse = ErrorResponse(
            code = 404,
            message = "Resource not found"
        )
        
        val serialized = json.encodeToString(errorResponse)
        
        // JSONを正しく検証する
        serialized.contains("\"code\":") shouldBe true
        serialized.contains("404") shouldBe true
        serialized.contains("\"message\":") shouldBe true
        serialized.contains("Resource not found") shouldBe true
    }
    
    "さまざまなエラーコードとメッセージでErrorResponseが正しく動作する" {
        checkAll(100, Arb.int(400, 599), Arb.string(10..50)) { code, message ->
            val errorResponse = ErrorResponse(
                code = code,
                message = message
            )
            
            errorResponse.code shouldBe code
            errorResponse.message shouldBe message
            
            // シリアライズとデシリアライズをテスト
            val serialized = json.encodeToString(errorResponse)
            val deserialized = json.decodeFromString<ErrorResponse>(serialized)
            
            deserialized.code shouldBe errorResponse.code
            deserialized.message shouldBe errorResponse.message
        }
    }
    
    "detailsを含むErrorResponseを正しくシリアライズできる" {
        val errorResponse = ErrorResponse(
            code = 400,
            message = "Bad Request",
            details = mapOf(
                "field" to "username",
                "error" to "must not be empty"
            )
        )
        
        val serialized = json.encodeToString(errorResponse)
        
        serialized.contains("\"details\"") shouldBe true
        serialized.contains("\"field\"") shouldBe true
        serialized.contains("\"username\"") shouldBe true
        serialized.contains("\"error\"") shouldBe true
        serialized.contains("must not be empty") shouldBe true
    }
    
    "複数のエラー詳細を含むErrorResponseを正しくシリアライズできる" {
        val details = mapOf(
            "field" to "email",
            "error" to "invalid format",
            "code" to "VALIDATION_ERROR",
            "timestamp" to "2025-04-06T12:00:00Z"
        )
        
        val errorResponse = ErrorResponse(
            code = 422,
            message = "Validation Error",
            details = details
        )
        
        val serialized = json.encodeToString(errorResponse)
        serialized.contains("\"details\"") shouldBe true
        serialized.contains("\"field\"") shouldBe true
        serialized.contains("\"email\"") shouldBe true
        serialized.contains("\"error\"") shouldBe true
        serialized.contains("invalid format") shouldBe true
        
        // デシリアライズも検証
        val deserialized = json.decodeFromString<ErrorResponse>(serialized)
        deserialized.details?.get("field") shouldBe "email"
        deserialized.details?.get("error") shouldBe "invalid format"
        deserialized.details?.get("code") shouldBe "VALIDATION_ERROR"
        deserialized.details?.get("timestamp") shouldBe "2025-04-06T12:00:00Z"
    }
})