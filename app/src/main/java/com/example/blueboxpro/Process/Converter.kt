/**
 * Utility object for data conversion across different unit systems and formats.
 * Centralizes logic for speed, altitude, and date formatting.
 */
package com.example.blueboxpro.Process

import com.example.blueboxpro.Option
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Provides methods to convert raw sensor data into user-friendly formats.
 */
object Converter {

    /**
     * Converts speed from meters per second to the target unit system.
     * 
     * @param speedMs Speed in m/s.
     * @param unitSystem The target unit system.
     * @return Converted speed value.
     */
    fun convertSpeed(speedMs: Float, unitSystem: UnitSystem): Float {
        return when (unitSystem) {
            UnitSystem.METRIC_KMH -> speedMs * Option.Movement.MS_TO_KMH
            UnitSystem.METRIC_MS -> speedMs
            UnitSystem.IMPERIAL -> speedMs * Option.Movement.MS_TO_MPH
            UnitSystem.NAUTICAL -> speedMs * Option.Movement.MS_TO_KNOTS
        }
    }

    /**
     * Converts speed from meters per second to the target unit system using a string key.
     * 
     * @param speedMs Speed in m/s.
     * @param unitSystemStr The unit system key (e.g., "METRIC_KMH").
     * @return Converted speed value.
     */
    fun convertSpeed(speedMs: Float, unitSystemStr: String): Float {
        return convertSpeed(speedMs, getUnitSystem(unitSystemStr))
    }

    /**
     * Converts altitude from meters to the target unit system.
     * 
     * @param altitudeMeters Altitude in meters.
     * @param unitSystem The target unit system.
     * @return Converted altitude value.
     */
    fun convertAlt(altitudeMeters: Double, unitSystem: UnitSystem): Double {
        return when (unitSystem) {
            UnitSystem.IMPERIAL -> altitudeMeters * Option.Movement.METERS_TO_FEET
            else -> altitudeMeters
        }
    }

    /**
     * Converts altitude from meters to the target unit system using a string key.
     * 
     * @param altitudeMeters Altitude in meters.
     * @param unitSystemStr The unit system key.
     * @return Converted altitude value.
     */
    fun convertAlt(altitudeMeters: Double, unitSystemStr: String): Double {
        return convertAlt(altitudeMeters, getUnitSystem(unitSystemStr))
    }

    /**
     * Formats a timestamp into a readable date string.
     * 
     * @param timestamp Epoch time in milliseconds.
     * @param pattern Date pattern (e.g., "HH:mm:ss").
     * @return Formatted date string.
     */
    fun convertDateFormat(timestamp: Long, pattern: String): String {
        return SimpleDateFormat(pattern, Locale.US).format(Date(timestamp))
    }

    /**
     * Helper to resolve UnitSystem enum from string key.
     * 
     * @param unitSystemStr The unit system key.
     * @return The corresponding UnitSystem enum.
     */
    fun getUnitSystem(unitSystemStr: String): UnitSystem {
        return when (unitSystemStr) {
            "METRIC_KMH" -> UnitSystem.METRIC_KMH
            "METRIC_MS" -> UnitSystem.METRIC_MS
            "IMPERIAL" -> UnitSystem.IMPERIAL
            "NAUTICAL" -> UnitSystem.NAUTICAL
            else -> {
                // Compatibility fallback
                when {
                    unitSystemStr.contains("km/h") -> UnitSystem.METRIC_KMH
                    unitSystemStr.contains("m/s") -> UnitSystem.METRIC_MS
                    unitSystemStr.contains("Imperial") || unitSystemStr.contains("Impérial") -> UnitSystem.IMPERIAL
                    unitSystemStr.contains("Nautical") || unitSystemStr.contains("Nautique") -> UnitSystem.NAUTICAL
                    else -> UnitSystem.METRIC_KMH
                }
            }
        }
    }
}
