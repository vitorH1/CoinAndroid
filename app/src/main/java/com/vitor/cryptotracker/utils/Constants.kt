package com.vitor.cryptotracker.utils

object Constants {
    const val BASE_URL = "https://api.coingecko.com/api/v3/"
    const val DATABASE_NAME = "crypto_database"

    // SharedPreferences
    const val PREFS_NAME = "crypto_preferences"
    const val PREF_CURRENCY = "preferred_currency"

    // Default values
    const val DEFAULT_CURRENCY = "usd"
    const val CACHE_TIMEOUT = 300000L // 5 minutes
}