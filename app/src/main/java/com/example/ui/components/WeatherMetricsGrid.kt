package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.CurrentWeatherData
import com.example.data.model.DailyWeatherData
import com.example.data.model.UserSettings
import com.example.ui.theme.RainCyan
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SunGolden

@Composable
fun WeatherMetricsGrid(
    current: CurrentWeatherData,
    daily: DailyWeatherData?,
    settings: UserSettings,
    modifier: Modifier = Modifier
) {
    val isAz = settings.language == AppLanguage.AZ

    val sunriseTime = daily?.sunrise?.firstOrNull()?.substringAfter("T", "")?.take(5) ?: "06:30"
    val sunsetTime = daily?.sunset?.firstOrNull()?.substringAfter("T", "")?.take(5) ?: "18:45"
    val uv = current.uvIndex ?: daily?.uvIndexMax?.firstOrNull() ?: 3.0

    val uvLabel = when {
        uv < 3 -> if (isAz) "Aşağı" else "Low"
        uv < 6 -> if (isAz) "Mülayim" else "Moderate"
        uv < 8 -> if (isAz) "Yüksək" else "High"
        else -> if (isAz) "Çox yüksək" else "Very High"
    }

    val humidityLabel = when {
        current.humidity < 40 -> if (isAz) "Quru hava" else "Dry"
        current.humidity in 40..70 -> if (isAz) "Rahat və normal" else "Optimal"
        else -> if (isAz) "Yüksək rütubət" else "Humid"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weather_metrics_grid"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Wind & Humidity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = if (isAz) "Külək" else "Wind",
                value = settings.formatWind(current.windSpeed),
                subtitle = "${if (isAz) "İstiqamət" else "Direction"}: ${Math.round(current.windDirection)}°",
                icon = Icons.Filled.Air,
                iconTint = SkyBlueLight,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = if (isAz) "Rütubət" else "Humidity",
                value = "${current.humidity}%",
                subtitle = humidityLabel,
                icon = Icons.Filled.Opacity,
                iconTint = RainCyan,
                progress = current.humidity / 100f,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: UV Index & Pressure
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = if (isAz) "UV İndeksi" else "UV Index",
                value = "${String.format("%.1f", uv)}",
                subtitle = uvLabel,
                icon = Icons.Filled.WbSunny,
                iconTint = SunGolden,
                progress = (uv / 11.0).toFloat().coerceIn(0f, 1f),
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = if (isAz) "Təzyiq" else "Pressure",
                value = "${Math.round(current.surfacePressure)} hPa",
                subtitle = if (current.surfacePressure > 1013) (if (isAz) "Yüksək təzyiq" else "High") else (if (isAz) "Normal təzyiq" else "Normal"),
                icon = Icons.Filled.Compress,
                iconTint = Color(0xFFA78BFA),
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Precipitation & Sun Schedule
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = if (isAz) "Yağıntı" else "Precipitation",
                value = "${String.format("%.1f", current.precipitation)} mm",
                subtitle = if (current.precipitation > 0) (if (isAz) "Yağıntı müşahidə olunur" else "Precipitating") else (if (isAz) "Yağıntı yoxdur" else "No precipitation"),
                icon = Icons.Filled.WaterDrop,
                iconTint = Color(0xFF67E8F9),
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = if (isAz) "Gün Doğumu & Qürub" else "Sun Times",
                value = "$sunriseTime  |  $sunsetTime",
                subtitle = if (isAz) "Doğuş: $sunriseTime / Qürub: $sunsetTime" else "Dawn & Dusk",
                icon = Icons.Filled.WbTwilight,
                iconTint = Color(0xFFFDBA74),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    progress: Float? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A).copy(alpha = 0.38f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (progress != null) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = iconTint,
                    trackColor = Color.White.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp
            )
        }
    }
}
