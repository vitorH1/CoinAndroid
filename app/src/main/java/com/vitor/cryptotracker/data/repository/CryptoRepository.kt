package com.vitor.cryptotracker.data.repository

import com.vitor.cryptotracker.data.api.CryptoCompareApi
import com.vitor.cryptotracker.data.models.CoinInfoContainer
import com.vitor.cryptotracker.data.models.OHLCData
import com.vitor.cryptotracker.utils.Constants
import com.vitor.cryptotracker.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoRepository @Inject constructor(
    private val api: CryptoCompareApi
) {

    suspend fun getTopCoins(): Resource<List<CoinInfoContainer>> {
        return try {
            val response = api.getTopCoins(apiKey = Constants.API_KEY)
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it.data)
                } ?: Resource.Error("Response body is null")
            } else {
                Resource.Error("API Error: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Exception: ${e.message}")
        }
    }

    suspend fun getHistoricalData(coinSymbol: String, days: Int): Resource<List<OHLCData>> {
        return try {
            val response = api.getHistoricalDayData(
                fromSymbol = coinSymbol,
                limit = days,
                apiKey = Constants.API_KEY
            )
            if (response.isSuccessful) {
                response.body()?.data?.dailyData?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Response body or data is null")
            } else {
                Resource.Error("API Error: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Exception: ${e.message}")
        }
    }
}