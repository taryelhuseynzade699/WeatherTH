package com.example

import com.example.data.model.AppLanguage
import com.example.data.model.TemperatureUnit
import com.example.data.model.UserSettings
import com.example.data.model.WeatherCategory
import com.example.data.model.WeatherConditionHelper
import com.example.data.model.WindSpeedUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherUnitTest {

    @Test
    fun testWeatherConditionMapping() {
        val clearDay = WeatherConditionHelper.getCondition(0, isDay = true)
        assertEquals(WeatherCategory.CLEAR, clearDay.category)
        assertEquals("Açıq hava", clearDay.titleAz)
        assertEquals("Clear Sky", clearDay.titleEn)

        val rain = WeatherConditionHelper.getCondition(65, isDay = true)
        assertEquals(WeatherCategory.RAIN, rain.category)
        assertEquals("Güclü yağış", rain.titleAz)

        val thunder = WeatherConditionHelper.getCondition(95, isDay = true)
        assertEquals(WeatherCategory.THUNDERSTORM, thunder.category)
    }

    @Test
    fun testUserSettingsFormatting() {
        val settingsC = UserSettings(temperatureUnit = TemperatureUnit.CELSIUS, windSpeedUnit = WindSpeedUnit.KMH)
        assertEquals("22°C", settingsC.formatTemp(22.4))
        assertEquals("18 km/s", settingsC.formatWind(18.0))

        val settingsF = UserSettings(temperatureUnit = TemperatureUnit.FAHRENHEIT, windSpeedUnit = WindSpeedUnit.MPH, language = AppLanguage.EN)
        assertEquals("72°F", settingsF.formatTemp(22.0))
        assertEquals("11 mph", settingsF.formatWind(18.0))
    }
}
