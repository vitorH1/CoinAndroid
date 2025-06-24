package com.vitor.cryptotracker.data.repository

import com.vitor.cryptotracker.data.api.NetworkModule
import com.vitor.cryptotracker.data.models.Cryptocurrency
import com.vitor.cryptotracker.data.models.MarketChart
import com.vitor.cryptotracker.data.models.OHLCData

class CryptoRepository {

    private val api = NetworkModule.coinGeckoApi

    // Busca as principais criptomoedas pelo market cap
    suspend fun getTopCryptocurrencies(
        currency: String = "usd",
        perPage: Int = 100,
        page: Int = 1
    ): List<Cryptocurrency> {
        return api.getTopCryptocurrencies(currency = currency, perPage = perPage, page = page)
    }

    // Busca o histórico de preços para uma moeda específica
    suspend fun getMarketChart(
        id: String,
        currency: String = "usd",
        days: String = "7"
    ): MarketChart {
        return api.getMarketChart(id, currency = currency, days = days)
    }

    // Busca dados OHLC (Open, High, Low, Close) para uma moeda específica
    suspend fun getOHLCData(
        id: String,
        currency: String = "usd",
        days: Int = 7
    ): List<OHLCData> {
        val rawOhlc = api.getOHLCData(id, currency = currency, days = days)
        // Converte a lista bruta em uma lista de objetos OHLCData
        return rawOhlc.map { values ->
            OHLCData(
                timestamp = values[0].toLong(),
                open = values[1],
                high = values[2],
                low = values[3],
                close = values[4]
            )
        }
    }
}