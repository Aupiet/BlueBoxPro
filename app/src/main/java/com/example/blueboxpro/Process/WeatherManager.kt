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

    /**
     * Metadata describing a tile overlay layer.
     */
    data class LayerInfo(
        val id: String,
        val name: String,
        val baseUrls: Array<String>,
        val minZoom: Int,
        val maxZoom: Int,
        val tileSize: Int = 256,
        val extension: String = ".png"
    )

    /** Static topo layer info. */
    val topoLayerInfo = LayerInfo(
        id = "topo",
        name = "OpenTopoMap",
        baseUrls = arrayOf(
            "https://a.tile.opentopomap.org/",
            "https://b.tile.opentopomap.org/",
            "https://c.tile.opentopomap.org/"
        ),
        minZoom = 0,
        maxZoom = 17
    )

    /**
     * Builds a precipitation LayerInfo using a pre-fetched tile base URL.
     */
    fun buildPrecipLayerInfo(tileBaseUrl: String): LayerInfo = LayerInfo(
        id = "precip",
        name = "RainViewerPrecip",
        baseUrls = arrayOf(tileBaseUrl),
        minZoom = 0,
        maxZoom = 12,
        extension = "/2/1_1.png"
    )

    /**
     * Fetches the latest radar tile base URL from RainViewer's public API.
     * Returns the full base URL: {host}{path}/{size}/
     * Returns null on failure.
     */
    suspend fun fetchRainViewerTileBase(): String? = withContext(Dispatchers.IO) {
        try {
            val response = URL("https://api.rainviewer.com/public/weather-maps.json").readText()
            val obj = org.json.JSONObject(response)
            val host = obj.getString("host")
            val radar = obj.getJSONObject("radar")
            val past = radar.getJSONArray("past")
            val latest = past.getJSONObject(past.length() - 1)
            val path = latest.getString("path")
            // Final URL: {host}{path}/256/  →  e.g. https://tilecache.rainviewer.com/v2/radar/1609402200/256/
            "$host$path/256/"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    /**
     * Fetches a grid of wind vectors for the given bounding box.
     */
    suspend fun fetchWindGrid(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double, cols: Int = 5, rows: Int = 6): List<com.example.blueboxpro.ui.components.WindVector> = withContext(Dispatchers.IO) {
        try {
            val lats = mutableListOf<Double>()
            val lons = mutableListOf<Double>()
            val dLat = (maxLat - minLat) / (rows - 1).coerceAtLeast(1)
            val dLon = (maxLon - minLon) / (cols - 1).coerceAtLeast(1)

            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    lats.add(minLat + r * dLat)
                    lons.add(minLon + c * dLon)
                }
            }

            val chunkLimit = 50
            val latChunks = lats.chunked(chunkLimit)
            val lonChunks = lons.chunked(chunkLimit)

            val result = mutableListOf<com.example.blueboxpro.ui.components.WindVector>()

            for (i in latChunks.indices) {
                try {
                    val latStr = latChunks[i].joinToString(",")
                    val lonStr = lonChunks[i].joinToString(",")

                    val url = "https://api.open-meteo.com/v1/forecast?latitude=$latStr&longitude=$lonStr&current_weather=true&wind_speed_unit=kn"
                    val response = URL(url).readText()

                    val responseStr = response.trim()
                    if (responseStr.startsWith("[")) {
                        val jsonArray = org.json.JSONArray(responseStr)
                        for (j in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(j)
                            val lat = obj.optDouble("latitude", 0.0)
                            val lon = obj.optDouble("longitude", 0.0)
                            val current = obj.optJSONObject("current_weather")
                            if (current != null) {
                                val speed = current.optDouble("windspeed", 0.0).toFloat()
                                val dir = current.optDouble("winddirection", 0.0).toFloat()
                                result.add(com.example.blueboxpro.ui.components.WindVector(lat, lon, speed, dir))
                            }
                        }
                    } else if (responseStr.startsWith("{")) {
                        val obj = org.json.JSONObject(responseStr)
                        // If there's an error field, Open-Meteo returns { "error": true, "reason": "..." }
                        if (obj.optBoolean("error", false)) continue
                        
                        val lat = obj.optDouble("latitude", 0.0)
                        val lon = obj.optDouble("longitude", 0.0)
                        val current = obj.optJSONObject("current_weather")
                        if (current != null) {
                            val speed = current.optDouble("windspeed", 0.0).toFloat()
                            val dir = current.optDouble("winddirection", 0.0).toFloat()
                            result.add(com.example.blueboxpro.ui.components.WindVector(lat, lon, speed, dir))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Spatially interpolates a sparse list of wind vectors into a denser grid using Inverse Distance Weighting (IDW).
     * This creates a rich visual map without hammering the API.
     */
    fun interpolateWindGrid(baseVectors: List<com.example.blueboxpro.ui.components.WindVector>, minLat: Double, maxLat: Double, minLon: Double, maxLon: Double, targetCols: Int, targetRows: Int): List<com.example.blueboxpro.ui.components.WindVector> {
        if (baseVectors.isEmpty()) return emptyList()
        
        val result = mutableListOf<com.example.blueboxpro.ui.components.WindVector>()
        val dLat = (maxLat - minLat) / (targetRows - 1).coerceAtLeast(1)
        val dLon = (maxLon - minLon) / (targetCols - 1).coerceAtLeast(1)

        for (r in 0 until targetRows) {
            for (c in 0 until targetCols) {
                val lat = minLat + r * dLat
                val lon = minLon + c * dLon
                
                var sumWeight = 0.0
                var sumSpeed = 0.0
                var sumU = 0.0
                var sumV = 0.0
                var exactMatch = false
                
                for (v in baseVectors) {
                    val distSq = (v.lat - lat) * (v.lat - lat) + (v.lon - lon) * (v.lon - lon)
                    if (distSq < 1e-10) {
                        result.add(com.example.blueboxpro.ui.components.WindVector(lat, lon, v.speed, v.direction))
                        exactMatch = true
                        break
                    }
                    
                    val weight = 1.0 / distSq
                    sumWeight += weight
                    sumSpeed += v.speed * weight
                    
                    val rad = Math.toRadians(v.direction.toDouble())
                    val u = kotlin.math.sin(rad) * v.speed
                    val v_comp = kotlin.math.cos(rad) * v.speed
                    
                    sumU += u * weight
                    sumV += v_comp * weight
                }
                
                if (!exactMatch) {
                    val interpolatedSpeed = (sumSpeed / sumWeight).toFloat()
                    val interU = sumU / sumWeight
                    val interV = sumV / sumWeight
                    
                    var interpolatedDir = Math.toDegrees(kotlin.math.atan2(interU, interV)).toFloat()
                    if (interpolatedDir < 0) interpolatedDir += 360f
                    
                    result.add(com.example.blueboxpro.ui.components.WindVector(lat, lon, interpolatedSpeed, interpolatedDir))
                }
            }
        }
        return result
    }
}
