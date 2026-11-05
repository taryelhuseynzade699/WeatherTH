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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.data.model.CurrentWeatherData
import com.example.data.model.UserSettings
import com.example.data.model.WeatherCategory
import com.example.data.model.WeatherConditionHelper
import com.example.ui.theme.MintFresh
import com.example.ui.theme.SunGolden

data class WeatherTip(
    val title: String,
    val description: String
)

@Composable
fun AdviceCard(
    current: CurrentWeatherData,
    settings: UserSettings,
    modifier: Modifier = Modifier
) {
    val isAz = settings.language == AppLanguage.AZ
    val condition = WeatherConditionHelper.getCondition(current.weatherCode, current.isDay == 1)

    val tips = mutableListOf<WeatherTip>()

    // Rain / Precipitation check
    if (condition.category == WeatherCategory.RAIN || condition.category == WeatherCategory.DRIZZLE || current.precipitation > 0) {
        tips.add(
            WeatherTip(
                title = if (isAz) "Çətir götürün" else "Carry an Umbrella",
                description = if (isAz) "Yağıntı müşahidə olunur. Çətir və sukeçirməz ayaqqabı tövsiyə edilir."
                else "Rain observed. Don't forget an umbrella and waterproof shoes."
            )
        )
    }

    // Temperature check
    when {
        current.temperature <= 5 -> {
            tips.add(
                WeatherTip(
                    title = if (isAz) "Qalın geyinin" else "Dress Warmly",
                    description = if (isAz) "Hava soyuqdur. İsti gödəkçə, şərf və papaq taxmağınız məsləhətdir."
                    else "Chilly weather. Warm winter jacket and scarf recommended."
                )
            )
        }
        current.temperature in 6.0..17.0 -> {
            tips.add(
                WeatherTip(
                    title = if (isAz) "Mülayim sərinlik" else "Mild Coolness",
                    description = if (isAz) "Yüngül gödəkçə və ya sviter geyinmək idealdır."
                    else "A light jacket or sweater is suitable for today."
                )
            )
        }
        current.temperature in 18.0..26.0 -> {
            tips.add(
                WeatherTip(
                    title = if (isAz) "Əla hava" else "Pleasant Weather",
                    description = if (isAz) "Gəzinti, açıq hava idmanı və görüşlər üçün çox əlverişlidir."
                    else "Great conditions for walking, outdoor sports and activities."
                )
            )
        }
        current.temperature > 26.0 -> {
            tips.add(
                WeatherTip(
                    title = if (isAz) "İsti hava şəraiti" else "Warm Weather",
                    description = if (isAz) "Gündəlik su qəbulunu artırın və yüngül pambıq geyimlər seçin."
                    else "Stay hydrated with plenty of water and wear light clothing."
                )
            )
        }
    }

    // Wind check
    if (current.windSpeed >= 25.0) {
        tips.add(
            WeatherTip(
                title = if (isAz) "Küləkli hava" else "Windy Weather",
                description = if (isAz) "Küləyin sürəti yüksəkdir (${Math.round(current.windSpeed)} km/s). Küləkkeçirməz geyim seçin."
                else "High wind speeds (${Math.round(current.windSpeed)} km/h). Windbreaker jacket advised."
            )
        )
    }

    // UV check
    val uv = current.uvIndex ?: 0.0
    if (uv >= 6.0) {
        tips.add(
            WeatherTip(
                title = if (isAz) "Günəşdən qorunma" else "Sun Protection",
                description = if (isAz) "UV indeksi yüksəkdir ($uv). Gün eynəyi və qoruyucu krem tətbiq edin."
                else "UV index is high ($uv). Wear sunglasses and sunscreen."
            )
        )
    }

    if (tips.isEmpty()) {
        tips.add(
            WeatherTip(
                title = if (isAz) "Sakit və xoş gün" else "Calm Day",
                description = if (isAz) "Hər hansı təbii narahatlıq gözlənilmir. Gününüz uğurlu keçsin!"
                else "No weather warnings expected. Have a wonderful day!"
            )
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("advice_card"),
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
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SunGolden.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = SunGolden,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isAz) "Günün Məsləhətləri" else "Daily Weather Tips",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            tips.forEachIndexed { index, tip ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MintFresh,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tip.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tip.description,
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
                if (index < tips.size - 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
