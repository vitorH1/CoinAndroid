package com.vitor.cryptotracker.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitor.cryptotracker.data.models.CoinInfoContainer
import com.vitor.cryptotracker.data.repository.CryptoRepository
import com.vitor.cryptotracker.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: CryptoRepository
) : ViewModel() {

    private val _cryptocurrencies = MutableLiveData<Resource<List<CoinInfoContainer>>>()
    val cryptocurrencies: LiveData<Resource<List<CoinInfoContainer>>> = _cryptocurrencies

    // Variável para guardar a lista completa e original
    private var originalCoinList = listOf<CoinInfoContainer>()

    init {
        fetchCryptocurrencies()
    }

    fun fetchCryptocurrencies() {
        _cryptocurrencies.postValue(Resource.Loading())
        viewModelScope.launch {
            when (val result = repository.getTopCoins()) {
                is Resource.Success -> {
                    // Quando o resultado for sucesso, guardamos a lista original
                    originalCoinList = result.data ?: emptyList()
                    _cryptocurrencies.postValue(result)
                }
                else -> {
                    _cryptocurrencies.postValue(result)
                }
            }
        }
    }

    // Nova função para pesquisar
    fun searchCoin(query: String?) {
        if (query.isNullOrBlank()) {
            // Se a pesquisa estiver vazia, mostra a lista original
            _cryptocurrencies.postValue(Resource.Success(originalCoinList))
            return
        }

        // Filtra a lista original pelo nome ou símbolo da moeda
        val filteredList = originalCoinList.filter { coinContainer ->
            coinContainer.coinInfo.fullName.contains(query, ignoreCase = true) ||
                    coinContainer.coinInfo.name.contains(query, ignoreCase = true)
        }
        _cryptocurrencies.postValue(Resource.Success(filteredList))
    }
}