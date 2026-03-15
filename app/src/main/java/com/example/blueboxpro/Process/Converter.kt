/**
 * Utility object for data conversion across different unit systems and formats.
 * Centralizes logic for speed, altitude, temperature, angles, and date formatting.
 */
package com.example.blueboxpro.Process

import com.example.blueboxpro.Option
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI

/**
 * Provides methods to convert raw sensor data into user-friendly formats.
 */
object Converter {

    /**
     * Converts speed from meters per second to the target unit system.
     */
    fun convertSpeed(speedMs: Float, unitSystem: UnitSystem): Float {
        return when (unitSystem) {
            UnitSystem.METRIC_KMH -> speedMs * Option.Movement.MS_TO_KMH
            UnitSystem.METRIC_MS -> speedMs
            UnitSystem.IMPERIAL -> speedMs * Option.Movement.MS_TO_MPH
            UnitSystem.NAUTICAL -> speedMs * Option.Movement.MS_TO_KNOTS
        }
    }

    fun convertSpeed(speedMs: Float, unitSystemStr: String): Float {
        return convertSpeed(speedMs, getUnitSystem(unitSystemStr))
    }

    /**
     * Converts altitude from meters to the target unit system.
     */
    fun convertAlt(altitudeMeters: Double, unitSystem: UnitSystem): Double {
        return when (unitSystem) {
            UnitSystem.IMPERIAL -> altitudeMeters * Option.Movement.METERS_TO_FEET
            else -> altitudeMeters
        }
    }

    /**
     * Temperature conversions.
     */
    fun celsiusToFahrenheit(celsius: Double): Double = (celsius * 9 / 5) + 32
    fun celsiusToKelvin(celsius: Double): Double = celsius + 273.15
    fun fahrenheitToCelsius(fahrenheit: Double): Double = (fahrenheit - 32) * 5 / 9
    fun kelvinToCelsius(kelvin: Double): Double = kelvin - 273.15

    /**
     * Angle conversions.
     */
    fun degreesToRadians(degrees: Double): Double = degrees * PI / 180.0
    fun radiansToDegrees(radians: Double): Double = radians * 180.0 / PI

    /**
     * Formats a timestamp into a readable date string.
     */
    fun convertDateFormat(timestamp: Long, pattern: String): String {
        return SimpleDateFormat(pattern, Locale.US).format(Date(timestamp))
    }

    /**
     * Helper to resolve UnitSystem enum from string key.
     */
    fun getUnitSystem(unitSystemStr: String): UnitSystem {
        return when {
            unitSystemStr == "METRIC_KMH" -> UnitSystem.METRIC_KMH
            unitSystemStr == "METRIC_MS" -> UnitSystem.METRIC_MS
            unitSystemStr == "IMPERIAL" -> UnitSystem.IMPERIAL
            unitSystemStr == "NAUTICAL" -> UnitSystem.NAUTICAL
            unitSystemStr.contains("km/h") -> UnitSystem.METRIC_KMH
            unitSystemStr.contains("m/s") -> UnitSystem.METRIC_MS
            unitSystemStr.contains("Imperial") || unitSystemStr.contains("Impérial") -> UnitSystem.IMPERIAL
            unitSystemStr.contains("Nautical") || unitSystemStr.contains("Nautique") -> UnitSystem.NAUTICAL
            else -> UnitSystem.METRIC_KMH
        }
    }
}
