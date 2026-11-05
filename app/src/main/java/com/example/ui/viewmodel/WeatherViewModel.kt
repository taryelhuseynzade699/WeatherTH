package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppLanguage
import com.example.data.model.City
import com.example.data.model.DailyWeatherData
import com.example.data.model.HourlyWeatherData
import com.example.data.model.TemperatureUnit
import com.example.data.model.UserSettings
import com.example.data.model.WeatherResponse
import com.example.data.model.WindSpeedUnit
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val forecast: WeatherResponse) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel(
    private val repository: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    val currentCity: StateFlow<City> = repository.selectedCity
    val savedCities: StateFlow<List<City>> = repository.savedCities

    private val _userSettings = MutableStateFlow(UserSettings())
    val userSettings: StateFlow<UserSettings> = _userSettings.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<City>>(emptyList())
    val searchResults: StateFlow<List<City>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadForecast(currentCity.value)
    }

    fun loadForecast(city: City) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val forecast = repository.getForecast(city)
                _uiState.value = WeatherUiState.Success(forecast)
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(
                    e.localizedMessage ?: "Hava məlumatları yüklənərkən xəta baş verdi"
                )
            }
        }
    }

    fun refresh() {
        loadForecast(currentCity.value)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(400) // Debounce typing
            try {
                val results = repository.searchCities(
                    query = query,
                    language = _userSettings.value.language.code
                )
                _searchResults.value = results
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _isSearching.value = false
    }

    fun selectCity(city: City) {
        repository.selectCity(city)
        clearSearch()
        loadForecast(city)
    }

    fun toggleFavorite(city: City) {
        repository.toggleFavorite(city)
    }

    fun removeCity(city: City) {
        repository.removeCity(city)
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        _userSettings.value = _userSettings.value.copy(temperatureUnit = unit)
    }

    fun setWindSpeedUnit(unit: WindSpeedUnit) {
        _userSettings.value = _userSettings.value.copy(windSpeedUnit = unit)
    }

    fun setLanguage(language: AppLanguage) {
        _userSettings.value = _userSettings.value.copy(language = language)
    }

    fun toggleSound() {
        val current = _userSettings.value.soundEnabled
        _userSettings.value = _userSettings.value.copy(soundEnabled = !current)
    }

    fun setSoundEnabled(enabled: Boolean) {
        _userSettings.value = _userSettings.value.copy(soundEnabled = enabled)
    }

    fun setSoundVolume(volume: Float) {
        _userSettings.value = _userSettings.value.copy(soundVolume = volume)
    }
}
