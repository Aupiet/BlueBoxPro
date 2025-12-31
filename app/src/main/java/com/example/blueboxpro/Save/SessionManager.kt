package com.example.blueboxpro.Save

import android.content.Context
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
    val points: List<GpsPoint> = emptyList()
)

class Recording(val name: String) {
    private val _points = mutableListOf<GpsPoint>()
    val points: List<GpsPoint> get() = _points
    val startTime = System.currentTimeMillis()
    var lastPointTimestamp: Long = 0L // Pour gérer la cadence de 1Hz
    val startDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    fun addPoint(latitude: Double, longitude: Double, altitude: Double, sog: Float, cog: Float) {
        val now = System.currentTimeMillis()
        if (now - lastPointTimestamp >= 1000L) {
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

    fun stopAndSave(context: Context, distance: String) {
        val durationMillis = System.currentTimeMillis() - startTime
        val hours = durationMillis / 3600000
        val minutes = (durationMillis % 3600000) / 60000
        val seconds = (durationMillis % 60000) / 1000
        val durationStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        SessionManager.addSession(
            name = name,
            date = startDate,
            duration = durationStr,
            distance = distance,
            points = _points.toList()
        )
        SessionManager.saveSessions(context)
    }
}

object SessionManager {
    val sessions = mutableStateListOf<Session>()
    private const val FILE_NAME = "sessiontrace.json"
    
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true 
    }

    var activeRecording by mutableStateOf<Recording?>(null)
        private set

    fun startRecording(name: String) {
        activeRecording = Recording(name)
    }

    fun stopRecording(context: Context, distance: String = "0.0 km") {
        activeRecording?.stopAndSave(context, distance)
        activeRecording = null
    }

    fun updateRecording(processor: MovementProcessor) {
        activeRecording?.addPoint(
            latitude = processor.lastLocation?.latitude ?: 0.0,
            longitude = processor.lastLocation?.longitude ?: 0.0,
            altitude = processor.altitude,
            sog = processor.sog,
            cog = processor.cog
        )
    }

    fun addSession(name: String, date: String, duration: String, distance: String, points: List<GpsPoint> = emptyList()) {
        val newId = if (sessions.isEmpty()) 1 else sessions.maxOf { it.id } + 1
        sessions.add(Session(newId, name, date, duration, distance, points))
    }

    fun deleteSession(context: Context, session: Session) {
        sessions.remove(session)
        saveSessions(context)
    }

    fun clearSessions(context: Context) {
        sessions.clear()
        saveSessions(context)
    }

    fun loadSessions(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return

        try {
            val jsonString = file.readText()
            val loadedSessions = json.decodeFromString<List<Session>>(jsonString)
            sessions.clear()
            sessions.addAll(loadedSessions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveSessions(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        try {
            val jsonString = json.encodeToString(sessions.toList())
            file.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
