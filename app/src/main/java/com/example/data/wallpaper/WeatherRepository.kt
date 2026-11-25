package com.example.data.wallpaper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class LiveWeatherSnapshot(
    val temperatureLabel: String,
    val conditionLabel: String,
    val isLive: Boolean
)

class WeatherRepository {

    suspend fun fetchCurrentWeather(): LiveWeatherSnapshot? = withContext(Dispatchers.IO) {
        runCatching {
            val tz = java.util.TimeZone.getDefault().id
            // Approximate city coordinates inferred from device timezone region, or default IST/UTC coords cleanly over HTTPS
            val (lat, lon) = coordinatesForTimeZone(tz)
            val endpoint = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code"
            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8_000
                readTimeout = 8_000
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val current = JSONObject(body).getJSONObject("current")
            val tempC = current.getDouble("temperature_2m")
            val code = current.getInt("weather_code")
            LiveWeatherSnapshot(
                temperatureLabel = String.format(Locale.US, "%.0f°C", tempC),
                conditionLabel = describeWmoCode(code),
                isLive = true
            )
        }.getOrNull()
    }

    private fun coordinatesForTimeZone(tzId: String): Pair<Double, Double> {
        val t = tzId.lowercase()
        return when {
            t.contains("kolkata") || t.contains("calcutta") || t.contains("india") -> 28.6139 to 77.2090
            t.contains("london") -> 51.5072 to -0.1276
            t.contains("new_york") -> 40.7128 to -74.0060
            t.contains("los_angeles") -> 34.0522 to -118.2437
            t.contains("tokyo") -> 35.6762 to 139.6503
            t.contains("dubai") -> 25.2048 to 55.2708
            else -> 28.6139 to 77.2090
        }
    }

    private fun describeWmoCode(code: Int): String = when (code) {
        0 -> "Clear Sky"
        1, 2 -> "Partly Cloudy"
        3 -> "Overcast"
        45, 48 -> "Foggy"
        51, 53, 55, 61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snow"
        80, 81, 82 -> "Showers"
        95, 96, 99 -> "Thunderstorm"
        else -> "Live Weather"
    }
}
