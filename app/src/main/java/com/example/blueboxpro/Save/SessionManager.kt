/**
 * This file manages session data persistence, recording logic, and file exports.
 * It provides a centralized manager for saving, loading, and sharing historical GPS sessions.
 */
package com.example.blueboxpro.Save

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import com.example.blueboxpro.Option
import com.example.blueboxpro.Process.MovementProcessor
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class representing a single recorded GPS point.
 */
@Serializable
data class GpsPoint(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val sog: Float,
    val cog: Float,
    val timestamp: Long
)

/**
 * Data class representing a completed tracking session.
 */
@Serializable
data class Session(
    val id: Int,
    val name: String,
    val date: String,
    val duration: String,
    val distance: String,
    val averageSpeed: String,
    val points: List<GpsPoint> = emptyList()
)

/**
 * Handles the state and logic of an ongoing recording.
 * 
 * @property name The name assigned to this recording session.
 */
class Recording(val name: String) {
    private val _points = mutableListOf<GpsPoint>()
    val points: List<GpsPoint> get() = _points
    val startTime = System.currentTimeMillis()
    private var lastPointTimestamp: Long = 0L
    val startDate: String = SimpleDateFormat(Option.Save.DATE_FORMAT, Locale.US).format(Date())

    /**
     * Captures a new data point if the recording interval has elapsed.
     */
    fun addPoint(latitude: Double, longitude: Double, altitude: Double, sog: Float, cog: Float) {
        val now = System.currentTimeMillis()
        if (now - lastPointTimestamp >= Option.Save.RECORDING_INTERVAL_MS) {
            val newId = _points.size + 1
            _points.add(
                GpsPoint(
                    id = newId,
                    latitude = latitude,
                    longitude = longitude,
                    altitude = altitude,
                    sog = sog,
                    cog = cog,
                    timestamp = now
                )
            )
            lastPointTimestamp = now
        }
    }

    /**
     * Stops the recording, calculates statistics, and adds it to the SessionManager.
     * 
     * @param context Android context for saving to storage.
     */
    fun stopAndSave(context: Context) {
        val durationMillis = System.currentTimeMillis() - startTime
        val hours = durationMillis / Option.Save.MILLIS_IN_HOUR
        val minutes = (durationMillis % Option.Save.MILLIS_IN_HOUR) / Option.Save.MILLIS_IN_MINUTE
        val seconds = (durationMillis % Option.Save.MILLIS_IN_MINUTE) / Option.Save.MILLIS_IN_SECOND
        val durationStr = String.format(Locale.US, Option.Save.DURATION_FORMAT, hours, minutes, seconds)

        val totalDist = SessionManager.calculateDistance(_points)
        val finalDistanceStr = if (totalDist >= Option.Save.METERS_IN_KILOMETER) {
            String.format(Locale.US, Option.Save.KM_FORMAT, totalDist / Option.Save.METERS_IN_KILOMETER)
        } else {
            String.format(Locale.US, Option.Save.M_FORMAT, totalDist)
        }
        
        val durationSeconds = durationMillis / 1000.0
        val avgSpeedKmh = if (durationSeconds > 0) {
            (totalDist / durationSeconds) * Option.Movement.MS_TO_KMH
        } else 0.0
        val averageSpeedStr = String.format(Locale.US, "%.2f km/h", avgSpeedKmh)
        
        SessionManager.addSession(
            name = name,
            date = startDate,
            duration = durationStr,
            distance = finalDistanceStr,
            averageSpeed = averageSpeedStr,
            points = _points.toList()
        )
        SessionManager.saveSessions(context)
    }
}

/**
 * Singleton object managing session lifecycle, persistence, and exports.
 */
