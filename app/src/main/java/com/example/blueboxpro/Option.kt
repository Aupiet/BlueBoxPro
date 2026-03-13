/**
 * Centralized configuration for the BlueBoxPro application.
 * This class handles settings storage in a JSON file to ensure persistence across updates.
 */
package com.example.blueboxpro

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object Option {
    private const val FILE_NAME_CONFIG = "config.json"
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true 
    }

    /**
     * Internal data structure for serialization.
     */
    @Serializable
    private data class ConfigData(
        // Process
        val gpsTimeoutMs: Long = 5000L,
        val minGpsAccuracy: Float = 50f,
        val maxAcceptableAccuracy: Float = 10f,
        val azimuthAlpha: Float = 0.15f,
        val speedHistorySize: Int = 10,
        val lpfAccelAlpha: Float = 0.1f,
        val qVel: Float = 0.001f,
        val qBias: Float = 0.0001f,
        val rBaseGps: Float = 0.1f,
        val deadZoneSpeed: Float = 0.3f,
        val medianWindowSize: Int = 5,
        // Movement
        val roundingFactor: Float = 10f,
        // Save
        val fileName: String = "sessiontrace.json",
        val distanceThresholdMeters: Double = 3.0,
        val recordingFrequencyHz: Float = 1.0f
    )

    object Process {
        var GPS_TIMEOUT_MS = 5000L
        var MIN_GPS_ACCURACY = 50f
        var MAX_ACCEPTABLE_ACCURACY = 10f
        var AZIMUTH_ALPHA = 0.15f
        var SPEED_HISTORY_SIZE = 10
        var FULL_CIRCLE_DEGREES = 360f
        var HALF_CIRCLE_DEGREES = 180f
        
        var CALCULATION_FREQUENCY_HZ = 50f
        val FIXED_DT: Float get() = 1f / CALCULATION_FREQUENCY_HZ
        var LPF_ACCEL_ALPHA = 0.1f
        var Q_VEL = 0.001f
        var Q_BIAS = 0.0001f
        var R_BASE_GPS = 0.1f
        
        var DEAD_ZONE_SPEED = 0.3f
        var MEDIAN_WINDOW_SIZE = 5
    }

    object Movement {
        const val MS_TO_KMH = 3.6f
        const val MS_TO_MPH = 2.23694f
        const val MS_TO_KNOTS = 1.94384f
        const val METERS_TO_FEET = 3.28084
        const val METERS_TO_FEET_FLOAT = 3.28084f
        var ROUNDING_FACTOR = 10f
    }

    object Save {
        var FILE_NAME = "sessiontrace.json"
        var DISTANCE_THRESHOLD_METERS = 3.0
        var RECORDING_FREQUENCY_HZ = 1.0f
        val RECORDING_INTERVAL_MS: Long get() = (1000f / RECORDING_FREQUENCY_HZ).toLong()
        
        const val DATE_FORMAT = "dd/MM/yyyy"
        const val MILLIS_IN_HOUR = 3600000L
        const val MILLIS_IN_MINUTE = 60000L
        const val MILLIS_IN_SECOND = 1000L
        const val METERS_IN_KILOMETER = 1000.0
        const val DURATION_FORMAT = "%02d:%02d:%02d"
        const val KM_FORMAT = "%.2f km"
        const val M_FORMAT = "%.0f m"
    }

    object App {
        const val DEFAULT_UNIT_SYSTEM = "METRIC_KMH"
        const val LANG_FR = "fr"
        const val LANG_EN = "en"
        const val LANG_NAME_FR = "Français"
    }

    /**
     * Saves options to a text file (JSON).
     */
    fun save(context: Context) {
        val file = File(context.filesDir, FILE_NAME_CONFIG)
        val data = ConfigData(
            gpsTimeoutMs = Process.GPS_TIMEOUT_MS,
            minGpsAccuracy = Process.MIN_GPS_ACCURACY,
            maxAcceptableAccuracy = Process.MAX_ACCEPTABLE_ACCURACY,
            azimuthAlpha = Process.AZIMUTH_ALPHA,
            speedHistorySize = Process.SPEED_HISTORY_SIZE,
            lpfAccelAlpha = Process.LPF_ACCEL_ALPHA,
            qVel = Process.Q_VEL,
            qBias = Process.Q_BIAS,
            rBaseGps = Process.R_BASE_GPS,
            deadZoneSpeed = Process.DEAD_ZONE_SPEED,
            medianWindowSize = Process.MEDIAN_WINDOW_SIZE,
            roundingFactor = Movement.ROUNDING_FACTOR,
            fileName = Save.FILE_NAME,
            distanceThresholdMeters = Save.DISTANCE_THRESHOLD_METERS,
            recordingFrequencyHz = Save.RECORDING_FREQUENCY_HZ
        )
        try {
            val jsonString = json.encodeToString(data)
            file.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Loads options from the text file (JSON).
     */
    fun load(context: Context) {
        val file = File(context.filesDir, FILE_NAME_CONFIG)
        if (!file.exists()) return

        try {
            val jsonString = file.readText()
            val data = json.decodeFromString<ConfigData>(jsonString)
            
            Process.GPS_TIMEOUT_MS = data.gpsTimeoutMs
            Process.MIN_GPS_ACCURACY = data.minGpsAccuracy
            Process.MAX_ACCEPTABLE_ACCURACY = data.maxAcceptableAccuracy
            Process.AZIMUTH_ALPHA = data.azimuthAlpha
            Process.SPEED_HISTORY_SIZE = data.speedHistorySize
            Process.LPF_ACCEL_ALPHA = data.lpfAccelAlpha
            Process.Q_VEL = data.qVel
            Process.Q_BIAS = data.qBias
            Process.R_BASE_GPS = data.rBaseGps
            Process.DEAD_ZONE_SPEED = data.deadZoneSpeed
            Process.MEDIAN_WINDOW_SIZE = data.medianWindowSize
            
            Movement.ROUNDING_FACTOR = data.roundingFactor
            
            Save.FILE_NAME = data.fileName
            Save.DISTANCE_THRESHOLD_METERS = data.distanceThresholdMeters
            Save.RECORDING_FREQUENCY_HZ = data.recordingFrequencyHz
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
