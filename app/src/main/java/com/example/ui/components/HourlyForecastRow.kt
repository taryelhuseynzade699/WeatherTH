package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.HourlyWeatherData
import com.example.data.model.UserSettings
import com.example.data.model.WeatherConditionHelper
import com.example.ui.theme.RainCyan
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SunGolden

data class HourlyItemData(
    val timeLabel: String,
    val temp: Double,
    val weatherCode: Int,
    val rainProb: Int,
    val isDay: Boolean,
    val isCurrentHour: Boolean
)

@Composable
fun HourlyForecastRow(
    hourly: HourlyWeatherData,
    settings: UserSettings,
    modifier: Modifier = Modifier
) {
    var showChart by remember { mutableStateOf(false) }

    val sectionTitle = if (settings.language == AppLanguage.AZ) "24 Saatlıq Proqnoz" else "24-Hour Forecast"
    val nowLabel = if (settings.language == AppLanguage.AZ) "İndi" else "Now"

    // Take next 24 hourly points
    val items = remember(hourly) {
        val total = hourly.time.size
        val count = total.coerceAtMost(24)
        (0 until count).map { i ->
            val rawTime = hourly.time[i]
            val hourPart = rawTime.substringAfter("T", "").take(5)
            val isCurrent = (i == 0)
            val label = if (isCurrent) nowLabel else hourPart

            // Estimate day/night based on hour part (e.g. 06:00 to 19:00 is day)
            val hourInt = hourPart.take(2).toIntOrNull() ?: 12
            val isDay = hourInt in 6..19

            val rainP = hourly.precipitationProbability?.getOrNull(i) ?: 0

            HourlyItemData(
                timeLabel = label,
                temp = hourly.temperature[i],
                weatherCode = hourly.weatherCode[i],
                rainProb = rainP,
                isDay = isDay,
                isCurrentHour = isCurrent
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hourly_forecast_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A).copy(alpha = 0.38f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            // Header with toggle for trend chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = SkyBlueLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = sectionTitle,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = { showChart = !showChart },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShowChart,
                        contentDescription = "Qrafik",
                        tint = if (showChart) SkyBlueLight else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (showChart) {
                Spacer(modifier = Modifier.height(4.dp))
                TempTrendChart(temperatures = items.map { it.temp })
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal scrolling hours
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(items) { _, item ->
                    val condition = WeatherConditionHelper.getCondition(item.weatherCode, item.isDay)

                    Box(
                        modifier = Modifier
                            .width(68.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (item.isCurrentHour) Color(0xFF38BDF8).copy(alpha = 0.25f)
                                else Color.White.copy(alpha = 0.06f)
                            )
                            .then(
                                if (item.isCurrentHour) Modifier.border(
                                    width = 1.dp,
                                    color = SkyBlueLight.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(18.dp)
                                ) else Modifier
                            )
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = item.timeLabel,
                                color = if (item.isCurrentHour) SkyBlueLight else Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                fontWeight = if (item.isCurrentHour) FontWeight.Bold else FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Icon(
                                imageVector = if (item.isDay) condition.dayIcon else condition.nightIcon,
                                contentDescription = null,
                                tint = if (item.isDay) SunGolden else Color(0xFF93C5FD),
                                modifier = Modifier.size(28.dp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            if (item.rainProb > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.WaterDrop,
                                        contentDescription = null,
                                        tint = RainCyan,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "${item.rainProb}%",
                                        color = RainCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = settings.formatTemp(item.temp),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
