package com.example.converter_money

import androidx.lifecycle.*
import com.example.converter_money.network.RetrofitClient
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _rate = MutableLiveData<Double?>()
    val rate: LiveData<Double?> = _rate

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchRate(base: String, target: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val resp = RetrofitClient.api.latest(base, target)
                val r = resp.rates?.get(target)
                // если base == target (или API не вернул), ставим 1.0
                _rate.value = r ?: if (base == target) 1.0 else null
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Unknown error"
                _rate.value = null
            } finally {
                _loading.value = false
            }
        }
    }
}
