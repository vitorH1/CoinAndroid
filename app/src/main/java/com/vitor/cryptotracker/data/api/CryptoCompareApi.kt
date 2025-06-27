package com.vitor.cryptotracker.data.api

import com.vitor.cryptotracker.data.models.HistoricalDataResponse
import com.vitor.cryptotracker.data.models.TopCoinsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoCompareApi {

    @GET("data/top/totalvolfull")
    suspend fun getTopCoins(
        @Query("limit") limit: Int = 50,
        @Query("tsym") targetSymbol: String = "USD",
        @Query("api_key") apiKey: String
    ): Response<TopCoinsResponse>

    @GET("data/v2/histoday")
    suspend fun getHistoricalDayData(
        @Query("fsym") fromSymbol: String,
        @Query("tsym") toSymbol: String = "USD",
        @Query("limit") limit: Int = 30,
        @Query("api_key") apiKey: String
    ): Response<HistoricalDataResponse>

}