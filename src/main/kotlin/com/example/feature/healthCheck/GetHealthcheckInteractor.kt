package com.example.feature.healthCheck

class GetHealthcheckInteractor {
    data class Result(
        val message: kotlin.String
    )
    fun invoke(): Result {
        return Result("Hello World!")
    }
}