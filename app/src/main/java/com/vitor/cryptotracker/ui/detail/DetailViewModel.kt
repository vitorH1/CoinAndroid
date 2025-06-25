package com.vitor.cryptotracker.ui.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitor.cryptotracker.data.models.MarketChart
import com.vitor.cryptotracker.data.models.OHLCData
import com.vitor.cryptotracker.data.models.Resource
import com.vitor.cryptotracker.data.repository.CryptoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException

class DetailViewModel : ViewModel() {
    private val repository = CryptoRepository()

    // Debounce - permite apenas uma chamada por vez (agora PÚBLICO)
    var isLoading = false
        private set

    // Cache simples: [coinId_period] -> dados
    private val marketChartCache = mutableMapOf<String, MarketChart>()
    private val ohlcCache = mutableMapOf<String, List<OHLCData>>()

    private val _marketChart = MutableLiveData<Resource<MarketChart>>()
    val marketChart: LiveData<Resource<MarketChart>> get() = _marketChart

    private val _ohlcData = MutableLiveData<Resource<List<OHLCData>>>()
    val ohlcData: LiveData<Resource<List<OHLCData>>> get() = _ohlcData

    private val _selectedPeriod = MutableLiveData("7")
    val selectedPeriod: LiveData<String> get() = _selectedPeriod

    private var marketChartJob: Job? = null
    private var ohlcJob: Job? = null

    fun fetchMarketChart(coinId: String, period: String = "7") {
        val cacheKey = "${coinId}_$period"

        // 1. PRIMEIRO: verifica se tem cache
        marketChartCache[cacheKey]?.let {
            _marketChart.value = Resource.success(it)
            isLoading = false
            return
        }

        // 2. Só aqui troca o período e dispara o loading
        _selectedPeriod.value = period
        _marketChart.value = Resource.loading()
        isLoading = true

        marketChartJob?.cancel()
        marketChartJob = viewModelScope.launch {
            try {
                val result = repository.getMarketChart(coinId, days = period)
                marketChartCache[cacheKey] = result
                _marketChart.value = Resource.success(result)
            } catch (e: IOException) {
                Log.e("DetailViewModel", "Erro de conexão: ${e.message}")
                _marketChart.value = Resource.error("Erro de conexão: ${e.message}")
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Erro ao carregar dados: ${e.message}")
                if (e.message?.contains("429") == true) {
                    _marketChart.value = Resource.error("Limite de requisições atingido. Aguarde alguns minutos.")
                } else {
                    _marketChart.value = Resource.error("Erro ao carregar dados: ${e.message}")
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchOHLCData(coinId: String, period: String = "7") {
        val cacheKey = "${coinId}_$period"

        // 1. PRIMEIRO: verifica se tem cache
        ohlcCache[cacheKey]?.let {
            _ohlcData.value = Resource.success(it)
            isLoading = false
            return
        }

        // 2. Só aqui troca o período e dispara o loading
        _selectedPeriod.value = period
        _ohlcData.value = Resource.loading()
        isLoading = true

        ohlcJob?.cancel()
        ohlcJob = viewModelScope.launch {
            try {
                val days = when (period) {
                    "1" -> 1
                    "7" -> 7
                    "30" -> 30
                    "90" -> 90
                    "365" -> 365
                    else -> 7
                }
                val result = repository.getOHLCData(coinId, days = days)
                ohlcCache[cacheKey] = result
                _ohlcData.value = Resource.success(result)
            } catch (e: IOException) {
                Log.e("DetailViewModel", "Erro de conexão: ${e.message}")
                _ohlcData.value = Resource.error("Erro de conexão: ${e.message}")
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Erro ao carregar dados OHLC: ${e.message}")
                if (e.message?.contains("429") == true) {
                    _ohlcData.value = Resource.error("Limite de requisições atingido. Aguarde alguns minutos.")
                } else {
                    _ohlcData.value = Resource.error("Erro ao carregar dados OHLC: ${e.message}")
                }
            } finally {
                isLoading = false
            }
        }
    }

    // Exemplo de debounce: só permite nova requisição se não estiver carregando
    fun changePeriod(coinId: String, period: String) {
        if (isLoading) return
        fetchMarketChart(coinId, period)
        fetchOHLCData(coinId, period)
    }
}