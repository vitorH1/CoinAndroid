package com.vitor.cryptotracker.data.models

data class Cryptocurrency(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    val current_price: Double,
    val market_cap: Long,
    val market_cap_rank: Int,
    val price_change_percentage_24h: Double,
    val circulating_supply: Double?,
    val total_supply: Double?,
    val high_24h: Double?,
    val low_24h: Double?,
    val last_updated: String
)