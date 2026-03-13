/**
 * Centralized configuration for the BlueBoxPro application.
 * This class holds variable values that can be modified via Advanced Settings.
 */
package com.example.blueboxpro

import android.content.Context
import android.content.SharedPreferences

object Option {
    private const val PREFS_NAME = "bluebox_options"

    /**
     * Constants related to the Movement Processor and sensor data filtering.
     */
    object Process {
        var GPS_TIMEOUT_MS = 5000L
        var MIN_GPS_ACCURACY = 50f
        var MAX_ACCEPTABLE_ACCURACY = 10f
        var AZIMUTH_ALPHA = 0.15f
        var SPEED_HISTORY_SIZE = 10
        var FULL_CIRCLE_DEGREES = 360f
        var HALF_CIRCLE_DEGREES = 180f
        
        // EKF & Filter Constants
        var CALCULATION_FREQUENCY_HZ = 50f
        val FIXED_DT: Float get() = 1f / CALCULATION_FREQUENCY_HZ
        var LPF_ACCEL_ALPHA = 0.1f
        var Q_VEL = 0.001f
        var Q_BIAS = 0.0001f
        var R_BASE_GPS = 0.1f
        
        // Advanced Smoothing
        var DEAD_ZONE_SPEED = 0.3f // m/s
        var MEDIAN_WINDOW_SIZE = 5
    }

    /**
     * Constants related to unit conversions and speed calculations.
     */
    object Movement {
        const val MS_TO_KMH = 3.6f
        const val MS_TO_MPH = 2.23694f
        const val MS_TO_KNOTS = 1.94384f
        const val METERS_TO_FEET = 3.28084
        const val METERS_TO_FEET_FLOAT = 3.28084f
        var ROUNDING_FACTOR = 10f
    }

    /**
     * Constants related to session management and data persistence.
     */
    object Save {
        var FILE_NAME = "sessiontrace.json"
        var DISTANCE_THRESHOLD_METERS = 3.0
        var RECORDING_FREQUENCY_HZ = 1.0f // Points par seconde
        val RECORDING_INTERVAL_MS: Long get() = (1000f / RECORDING_FREQUENCY_HZ).toLong()
        
        const val DATE_FORMAT = "dd/MM/yyyy"
        
        // Time Conversion Constants
        const val MILLIS_IN_HOUR = 3600000L
        const val MILLIS_IN_MINUTE = 60000L
        const val MILLIS_IN_SECOND = 1000L
        const val METERS_IN_KILOMETER = 1000.0
        
        // Formatting Constants
        const val DURATION_FORMAT = "%02d:%02d:%02d"
        const val KM_FORMAT = "%.2f km"
        const val M_FORMAT = "%.0f m"
    }

    /**
     * General application settings and UI defaults.
     */
    object App {
        const val DEFAULT_UNIT_SYSTEM = "METRIC_KMH"
        const val LANG_FR = "fr"
        const val LANG_EN = "en"
        const val LANG_NAME_FR = "Français"
    }

    /**
     * Saves the current options to SharedPreferences.
     */
    fun save(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putLong("GPS_TIMEOUT_MS", Process.GPS_TIMEOUT_MS)
            putFloat("MIN_GPS_ACCURACY", Process.MIN_GPS_ACCURACY)
            putFloat("MAX_ACCEPTABLE_ACCURACY", Process.MAX_ACCEPTABLE_ACCURACY)
            putFloat("AZIMUTH_ALPHA", Process.AZIMUTH_ALPHA)
            putInt("SPEED_HISTORY_SIZE", Process.SPEED_HISTORY_SIZE)
            putFloat("LPF_ACCEL_ALPHA", Process.LPF_ACCEL_ALPHA)
            putFloat("Q_VEL", Process.Q_VEL)
            putFloat("Q_BIAS", Process.Q_BIAS)
            putFloat("R_BASE_GPS", Process.R_BASE_GPS)
            putFloat("DEAD_ZONE_SPEED", Process.DEAD_ZONE_SPEED)
            putInt("MEDIAN_WINDOW_SIZE", Process.MEDIAN_WINDOW_SIZE)
            putFloat("ROUNDING_FACTOR", Movement.ROUNDING_FACTOR)
            putString("FILE_NAME", Save.FILE_NAME)
            putFloat("DISTANCE_THRESHOLD_METERS", Save.DISTANCE_THRESHOLD_METERS.toFloat())
            putFloat("RECORDING_FREQUENCY_HZ", Save.RECORDING_FREQUENCY_HZ)
            apply()
        }
    }

    /**
     * Loads the options from SharedPreferences.
     */
    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        Process.GPS_TIMEOUT_MS = prefs.getLong("GPS_TIMEOUT_MS", 5000L)
        Process.MIN_GPS_ACCURACY = prefs.getFloat("MIN_GPS_ACCURACY", 50f)
        Process.MAX_ACCEPTABLE_ACCURACY = prefs.getFloat("MAX_ACCEPTABLE_ACCURACY", 10f)
        Process.AZIMUTH_ALPHA = prefs.getFloat("AZIMUTH_ALPHA", 0.15f)
        Process.SPEED_HISTORY_SIZE = prefs.getInt("SPEED_HISTORY_SIZE", 10)
        Process.LPF_ACCEL_ALPHA = prefs.getFloat("LPF_ACCEL_ALPHA", 0.1f)
        Process.Q_VEL = prefs.getFloat("Q_VEL", 0.001f)
        Process.Q_BIAS = prefs.getFloat("Q_BIAS", 0.0001f)
        Process.R_BASE_GPS = prefs.getFloat("R_BASE_GPS", 0.1f)
        Process.DEAD_ZONE_SPEED = prefs.getFloat("DEAD_ZONE_SPEED", 0.3f)
        Process.MEDIAN_WINDOW_SIZE = prefs.getInt("MEDIAN_WINDOW_SIZE", 5)
        Movement.ROUNDING_FACTOR = prefs.getFloat("ROUNDING_FACTOR", 10f)
        Save.FILE_NAME = prefs.getString("FILE_NAME", "sessiontrace.json") ?: "sessiontrace.json"
        Save.DISTANCE_THRESHOLD_METERS = prefs.getFloat("DISTANCE_THRESHOLD_METERS", 3.0f).toDouble()
        Save.RECORDING_FREQUENCY_HZ = prefs.getFloat("RECORDING_FREQUENCY_HZ", 1.0f)
    }
}
