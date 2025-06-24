package com.vitor.cryptotracker.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.vitor.cryptotracker.utils.Constants

object NetworkModule {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val coinGeckoApi: CoinGeckoApi = retrofit.create(CoinGeckoApi::class.java)
}