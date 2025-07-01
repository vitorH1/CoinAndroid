package com.vitor.cryptotracker.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitor.cryptotracker.data.models.CoinEntity
import com.vitor.cryptotracker.data.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: CryptoRepository
) : ViewModel() {

    // Fonte de dados principal, vinda do banco de dados
    private val allCoinsFromDb = repository.allCoins

    // LiveData que a UI irá observar. Ele "escuta" a fonte de dados principal
    // e também pode ser modificado pela lógica de pesquisa.
    private val _cryptocurrencies = MediatorLiveData<List<CoinEntity>>()
    val cryptocurrencies: LiveData<List<CoinEntity>> = _cryptocurrencies

    private var currentQuery: String? = null

    init {
        // Conecta o LiveData da UI com a fonte de dados do banco
        _cryptocurrencies.addSource(allCoinsFromDb) { originalList ->
            // Toda vez que o banco de dados muda, nós aplicamos o filtro atual
            applyFilter(currentQuery, originalList)
        }

        // Busca dados frescos da rede ao iniciar
        fetchCryptocurrencies()
    }

    // Renomeado de volta para corresponder ao que a MainActivity chama
    fun fetchCryptocurrencies() {
        viewModelScope.launch {
            repository.refreshCoins()
        }
    }

    // Função de pesquisa que a MainActivity vai chamar
    fun searchCoin(query: String?) {
        currentQuery = query
        applyFilter(query, allCoinsFromDb.value)
    }

    private fun applyFilter(query: String?, originalList: List<CoinEntity>?) {
        if (originalList == null) return

        val filteredList = if (query.isNullOrBlank()) {
            originalList
        } else {
            originalList.filter { coin ->
                coin.fullName.contains(query, ignoreCase = true) ||
                        coin.symbol.contains(query, ignoreCase = true)
            }
        }
        _cryptocurrencies.postValue(filteredList)
    }
}