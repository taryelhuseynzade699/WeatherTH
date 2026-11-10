package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CitiesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.WeatherTHTheme
import com.example.ui.viewmodel.WeatherViewModel

enum class CurrentScreen {
    HOME,
    CITIES,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherTHTheme {
                WeatherApp()
            }
        }
    }
}

@Composable
fun WeatherApp(
    viewModel: WeatherViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(CurrentScreen.HOME) }

    val uiState by viewModel.uiState.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()
    val savedCities by viewModel.savedCities.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    Crossfade(
        targetState = currentScreen,
        modifier = Modifier.fillMaxSize(),
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            CurrentScreen.HOME -> {
                HomeScreen(
                    uiState = uiState,
                    currentCity = currentCity,
                    savedCities = savedCities,
                    settings = settings,
                    onRefresh = { viewModel.refresh() },
                    onCityClick = { currentScreen = CurrentScreen.CITIES },
                    onCitySelect = { city -> viewModel.selectCity(city) },
                    onFavoriteToggle = { city -> viewModel.toggleFavorite(city) },
                    onOpenSearch = { currentScreen = CurrentScreen.CITIES },
                    onOpenSettings = { currentScreen = CurrentScreen.SETTINGS },
                    modifier = Modifier.fillMaxSize()
                )
            }

            CurrentScreen.CITIES -> {
                CitiesScreen(
                    currentCity = currentCity,
                    savedCities = savedCities,
                    searchQuery = searchQuery,
                    searchResults = searchResults,
                    isSearching = isSearching,
                    settings = settings,
                    onSearchQueryChange = { q -> viewModel.onSearchQueryChanged(q) },
                    onCitySelect = { city ->
                        viewModel.selectCity(city)
                        currentScreen = CurrentScreen.HOME
                    },
                    onFavoriteToggle = { city -> viewModel.toggleFavorite(city) },
                    onBack = { currentScreen = CurrentScreen.HOME },
                    modifier = Modifier.fillMaxSize()
                )
            }

            CurrentScreen.SETTINGS -> {
                SettingsScreen(
                    settings = settings,
                    onTemperatureUnitChange = { u -> viewModel.setTemperatureUnit(u) },
                    onWindSpeedUnitChange = { w -> viewModel.setWindSpeedUnit(w) },
                    onLanguageChange = { l -> viewModel.setLanguage(l) },
                    onBack = { currentScreen = CurrentScreen.HOME },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
