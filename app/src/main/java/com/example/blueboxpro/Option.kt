/**
 * Centralized configuration and constants for the BlueBoxPro application.
 * This object manages application-wide settings, sensor processing parameters,
 * and persistence of user preferences to a JSON configuration file.
 */
package com.example.blueboxpro

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Global options object.
 */
object Option {
    private const val FILE_NAME_CONFIG = "config.json"
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true 
    }

    /**
     * Internal data structure used for JSON serialization of configuration.
     */
    @Serializable
    private data class ConfigData(
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
        val roundingFactor: Float = 10f,
        val fileName: String = "sessiontrace.json",
        val distanceThresholdMeters: Double = 3.0,
        val recordingFrequencyHz: Float = 1.0f,
        val maxLogicalSpeedKmh: Double = 300.0,
        val notificationIntervalMs: Long = 10000L,
        // UI Preferences
        val isDarkMode: Boolean = false,
        val themePalette: String = "OCEAN",
        val language: String = "English",
        val unitSpeed: String = "km/h",
        val unitDistance: String = "km",
        val unitAltitude: String = "m",
        val unitAngle: String = "°",
        val windDensity: Int = 15,
        val windArrowSize: Float = 1.0f
    )

    /**
     * Application level constants.
     */
    object App {
        const val LANG_FR = "fr"
        const val LANG_EN = "en"
        const val LANG_NAME_FR = "Français"
        const val LANG_NAME_EN = "English"
        const val DEFAULT_UNIT_SYSTEM = "METRIC_KMH"
    }

    /**
     * Settings related to sensor data processing and filtering algorithms.
     */
    object Process {
        var GPS_TIMEOUT_MS = 5000L
        var MIN_GPS_ACCURACY = 50f
        var MAX_ACCEPTABLE_ACCURACY = 10f
        var AZIMUTH_ALPHA = 0.15f
        var SPEED_HISTORY_SIZE = 10
        var FULL_CIRCLE_DEGREES = 360f
        var HALF_CIRCLE_DEGREES = 180f
        
        var CALCULATION_FREQUENCY_HZ = 50f
        /** Time step between calculation cycles in seconds. */
        val FIXED_DT: Float get() = 1f / CALCULATION_FREQUENCY_HZ
        var LPF_ACCEL_ALPHA = 0.1f
        var Q_VEL = 0.001f
        var Q_BIAS = 0.0001f
        var R_BASE_GPS = 0.1f
        
        var DEAD_ZONE_SPEED = 0.3f
        var MEDIAN_WINDOW_SIZE = 5
        var ZUPT_SPEED_THRESHOLD = 1.0f
        var MAX_DT_BACKGROUND = 0.1f
        
        const val GPS_MIN_SPEED_FOR_COG = 0.5f
        const val R_MEASUREMENT_MIN_FACTOR = 1f
    }

    /**
     * Mathematical constants and factors for unit conversions.
     */
    object Movement {
        const val MS_TO_KMH = 3.6f
        const val MS_TO_MPH = 2.23694f
        const val MS_TO_KNOTS = 1.94384f
        const val METERS_TO_FEET = 3.28084
        const val METERS_TO_FEET_FLOAT = 3.28084f
        var ROUNDING_FACTOR = 10f
        
        const val EARTH_RADIUS_METERS = 6371000.0
        const val CIRCLE_HALF_RATIO = 2.0
    }

    /**
     * Configuration for data recording and file storage formats.
     */
    object Save {
        var FILE_NAME = "sessiontrace.json"
        var DISTANCE_THRESHOLD_METERS = 3.0
        var RECORDING_FREQUENCY_HZ = 1.0f
        var MAX_LOGICAL_SPEED_KMH = 300.0
        /** Interval between recording points in milliseconds. */
        val RECORDING_INTERVAL_MS: Long get() = (1000f / RECORDING_FREQUENCY_HZ).toLong()
        
        const val DATE_FORMAT = "dd/MM/yyyy"
        const val TIME_FORMAT = "HH:mm:ss"
        const val MILLIS_IN_HOUR = 3600000L
        const val MILLIS_IN_MINUTE = 60000L
        const val MILLIS_IN_SECOND = 1000L
        const val METERS_IN_KILOMETER = 1000.0
        const val DURATION_FORMAT = "%02d:%02d:%02d"
        const val KM_FORMAT = "%.2f km"
        const val M_FORMAT = "%.0f m"
        const val EXPORT_CSV_SPEED_FORMAT = "%.2f km/h"
    }

    /**
     * UI specific preferences.
     */
    object UI {
        var isDarkMode = false
        var themePalette = "OCEAN"
        var language = App.LANG_NAME_EN
        var unitSpeed = "km/h"
        var unitDistance = "km"
        var unitAltitude = "m"
        var unitAngle = "°"
        
        // Backward compatibility
        var unitSystem = App.DEFAULT_UNIT_SYSTEM
        
        var notificationIntervalMs = 10000L
        var windDensity = 15
        var windArrowSize = 1.0f
    }

    /**
     * Persists the current configuration state to a local JSON file.
     * @param context Android context for file access.
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
            recordingFrequencyHz = Save.RECORDING_FREQUENCY_HZ,
            maxLogicalSpeedKmh = Save.MAX_LOGICAL_SPEED_KMH,
            notificationIntervalMs = UI.notificationIntervalMs,
            isDarkMode = UI.isDarkMode,
            themePalette = UI.themePalette,
            language = UI.language,
            unitSpeed = UI.unitSpeed,
            unitDistance = UI.unitDistance,
            unitAltitude = UI.unitAltitude,
            unitAngle = UI.unitAngle,
            windDensity = UI.windDensity,
            windArrowSize = UI.windArrowSize
        )
        try {
            val jsonString = json.encodeToString(data)
            file.writeText(jsonString)
        } catch (e: Exception) {
            // Error ignored
        }
    }

    /**
     * Loads the configuration from the local JSON file if it exists.
     * @param context Android context for file access.
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
            Save.MAX_LOGICAL_SPEED_KMH = data.maxLogicalSpeedKmh

            UI.isDarkMode = data.isDarkMode
            UI.themePalette = data.themePalette
            UI.language = data.language
            UI.unitSpeed = data.unitSpeed
            UI.unitDistance = data.unitDistance
            UI.unitAltitude = data.unitAltitude
            UI.unitAngle = data.unitAngle
            UI.notificationIntervalMs = data.notificationIntervalMs
            UI.windDensity = data.windDensity
            UI.windArrowSize = data.windArrowSize
            
            // Sync unitSystem for legacy code
            UI.unitSystem = when (UI.unitSpeed) {
                "km/h" -> "METRIC_KMH"
                "m/s" -> "METRIC_MS"
                "mph" -> "IMPERIAL"
                "kn" -> "NAUTICAL"
                else -> "METRIC_KMH"
            }
        } catch (e: Exception) {
            // Defaults remain if loading fails
        }
    }
}
