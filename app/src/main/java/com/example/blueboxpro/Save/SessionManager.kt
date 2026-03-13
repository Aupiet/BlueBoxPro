/**
 * This file handles session management, including recording GPS points,
 * calculating session statistics (duration, distance), and persisting sessions to local storage.
 */
package com.example.blueboxpro.Save

import android.content.Context
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.blueboxpro.Process.MovementProcessor
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log

@Serializable
data class GpsPoint(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val sog: Float, // Speed Over Ground
    val cog: Float, // Course Over Ground
    val timestamp: Long
)

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

class Recording(val name: String) {
    private val _points = mutableListOf<GpsPoint>()
    val points: List<GpsPoint> get() = _points
    val startTime = System.currentTimeMillis()
    var lastPointTimestamp: Long = 0L
    val startDate: String = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())

    companion object {
        private const val DATE_FORMAT = "dd/MM/yyyy"
        private const val RECORDING_INTERVAL_MS = 1000L
        private const val MILLIS_IN_HOUR = 3600000L
        private const val MILLIS_IN_MINUTE = 60000L
        private const val MILLIS_IN_SECOND = 1000L
        private const val METERS_IN_KILOMETER = 1000.0
        private const val DURATION_FORMAT = "%02d:%02d:%02d"
        private const val KM_FORMAT = "%.2f km"
        private const val M_FORMAT = "%.0f m"
    }

    /**
     * Adds a GPS point to the current recording if the interval threshold is met.
     */
    fun addPoint(latitude: Double, longitude: Double, altitude: Double, sog: Float, cog: Float) {
        val now = System.currentTimeMillis()
        if (now - lastPointTimestamp >= RECORDING_INTERVAL_MS) {
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
     * Finalizes the recording, calculates statistics, and saves the session.
     */
    fun stopAndSave(context: Context) {
        val durationMillis = System.currentTimeMillis() - startTime
        val hours = durationMillis / MILLIS_IN_HOUR
        val minutes = (durationMillis % MILLIS_IN_HOUR) / MILLIS_IN_MINUTE
        val seconds = (durationMillis % MILLIS_IN_MINUTE) / MILLIS_IN_SECOND
        val durationStr = String.format(DURATION_FORMAT, hours, minutes, seconds)

        val totalDist = SessionManager.calculateDistance(_points)
        val finalDistanceStr = if (totalDist >= METERS_IN_KILOMETER) {
            String.format(Locale.getDefault(), KM_FORMAT, totalDist / METERS_IN_KILOMETER)
        } else {
            String.format(Locale.getDefault(), M_FORMAT, totalDist)
        }
        val durationSeconds = durationMillis / 1000.0
        val averageSpeed = if (durationSeconds > 0) {
            (totalDist / durationSeconds) * 3.6 // conversion m/s -> km/h
        } else 0.0
        val averageSpeedStr = String.format(Locale.getDefault(), "%.2f km/h", averageSpeed)
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

object SessionManager {
    val sessions = mutableStateListOf<Session>()
    private const val FILE_NAME = "sessiontrace.json"
    private const val DISTANCE_THRESHOLD_METERS = 3.0
    
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true 
    }

    var activeRecording by mutableStateOf<Recording?>(null)
        private set

    /**
     * Initializes a new recording session.
     */
    fun startRecording(name: String) {
        activeRecording = Recording(name)
    }

    /**
     * Stops and saves the active recording session.
     */
    fun stopRecording(context: Context) {
        activeRecording?.stopAndSave(context)
        activeRecording = null
    }

    /**
     * Calculates the total distance covered by a list of GPS points.
     */
    fun calculateDistance(points: List<GpsPoint>): Double {
        if (points.size < 2) return 0.0
        var totalDistance = 0.0
        var lastValidPoint = points[0]
        val results = FloatArray(1)

        for (i in 1 until points.size) {
            val currentPoint = points[i]
            Location.distanceBetween(
                lastValidPoint.latitude, lastValidPoint.longitude,
                currentPoint.latitude, currentPoint.longitude,
                results
            )
            val distance = results[0].toDouble()

            if (distance >= DISTANCE_THRESHOLD_METERS) {
                totalDistance += distance
                lastValidPoint = currentPoint
            }
        }
        return totalDistance
    }

    /**
     * Adds a GPS point to the current recording based on the movement processor data.
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
     * Adds a new session to the list of saved sessions.
     */
    fun addSession(name: String, date: String, duration: String, distance: String, averageSpeed :String, points: List<GpsPoint> = emptyList()) {
        val newId = if (sessions.isEmpty()) 1 else sessions.maxOf { it.id } + 1
        sessions.add(Session(newId, name, date, duration, distance,  averageSpeed, points))
    }

    /**
     * Deletes a specific session and updates storage.
     */
    fun deleteSession(context: Context, session: Session) {
        sessions.remove(session)
        saveSessions(context)
    }

    /**
     * Clears all saved sessions.
     */
    fun clearSessions(context: Context) {
        sessions.clear()
        saveSessions(context)
    }

    /**
     * Loads saved sessions from local storage.
     */
    fun loadSessions(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return

        try {
            val jsonString = file.readText()
            Log.d("CSV Content", jsonString) // verifie contenu correcte
            val loadedSessions = json.decodeFromString<List<Session>>(jsonString)
            sessions.clear()
            sessions.addAll(loadedSessions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Persists the current list of sessions to local storage.
     */
    fun saveSessions(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        try {
            val jsonString = json.encodeToString(sessions.toList())
            file.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun exportSessionToCsv(context: Context, session: Session): File {
        val fileName = "session_${session.id}.csv"
        val file = File(context.cacheDir, fileName)

        val sb = StringBuilder()
        Log.d("CSV", "Path: ${file.absolutePath}") // verifie que le fichier est créé ds le cache
        // En-tête session
        sb.appendLine("Session name,Date,Duration,Distance,Average speed")
        sb.appendLine("${session.name},${session.date},${session.duration},${session.distance},${session.averageSpeed}")
        sb.appendLine()

        // En-tête points GPS
        sb.appendLine("id,latitude,longitude,altitude,sog,cog,timestamp")

        session.points.forEach { p ->
            sb.appendLine(
                "${p.id},${p.latitude},${p.longitude},${p.altitude},${p.sog},${p.cog},${p.timestamp}"
            )
        }

        file.writeText(sb.toString())
        return file
    }
    fun shareFile(context: Context, file: File) {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            android.content.Intent.createChooser(intent, "Exporter la session")
        )
    }

}
