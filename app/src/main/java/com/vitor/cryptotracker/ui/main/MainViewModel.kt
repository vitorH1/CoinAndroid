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

    init {
        fetchCryptocurrencies()
    }

    fun fetchCryptocurrencies() {
        _cryptocurrencies.postValue(Resource.Loading())
        viewModelScope.launch {
            val result = repository.getTopCoins()
            _cryptocurrencies.postValue(result)
        }
    }
}