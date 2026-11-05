package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.City
import com.example.data.model.CurrentWeatherData
import com.example.data.model.UserSettings
import com.example.data.model.WeatherCategory
import com.example.data.model.WeatherConditionHelper
import com.example.data.model.WeatherResponse
import com.example.ui.components.AdviceCard
import com.example.ui.components.CurrentWeatherCard
import com.example.ui.components.DailyForecastSection
import com.example.ui.components.HourlyForecastRow
import com.example.ui.components.WeatherBackground
import com.example.ui.components.WeatherMetricsGrid
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SunGolden
import com.example.ui.viewmodel.WeatherUiState

@Composable
fun HomeScreen(
    uiState: WeatherUiState,
    currentCity: City,
    savedCities: List<City>,
    settings: UserSettings,
    onRefresh: () -> Unit,
    onCityClick: () -> Unit,
    onCitySelect: (City) -> Unit,
    onFavoriteToggle: (City) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAz = settings.language == AppLanguage.AZ

    val currentWeather: CurrentWeatherData? = when (uiState) {
        is WeatherUiState.Success -> uiState.forecast.current
        else -> null
    }

    val isDay = currentWeather?.isDay != 0
    val condition = currentWeather?.let {
        WeatherConditionHelper.getCondition(it.weatherCode, isDay)
    }
    val category = condition?.category ?: WeatherCategory.CLEAR

    WeatherBackground(category = category, isDay = isDay, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top App Bar
            HomeTopBar(
                currentCity = currentCity,
                isLoading = uiState is WeatherUiState.Loading,
                settings = settings,
                onRefresh = onRefresh,
                onCityClick = onCityClick,
                onOpenSearch = onOpenSearch,
                onOpenSettings = onOpenSettings,
                onToggleSound = onToggleSound
            )

            // Quick City Switcher Row (Favorites & selected)
            QuickCityRow(
                currentCity = currentCity,
                cities = savedCities.filter { it.isFavorite || it.name == currentCity.name },
                onCitySelect = onCitySelect
            )

            // Content Area depending on state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (uiState) {
                    is WeatherUiState.Loading -> {
                        LoadingView(isAz = isAz)
                    }

                    is WeatherUiState.Error -> {
                        ErrorView(
                            message = uiState.message,
                            isAz = isAz,
                            onRetry = onRefresh
                        )
                    }

                    is WeatherUiState.Success -> {
                        WeatherContentView(
                            forecast = uiState.forecast,
                            city = currentCity,
                            settings = settings,
                            onFavoriteToggle = { onFavoriteToggle(currentCity) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    currentCity: City,
    isLoading: Boolean,
    settings: UserSettings,
    onRefresh: () -> Unit,
    onCityClick: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleSound: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "refresh")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title & Logo
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(SunGolden.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.WbSunny,
                    contentDescription = null,
                    tint = SunGolden,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "WeatherTH",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }

        // Action Icons
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sound Effect Toggle Button
            IconButton(
                onClick = onToggleSound,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("sound_button")
            ) {
                Icon(
                    imageVector = if (settings.soundEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                    contentDescription = if (settings.soundEnabled) "Səsi söndür" else "Səsi yandır",
                    tint = if (settings.soundEnabled) SkyBlueLight else Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Yenilə",
                    tint = Color.White,
                    modifier = Modifier
                        .size(22.dp)
                        .then(if (isLoading) Modifier.rotate(angle) else Modifier)
                )
            }

            IconButton(
                onClick = onOpenSearch,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("search_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Şəhər axtarışı",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Tənzimləmələr",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickCityRow(
    currentCity: City,
    cities: List<City>,
    onCitySelect: (City) -> Unit
) {
    if (cities.size <= 1) return

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cities) { city ->
            val isSelected = city.name.equals(currentCity.name, ignoreCase = true)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) SkyBlueLight.copy(alpha = 0.35f)
                        else Color(0xFF0F172A).copy(alpha = 0.45f)
                    )
                    .then(
                        Modifier.border(
                            1.dp,
                            if (isSelected) SkyBlueLight.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.16f),
                            RoundedCornerShape(20.dp)
                        )
                    )
                    .clickable { onCitySelect(city) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = if (isSelected) SkyBlueLight else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = city.name,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherContentView(
    forecast: WeatherResponse,
    city: City,
    settings: UserSettings,
    onFavoriteToggle: () -> Unit
) {
    val current = forecast.current ?: return
    val tempMax = forecast.daily?.temperatureMax?.firstOrNull()
    val tempMin = forecast.daily?.temperatureMin?.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Current Weather Hero
        CurrentWeatherCard(
            city = city,
            current = current,
            tempMax = tempMax,
            tempMin = tempMin,
            settings = settings,
            onFavoriteToggle = onFavoriteToggle
        )

        // Hourly Forecast
        forecast.hourly?.let { hourly ->
            HourlyForecastRow(
                hourly = hourly,
                settings = settings
            )
        }

        // Daily 7-Day Forecast
        forecast.daily?.let { daily ->
            DailyForecastSection(
                daily = daily,
                settings = settings
            )
        }

        // Detailed Meteorological Metrics Grid
        WeatherMetricsGrid(
            current = current,
            daily = forecast.daily,
            settings = settings
        )

        // Smart Weather Advisor Card
        AdviceCard(
            current = current,
            settings = settings
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun LoadingView(isAz: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = SkyBlueLight,
            strokeWidth = 3.dp
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = if (isAz) "Hava məlumatları yenilənir..." else "Loading weather data...",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ErrorView(
    message: String,
    isAz: Boolean,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = Color(0xFFF87171),
            modifier = Modifier.size(52.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isAz) "Xəta baş verdi" else "Something went wrong",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = SkyBlueLight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isAz) "Yenidən yoxla" else "Retry",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
