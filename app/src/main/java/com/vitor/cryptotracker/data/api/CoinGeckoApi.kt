package com.vitor.cryptotracker.data.api

import com.vitor.cryptotracker.data.models.Cryptocurrency
import com.vitor.cryptotracker.data.models.MarketChart
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoinGeckoApi {

    @GET("coins/markets")
    suspend fun getTopCryptocurrencies(
        @Query("vs_currency") currency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false
    ): List<Cryptocurrency>

    @GET("coins/{id}/market_chart")
    suspend fun getMarketChart(
        @Path("id") id: String,
        @Query("vs_currency") currency: String = "usd",
        @Query("days") days: String = "7"
    ): MarketChart

    @GET("coins/{id}/ohlc")
    suspend fun getOHLCData(
        @Path("id") id: String,
        @Query("vs_currency") currency: String = "usd",
        @Query("days") days: Int = 7
    ): List<List<Double>>
}