package com.example.converter_money.network

import retrofit2.http.GET
import retrofit2.http.Query

data class RatesResponse(
    val base: String,
    val date: String?,
    val rates: Map<String, Double>?
)

interface ApiService {
    @GET("latest")
    suspend fun latest(
        @Query("base") base: String,
        @Query("symbols") symbols: String
    ): RatesResponse
}
