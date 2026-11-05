package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.api.WeatherApiService
import com.example.data.model.City
import com.example.data.model.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WeatherRepository(
    private val apiService: WeatherApiService = ApiClient.weatherService
) {
    val defaultCities: List<City> = listOf(
        City("baku", "Bakı", "Azərbaycan", 40.4093, 49.8671, "Abşeron", isFavorite = true),
        City("ganja", "Gəncə", "Azərbaycan", 40.6828, 46.3606, "Gəncə-Daşkəsən", isFavorite = true),
        City("shusha", "Şuşa", "Azərbaycan", 39.7594, 46.7497, "Qarabağ", isFavorite = true),
        City("sumqayit", "Sumqayıt", "Azərbaycan", 40.5897, 49.6686, "Abşeron", isFavorite = false),
        City("nakhchivan", "Naxçıvan", "Azərbaycan", 39.2089, 45.4122, "Naxçıvan MR", isFavorite = false),
        City("sheki", "Şəki", "Azərbaycan", 41.1919, 47.1706, "Şəki-Zaqatala", isFavorite = false),
        City("quba", "Quba", "Azərbaycan", 41.3643, 48.5134, "Quba-Xaçmaz", isFavorite = false),
        City("lankaran", "Lənkəran", "Azərbaycan", 38.7543, 48.8506, "Lənkəran-Astara", isFavorite = false),
        City("mingachevir", "Mingəçevir", "Azərbaycan", 40.7640, 47.0595, "Mərkəzi Aran", isFavorite = false),
        City("khankendi", "Xankəndi", "Azərbaycan", 39.8177, 46.7528, "Qarabağ", isFavorite = false),
        City("istanbul", "İstanbul", "Türkiyə", 41.0082, 28.9784, "Mərmərə", isFavorite = false),
        City("london", "London", "Böyük Britaniya", 51.5074, -0.1278, "İngiltərə", isFavorite = false)
    )

    private val _savedCities = MutableStateFlow<List<City>>(defaultCities)
    val savedCities: StateFlow<List<City>> = _savedCities.asStateFlow()

    private val _selectedCity = MutableStateFlow<City>(defaultCities.first())
    val selectedCity: StateFlow<City> = _selectedCity.asStateFlow()

    private val cachedForecasts = mutableMapOf<String, WeatherResponse>()

    suspend fun getForecast(city: City): WeatherResponse {
        val key = "${city.latitude},${city.longitude}"
        return try {
            val response = apiService.getForecast(
                latitude = city.latitude,
                longitude = city.longitude
            )
            cachedForecasts[key] = response
            response
        } catch (e: Exception) {
            cachedForecasts[key] ?: throw e
        }
    }

    suspend fun searchCities(query: String, language: String = "az"): List<City> {
        if (query.trim().length < 2) return emptyList()
        val response = apiService.searchCity(name = query.trim(), count = 10, language = language)
        val results = response.results ?: emptyList()
        val favorites = _savedCities.value.filter { it.isFavorite }.map { it.name.lowercase() }.toSet()
        return results.map { item ->
            City(
                id = "${item.id}_${item.latitude}_${item.longitude}",
                name = item.name,
                country = item.country ?: item.countryCode ?: "",
                latitude = item.latitude,
                longitude = item.longitude,
                admin1 = item.admin1,
                isFavorite = favorites.contains(item.name.lowercase())
            )
        }
    }

    fun selectCity(city: City) {
        _selectedCity.value = city
        // If not present in saved cities list, add it
        val currentList = _savedCities.value.toMutableList()
        val existingIndex = currentList.indexOfFirst {
            it.name.equals(city.name, ignoreCase = true) ||
            (Math.abs(it.latitude - city.latitude) < 0.05 && Math.abs(it.longitude - city.longitude) < 0.05)
        }
        if (existingIndex < 0) {
            currentList.add(0, city)
            _savedCities.value = currentList
        }
    }

    fun toggleFavorite(city: City) {
        val currentList = _savedCities.value.toMutableList()
        val index = currentList.indexOfFirst {
            it.name.equals(city.name, ignoreCase = true) ||
            (Math.abs(it.latitude - city.latitude) < 0.05 && Math.abs(it.longitude - city.longitude) < 0.05)
        }
        if (index >= 0) {
            val item = currentList[index]
            val updated = item.copy(isFavorite = !item.isFavorite)
            currentList[index] = updated
            _savedCities.value = currentList
            if (_selectedCity.value.name.equals(city.name, ignoreCase = true)) {
                _selectedCity.value = updated
            }
        } else {
            val newCity = city.copy(isFavorite = true)
            currentList.add(0, newCity)
            _savedCities.value = currentList
            if (_selectedCity.value.name.equals(city.name, ignoreCase = true)) {
                _selectedCity.value = newCity
            }
        }
    }

    fun removeCity(city: City) {
        val currentList = _savedCities.value.toMutableList()
        currentList.removeAll {
            it.name.equals(city.name, ignoreCase = true) && !it.isFavorite
        }
        _savedCities.value = currentList
    }
}
