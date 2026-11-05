package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

enum class WeatherCategory {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    THUNDERSTORM
}

data class WeatherInfo(
    val titleAz: String,
    val titleEn: String,
    val descriptionAz: String,
    val descriptionEn: String,
    val category: WeatherCategory,
    val dayIcon: ImageVector,
    val nightIcon: ImageVector
)

object WeatherConditionHelper {
    fun getCondition(code: Int, isDay: Boolean = true): WeatherInfo {
        return when (code) {
            0 -> WeatherInfo(
                titleAz = if (isDay) "Açıq hava" else "Açıq gecə",
                titleEn = if (isDay) "Clear Sky" else "Clear Night",
                descriptionAz = if (isDay) "Parlaq günəşli və aydın səma" else "Aydın ulduzlu səma",
                descriptionEn = if (isDay) "Bright sunny and clear sky" else "Clear starry sky",
                category = WeatherCategory.CLEAR,
                dayIcon = Icons.Filled.WbSunny,
                nightIcon = Icons.Filled.NightsStay
            )
            1, 2 -> WeatherInfo(
                titleAz = "Qismən buludlu",
                titleEn = "Partly Cloudy",
                descriptionAz = "Aralıqlarla günəşli və seyrək buludlu",
                descriptionEn = "Intermittent sun with scattered clouds",
                category = WeatherCategory.PARTLY_CLOUDY,
                dayIcon = Icons.Filled.WbCloudy,
                nightIcon = Icons.Filled.Cloud
            )
            3 -> WeatherInfo(
                titleAz = "Tutqun buludlu",
                titleEn = "Overcast",
                descriptionAz = "Səma tamamilə sıx buludlarla örtülüdür",
                descriptionEn = "Sky completely overcast with thick clouds",
                category = WeatherCategory.CLOUDY,
                dayIcon = Icons.Filled.Cloud,
                nightIcon = Icons.Filled.Cloud
            )
            45, 48 -> WeatherInfo(
                titleAz = "Dumanlı",
                titleEn = "Foggy",
                descriptionAz = "Görünüş məsafəsi məhduddur, duman müşahidə olunur",
                descriptionEn = "Visibility is restricted with fog",
                category = WeatherCategory.FOG,
                dayIcon = Icons.Filled.Air,
                nightIcon = Icons.Filled.Air
            )
            51, 53, 55 -> WeatherInfo(
                titleAz = "Çiskinli yağış",
                titleEn = "Drizzle",
                descriptionAz = "Xırda damcılı çiskin yağışı",
                descriptionEn = "Light fine drizzle",
                category = WeatherCategory.DRIZZLE,
                dayIcon = Icons.Filled.Grain,
                nightIcon = Icons.Filled.Grain
            )
            56, 57 -> WeatherInfo(
                titleAz = "Donan çiskin",
                titleEn = "Freezing Drizzle",
                descriptionAz = "Soyuq hava və donan çiskinli yağış",
                descriptionEn = "Cold freezing drizzle",
                category = WeatherCategory.DRIZZLE,
                dayIcon = Icons.Filled.AcUnit,
                nightIcon = Icons.Filled.AcUnit
            )
            61, 63 -> WeatherInfo(
                titleAz = "Mülayim yağış",
                titleEn = "Moderate Rain",
                descriptionAz = "Davamlı yağış müşahidə olunur",
                descriptionEn = "Steady moderate rainfall",
                category = WeatherCategory.RAIN,
                dayIcon = Icons.Filled.WaterDrop,
                nightIcon = Icons.Filled.WaterDrop
            )
            65 -> WeatherInfo(
                titleAz = "Güclü yağış",
                titleEn = "Heavy Rain",
                descriptionAz = "İntensiv və güclü yağış",
                descriptionEn = "Intense heavy rain",
                category = WeatherCategory.RAIN,
                dayIcon = Icons.Filled.WaterDrop,
                nightIcon = Icons.Filled.WaterDrop
            )
            66, 67 -> WeatherInfo(
                titleAz = "Donan yağış",
                titleEn = "Freezing Rain",
                descriptionAz = "Buz bağlayan soyuq yağış",
                descriptionEn = "Freezing cold rain",
                category = WeatherCategory.RAIN,
                dayIcon = Icons.Filled.AcUnit,
                nightIcon = Icons.Filled.AcUnit
            )
            71, 73 -> WeatherInfo(
                titleAz = "Qar yağışı",
                titleEn = "Snowfall",
                descriptionAz = "Yüngül və ya mülayim qar yağışı",
                descriptionEn = "Light to moderate snowfall",
                category = WeatherCategory.SNOW,
                dayIcon = Icons.Filled.AcUnit,
                nightIcon = Icons.Filled.AcUnit
            )
            75, 77 -> WeatherInfo(
                titleAz = "İntensiv qar",
                titleEn = "Heavy Snow",
                descriptionAz = "Güclü qar və şaxtalı hava",
                descriptionEn = "Heavy snowfall and frosty conditions",
                category = WeatherCategory.SNOW,
                dayIcon = Icons.Filled.AcUnit,
                nightIcon = Icons.Filled.AcUnit
            )
            80, 81, 82 -> WeatherInfo(
                titleAz = "Leysan xarakterli yağış",
                titleEn = "Rain Showers",
                descriptionAz = "Qısamüddətli intensiv leysan",
                descriptionEn = "Sudden heavy rain showers",
                category = WeatherCategory.RAIN,
                dayIcon = Icons.Filled.WaterDrop,
                nightIcon = Icons.Filled.WaterDrop
            )
            85, 86 -> WeatherInfo(
                titleAz = "Qar leysanı",
                titleEn = "Snow Showers",
                descriptionAz = "Qısamüddətli güclü qar çovğunu",
                descriptionEn = "Sudden snow showers",
                category = WeatherCategory.SNOW,
                dayIcon = Icons.Filled.AcUnit,
                nightIcon = Icons.Filled.AcUnit
            )
            95 -> WeatherInfo(
                titleAz = "Şimşək və ildırım",
                titleEn = "Thunderstorm",
                descriptionAz = "Şimşək çaxması və yağıntı",
                descriptionEn = "Thunderstorm and lightning activity",
                category = WeatherCategory.THUNDERSTORM,
                dayIcon = Icons.Filled.Thunderstorm,
                nightIcon = Icons.Filled.Thunderstorm
            )
            96, 99 -> WeatherInfo(
                titleAz = "Dolu və fırtına",
                titleEn = "Hail Storm",
                descriptionAz = "Şiddətli şimşək və dolu yağıntısı",
                descriptionEn = "Severe thunderstorm with hail",
                category = WeatherCategory.THUNDERSTORM,
                dayIcon = Icons.Filled.Thunderstorm,
                nightIcon = Icons.Filled.Thunderstorm
            )
            else -> WeatherInfo(
                titleAz = "Mülayim hava",
                titleEn = "Fair Weather",
                descriptionAz = "Sakit hava şəraiti",
                descriptionEn = "Calm weather conditions",
                category = WeatherCategory.CLEAR,
                dayIcon = Icons.Filled.WbSunny,
                nightIcon = Icons.Filled.NightsStay
            )
        }
    }
}
