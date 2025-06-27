package com.vitor.cryptotracker.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitor.cryptotracker.data.models.OHLCData
import com.vitor.cryptotracker.data.repository.CryptoRepository
import com.vitor.cryptotracker.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: CryptoRepository
) : ViewModel() {

    private val _historicalData = MutableLiveData<Resource<List<OHLCData>>>()
    val historicalData: LiveData<Resource<List<OHLCData>>> = _historicalData

    fun fetchHistoricalData(coinSymbol: String, days: Int) {
        _historicalData.postValue(Resource.Loading())
        viewModelScope.launch {
            val result = repository.getHistoricalData(coinSymbol, days)
            _historicalData.postValue(result)
        }
    }
}