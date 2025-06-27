package com.vitor.cryptotracker.data.models

import com.google.gson.annotations.SerializedName

// Resposta da API para dados históricos
data class HistoricalDataResponse(
    @SerializedName("Data")
    val data: HistoricalData
)

data class HistoricalData(
    @SerializedName("Data")
    val dailyData: List<OHLCData>
)

// Nosso novo modelo para os dados de vela (OHLC)
data class OHLCData(
    @SerializedName("time")
    val time: Long,
    @SerializedName("high")
    val high: Float,
    @SerializedName("low")
    val low: Float,
    @SerializedName("open")
    val open: Float,
    @SerializedName("close")
    val close: Float
)