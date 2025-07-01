package com.vitor.cryptotracker.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.vitor.cryptotracker.data.api.CryptoCompareApi
import com.vitor.cryptotracker.data.db.CoinDao
import com.vitor.cryptotracker.data.models.CoinEntity
import com.vitor.cryptotracker.data.models.CoinInfoContainer
import com.vitor.cryptotracker.data.models.OHLCData
import com.vitor.cryptotracker.utils.Constants
import com.vitor.cryptotracker.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoRepository @Inject constructor(
    private val api: CryptoCompareApi,
    private val coinDao: CoinDao
) {

    val allCoins: LiveData<List<CoinEntity>> = coinDao.getAllCoins()

    suspend fun refreshCoins() {
        try {
            val response = api.getTopCoins(limit = 50, apiKey = Constants.API_KEY)
            if (response.isSuccessful) {
                response.body()?.let { topCoinsResponse ->

                    // ******** A CORREÇÃO ESTÁ AQUI ********
                    // 1. Filtramos a lista para pegar apenas moedas que tenham dados de display
                    val validCoins = topCoinsResponse.data.filter { it.display != null }

                    // 2. Mapeamos apenas as moedas válidas
                    val coinEntities = validCoins.map { mapToEntity(it) }
                    coinDao.insertAll(coinEntities)
                }
            } else {
                Log.e("CryptoRepo", "Erro na API ao buscar moedas: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("CryptoRepo", "Exceção ao buscar moedas: ${e.message}", e)
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
                } ?: Resource.Error("A resposta dos dados históricos está vazia.")
            } else {
                Resource.Error("Erro de API: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Exceção: ${e.message}")
        }
    }

    // A função de mapeamento agora é 100% segura contra valores nulos
    private fun mapToEntity(container: CoinInfoContainer): CoinEntity {
        val displayUsd = container.display!!.usd // Usamos !! pois já garantimos que display não é nulo com o filtro
        return CoinEntity(
            id = container.coinInfo.id,
            symbol = container.coinInfo.name,
            fullName = container.coinInfo.fullName,
            imageUrl = container.coinInfo.imageUrl,
            price = displayUsd.price,
            changePct24Hour = displayUsd.changePct24Hour,
            marketCap = displayUsd.marketCap,
            volume24h = displayUsd.totalVolume24h,
            supply = displayUsd.supply
        )
    }
}