object SessionManager {
    val sessions = mutableStateListOf<Session>()
    
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true 
    }

    var activeRecording by mutableStateOf<Recording?>(null)
        private set

    /**
     * Starts a new session recording.
     * 
     * @param name Name for the new session.
     */
    fun startRecording(name: String) {
        activeRecording = Recording(name)
    }

    /**
     * Stops the current recording and clears the active state.
     * 
     * @param context Context required for saving to disk.
     */
    fun stopRecording(context: Context) {
        activeRecording?.stopAndSave(context)
        activeRecording = null
    }

    /**
     * Calculates total distance using the Haversine formula, filtering out jitter.
     * 
     * @param points List of GPS points.
     * @return Total distance in meters.
     */
    fun calculateDistance(points: List<GpsPoint>): Double {
        if (points.size < 2) return 0.0
        var totalDistance = 0.0
        var lastValidPoint = points[0]

        for (i in 1 until points.size) {
            val currentPoint = points[i]
            val distance = haversine(
                lastValidPoint.latitude, lastValidPoint.longitude,
                currentPoint.latitude, currentPoint.longitude
            )

            if (distance >= Option.Save.DISTANCE_THRESHOLD_METERS) {
                totalDistance += distance
                lastValidPoint = currentPoint
            }
        }
        return totalDistance
    }

    /**
     * Internal helper for great-circle distance calculation.
     */
    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return r * c
    }

    /**
     * Updates the active recording with the latest processor state.
     */
    fun updateRecording(processor: MovementProcessor) {
        activeRecording?.addPoint(
            latitude = processor.lastLocation?.latitude ?: 0.0,
            longitude = processor.lastLocation?.longitude ?: 0.0,
            altitude = processor.altitude,
            sog = processor.sog,
            cog = processor.cog
        )
    }

    /**
     * Internal method to add a session to the memory list and assign an ID.
     */
    fun addSession(name: String, date: String, duration: String, distance: String, averageSpeed: String, points: List<GpsPoint> = emptyList()) {
        val newId = if (sessions.isEmpty()) 1 else sessions.maxOf { it.id } + 1
        sessions.add(Session(newId, name, date, duration, distance, averageSpeed, points))
    }

    /**
     * Removes a session from memory and updates storage.
     */
    fun deleteSession(context: Context, session: Session) {
        sessions.remove(session)
        saveSessions(context)
    }

    /**
     * Loads the sessions list from the local JSON file.
     */
    fun loadSessions(context: Context) {
        val file = File(context.filesDir, Option.Save.FILE_NAME)
        if (!file.exists()) return

        try {
            val jsonString = file.readText()
            val loadedSessions = json.decodeFromString<List<Session>>(jsonString)
            sessions.clear()
            sessions.addAll(loadedSessions)
        } catch (e: Exception) {
            // Log or handle error
        }
    }

    /**
     * Saves the current sessions list to a local JSON file.
     */
    fun saveSessions(context: Context) {
        val file = File(context.filesDir, Option.Save.FILE_NAME)
        try {
            val jsonString = json.encodeToString(sessions.toList())
            file.writeText(jsonString)
        } catch (e: Exception) {
            // Log or handle error
        }
    }

    /**
     * Exports a session to a CSV file in the cache directory.
     * 
     * @return The generated File.
     */
    fun exportSessionToCsv(context: Context, session: Session): File {
        val fileName = "session_${session.id}.csv"
        val file = File(context.cacheDir, fileName)

        val sb = StringBuilder()
        sb.appendLine("Session Name,Date,Duration,Distance,Average Speed")
        sb.appendLine("${session.name},${session.date},${session.duration},${session.distance},${session.averageSpeed}")
        sb.appendLine()
        sb.appendLine("id,latitude,longitude,altitude,sog,cog,timestamp")

        session.points.forEach { p ->
            sb.appendLine("${p.id},${p.latitude},${p.longitude},${p.altitude},${p.sog},${p.cog},${p.timestamp}")
        }

        file.writeText(sb.toString())
        return file
    }

    /**
     * Triggers a system share sheet for the provided file.
     */
    fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Session Export"))
    }
}
