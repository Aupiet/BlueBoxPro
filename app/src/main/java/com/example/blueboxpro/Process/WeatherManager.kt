package com.example.blueboxpro.Process

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL

/**
 * Weather data structure for Open-Meteo API.
 */
@Serializable
data class WeatherData(
    @SerialName("current_weather") val currentWeather: CurrentWeather
)

@Serializable
data class CurrentWeather(
    val temperature: Double,
    @SerialName("weathercode") val weatherCode: Int,
    @SerialName("windspeed") val windSpeed: Double,
    @SerialName("winddirection") val windDirection: Double,
    val time: String
)

/**
 * Utility to fetch weather data from Open-Meteo (No API Key required).
 */
object WeatherManager {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches current weather for the given coordinates.
     */
    suspend fun fetchWeather(lat: Double, lon: Double): WeatherData? = withContext(Dispatchers.IO) {
        try {
            // Open-Meteo endpoint: Free, no key, no registration
            val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true"
            val response = URL(url).readText()
            json.decodeFromString<WeatherData>(response)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Translates WMO Weather interpretation codes to human readable strings.
     */
    fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "Ciel dégagé"
            1, 2, 3 -> "Partiellement nuageux"
            45, 48 -> "Brouillard"
            51, 53, 55 -> "Bruine"
            61, 63, 65 -> "Pluie"
            71, 73, 75 -> "Neige"
            80, 81, 82 -> "Averses de pluie"
            95, 96, 99 -> "Orage"
            else -> "Inconnu"
        }
    }

    /**
     * Converts wind direction degrees to a cardinal string.
     */
    fun getWindDirectionCardinal(degrees: Double): String {
        val directions = arrayOf("N", "N-E", "E", "S-E", "S", "S-O", "O", "N-O", "N")
        return directions[((degrees % 360) / 45).toInt()]
    }
}
