package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.TemperatureUnit
import com.example.data.model.UserSettings
import com.example.data.model.WindSpeedUnit
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SunGolden

@Composable
fun SettingsScreen(
    settings: UserSettings,
    onTemperatureUnitChange: (TemperatureUnit) -> Unit,
    onWindSpeedUnitChange: (WindSpeedUnit) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val isAz = settings.language == AppLanguage.AZ
    val screenTitle = if (isAz) "Tənzimləmələr" else "Settings"
    val tempTitle = if (isAz) "Temperatur Vahidi" else "Temperature Unit"
    val windTitle = if (isAz) "Külək Sürəti Vahidi" else "Wind Speed Unit"
    val langTitle = if (isAz) "Tətbiq Dili" else "App Language"
    val aboutTitle = if (isAz) "WeatherTH Haqqında" else "About WeatherTH"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = screenTitle,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Temperature Unit
            SettingsCard(
                title = tempTitle,
                icon = Icons.Filled.Thermostat,
                iconTint = SunGolden
            ) {
                TemperatureUnit.entries.forEach { unit ->
                    val label = if (isAz) unit.labelAz else unit.labelEn
                    val selected = settings.temperatureUnit == unit
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onTemperatureUnitChange(unit) }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                        RadioButton(
                            selected = selected,
                            onClick = { onTemperatureUnitChange(unit) },
                            colors = RadioButtonDefaults.colors(selectedColor = SkyBlueLight)
                        )
                    }
                }
            }

            // Wind Speed Unit
            SettingsCard(
                title = windTitle,
                icon = Icons.Filled.Air,
                iconTint = SkyBlueLight
            ) {
                WindSpeedUnit.entries.forEach { unit ->
                    val label = if (isAz) unit.labelAz else unit.labelEn
                    val selected = settings.windSpeedUnit == unit
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onWindSpeedUnitChange(unit) }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                        RadioButton(
                            selected = selected,
                            onClick = { onWindSpeedUnitChange(unit) },
                            colors = RadioButtonDefaults.colors(selectedColor = SkyBlueLight)
                        )
                    }
                }
            }

            // Language
            SettingsCard(
                title = langTitle,
                icon = Icons.Filled.Language,
                iconTint = Color(0xFFA78BFA)
            ) {
                AppLanguage.entries.forEach { lang ->
                    val selected = settings.language == lang
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onLanguageChange(lang) }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = lang.label,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                        RadioButton(
                            selected = selected,
                            onClick = { onLanguageChange(lang) },
                            colors = RadioButtonDefaults.colors(selectedColor = SkyBlueLight)
                        )
                    }
                }
            }

            // About Card
            SettingsCard(
                title = aboutTitle,
                icon = Icons.Filled.Info,
                iconTint = Color(0xFF34D399)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "WeatherTH - Dəqiq Hava Proqnozu",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Versiya 1.0",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isAz)
                            "Məlumat mənbəyi: Open-Meteo qlobal meteoroloji API. Dəqiq və real vaxt rejimində hava proqnozu təqdim edir."
                        else
                            "Data source: Open-Meteo global meteorological API. Providing accurate real-time forecasts worldwide.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}
