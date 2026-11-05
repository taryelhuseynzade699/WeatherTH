package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.audio.WeatherSoundManager
import com.example.data.model.WeatherCategory
import com.example.data.model.WeatherConditionHelper
import com.example.ui.screens.CitiesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.WeatherTHTheme
import com.example.ui.viewmodel.WeatherUiState
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
    val context = LocalContext.current
    val soundManager = remember { WeatherSoundManager(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> soundManager.pause()
                Lifecycle.Event.ON_RESUME -> soundManager.resume()
                Lifecycle.Event.ON_DESTROY -> soundManager.release()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            soundManager.release()
        }
    }

    var currentScreen by remember { mutableStateOf(CurrentScreen.HOME) }

    val uiState by viewModel.uiState.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()
    val savedCities by viewModel.savedCities.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    // Determine current weather category for audio
    val currentWeather = (uiState as? WeatherUiState.Success)?.forecast?.current
    val isDay = currentWeather?.isDay != 0
    val condition = currentWeather?.let { WeatherConditionHelper.getCondition(it.weatherCode, isDay) }
    val category = condition?.category ?: WeatherCategory.CLEAR

    // Update ambient sound when weather condition or sound setting changes
    LaunchedEffect(category, isDay, settings.soundEnabled, settings.soundVolume, currentScreen) {
        if (currentScreen == CurrentScreen.HOME && settings.soundEnabled) {
            soundManager.setVolume(settings.soundVolume)
            soundManager.setSoundEnabled(true, category, isDay)
            soundManager.playAmbient(category, isDay)
        } else {
            soundManager.pause()
        }
    }

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
                    onRefresh = {
                        soundManager.playClick()
                        viewModel.refresh()
                    },
                    onCityClick = {
                        soundManager.playClick()
                        currentScreen = CurrentScreen.CITIES
                    },
                    onCitySelect = { city ->
                        soundManager.playClick()
                        viewModel.selectCity(city)
                    },
                    onFavoriteToggle = { city ->
                        soundManager.playClick()
                        viewModel.toggleFavorite(city)
                    },
                    onOpenSearch = {
                        soundManager.playClick()
                        currentScreen = CurrentScreen.CITIES
                    },
                    onOpenSettings = {
                        soundManager.playClick()
                        currentScreen = CurrentScreen.SETTINGS
                    },
                    onToggleSound = {
                        soundManager.playClick()
                        viewModel.toggleSound()
                    },
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
                        soundManager.playClick()
                        viewModel.selectCity(city)
                        currentScreen = CurrentScreen.HOME
                    },
                    onFavoriteToggle = { city ->
                        soundManager.playClick()
                        viewModel.toggleFavorite(city)
                    },
                    onBack = {
                        soundManager.playClick()
                        currentScreen = CurrentScreen.HOME
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            CurrentScreen.SETTINGS -> {
                SettingsScreen(
                    settings = settings,
                    onTemperatureUnitChange = { u ->
                        soundManager.playClick()
                        viewModel.setTemperatureUnit(u)
                    },
                    onWindSpeedUnitChange = { w ->
                        soundManager.playClick()
                        viewModel.setWindSpeedUnit(w)
                    },
                    onLanguageChange = { l ->
                        soundManager.playClick()
                        viewModel.setLanguage(l)
                    },
                    onSoundToggle = { enabled ->
                        soundManager.playClick()
                        viewModel.setSoundEnabled(enabled)
                    },
                    onSoundVolumeChange = { vol ->
                        viewModel.setSoundVolume(vol)
                        soundManager.setVolume(vol)
                    },
                    onPlaySampleSound = { sampleType ->
                        when (sampleType) {
                            "rain" -> {
                                soundManager.setSoundEnabled(true)
                                soundManager.playAmbient(WeatherCategory.RAIN, true)
                            }
                            "thunder" -> soundManager.playThunder()
                            "wind" -> {
                                soundManager.setSoundEnabled(true)
                                soundManager.playAmbient(WeatherCategory.CLOUDY, true)
                            }
                            "night" -> {
                                soundManager.setSoundEnabled(true)
                                soundManager.playAmbient(WeatherCategory.CLEAR, false)
                            }
                            else -> soundManager.playClick()
                        }
                    },
                    onBack = {
                        soundManager.playClick()
                        currentScreen = CurrentScreen.HOME
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
