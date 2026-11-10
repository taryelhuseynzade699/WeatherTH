package com.example.data.model

enum class TemperatureUnit(val symbol: String, val labelAz: String, val labelEn: String) {
    CELSIUS("°C", "Selsi (°C)", "Celsius (°C)"),
    FAHRENHEIT("°F", "Farenheyt (°F)", "Fahrenheit (°F)")
}

enum class WindSpeedUnit(val symbol: String, val labelAz: String, val labelEn: String) {
    KMH("km/s", "Kilometr/saat (km/s)", "Kilometers per hour (km/h)"),
    MS("m/s", "Metr/saniyə (m/s)", "Meters per second (m/s)"),
    MPH("mph", "Mil/saat (mph)", "Miles per hour (mph)")
}

enum class AppLanguage(val code: String, val label: String) {
    AZ("az", "Azərbaycan dili"),
    EN("en", "English")
}

data class UserSettings(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KMH,
    val language: AppLanguage = AppLanguage.AZ
) {
    fun formatTemp(celsius: Double): String {
        return when (temperatureUnit) {
            TemperatureUnit.CELSIUS -> "${Math.round(celsius)}°C"
            TemperatureUnit.FAHRENHEIT -> "${Math.round(celsius * 9 / 5 + 32)}°F"
        }
    }

    fun formatWind(kmh: Double): String {
        return when (windSpeedUnit) {
            WindSpeedUnit.KMH -> "${Math.round(kmh)} ${if (language == AppLanguage.AZ) "km/s" else "km/h"}"
            WindSpeedUnit.MS -> "${String.format("%.1f", kmh / 3.6)} m/s"
            WindSpeedUnit.MPH -> "${Math.round(kmh * 0.621371)} mph"
        }
    }
}
