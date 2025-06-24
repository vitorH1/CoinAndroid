package com.vitor.cryptotracker.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vitor.cryptotracker.data.models.Cryptocurrency
import com.vitor.cryptotracker.data.repository.CryptoRepository
import com.vitor.cryptotracker.utils.Resource
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = CryptoRepository()

    private val _cryptocurrencies = MutableLiveData<Resource<List<Cryptocurrency>>>()
    val cryptocurrencies: LiveData<Resource<List<Cryptocurrency>>> = _cryptocurrencies

    fun fetchCryptocurrencies(currency: String = "usd") {
        _cryptocurrencies.value = Resource.loading()
        viewModelScope.launch {
            try {
                val data = repository.getTopCryptocurrencies(currency)
                _cryptocurrencies.value = Resource.success(data)
            } catch (e: Exception) {
                _cryptocurrencies.value = Resource.error("Erro ao buscar criptomoedas: ${e.message}")
            }
        }
    }
}