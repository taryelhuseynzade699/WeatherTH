package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.DailyWeatherData
import com.example.data.model.UserSettings
import com.example.data.model.WeatherConditionHelper
import com.example.ui.theme.RainCyan
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SunGolden
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyForecastSection(
    daily: DailyWeatherData,
    settings: UserSettings,
    modifier: Modifier = Modifier
) {
    val isAz = settings.language == AppLanguage.AZ
    val title = if (isAz) "7 Günlük Proqnoz" else "7-Day Forecast"

    val minGlobal = daily.temperatureMin.minOrNull() ?: 0.0
    val maxGlobal = daily.temperatureMax.maxOrNull() ?: 1.0
    val totalRange = (maxGlobal - minGlobal).coerceAtLeast(1.0)

    var expandedDayIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_forecast_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A).copy(alpha = 0.38f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = SkyBlueLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val count = daily.time.size.coerceAtMost(7)
            (0 until count).forEach { i ->
                val dateStr = daily.time[i]
                val dayName = getDayLabel(dateStr, i, isAz)
                val code = daily.weatherCode[i]
                val condition = WeatherConditionHelper.getCondition(code, isDay = true)
                val conditionName = if (isAz) condition.titleAz else condition.titleEn
                val tMin = daily.temperatureMin[i]
                val tMax = daily.temperatureMax[i]
                val rainProb = daily.precipitationProbabilityMax?.getOrNull(i) ?: 0
                val uv = daily.uvIndexMax?.getOrNull(i)
                val wind = daily.windSpeedMax?.getOrNull(i)
                val sunrise = daily.sunrise?.getOrNull(i)?.substringAfter("T", "")?.take(5)
                val sunset = daily.sunset?.getOrNull(i)?.substringAfter("T", "")?.take(5)

                val isExpanded = expandedDayIndex == i

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            expandedDayIndex = if (isExpanded) null else i
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Day Name
                        Column(modifier = Modifier.width(90.dp)) {
                            Text(
                                text = dayName,
                                color = if (i == 0) SkyBlueLight else Color.White,
                                fontSize = 14.sp,
                                fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = dateStr.takeLast(5),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }

                        // Weather icon & rain chance
                        Row(
                            modifier = Modifier.width(70.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = condition.dayIcon,
                                contentDescription = conditionName,
                                tint = SunGolden,
                                modifier = Modifier.size(24.dp)
                            )
                            if (rainProb > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$rainProb%",
                                    color = RainCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Min temp
                        Text(
                            text = settings.formatTemp(tMin),
                            color = Color(0xFF93C5FD),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(42.dp)
                        )

                        // Gradient bar representing temperature spread
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            val startFrac = ((tMin - minGlobal) / totalRange).toFloat().coerceIn(0f, 1f)
                            val endFrac = ((tMax - minGlobal) / totalRange).toFloat().coerceIn(0f, 1f)
                            val barWidth = (endFrac - startFrac).coerceAtLeast(0.08f)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(barWidth)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF38BDF8),
                                                Color(0xFFF59E0B)
                                            )
                                        )
                                    )
                            )
                        }

                        // Max temp
                        Text(
                            text = settings.formatTemp(tMax),
                            color = Color(0xFFFCA5A5),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(42.dp)
                        )

                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Expanded Day Details
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.07f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = conditionName,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (wind != null) {
                                    Text(
                                        text = "${if (isAz) "Külək" else "Wind"}: ${settings.formatWind(wind)}",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp
                                    )
                                }
                                if (uv != null) {
                                    Text(
                                        text = "UV: $uv",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp
                                    )
                                }
                                if (sunrise != null && sunset != null) {
                                    Text(
                                        text = "☀ $sunrise - $sunset",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    if (i < count - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .height(0.5.dp)
                                .background(Color.White.copy(alpha = 0.08f))
                        )
                    }
                }
            }
        }
    }
}

private fun getDayLabel(dateStr: String, index: Int, isAz: Boolean): String {
    if (index == 0) return if (isAz) "Bugün" else "Today"
    if (index == 1) return if (isAz) "Sabah" else "Tomorrow"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = parser.parse(dateStr) ?: return dateStr
        val dayFormat = SimpleDateFormat("EEEE", if (isAz) Locale("az") else Locale.ENGLISH)
        val raw = dayFormat.format(date)
        raw.replaceFirstChar { it.uppercase() }
    } catch (e: Exception) {
        dateStr
    }
}
