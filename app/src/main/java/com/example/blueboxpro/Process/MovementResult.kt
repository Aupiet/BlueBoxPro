/**
 * This class encapsulates the processed results of movement analysis.
 * It provides methods to retrieve various motion parameters (speed, orientation, acceleration)
 * formatted and converted according to a specific UnitSystem.
 */
package com.example.blueboxpro.Process

import com.example.blueboxpro.Option
import kotlin.math.round

/**
 * Supported systems of measurement.
 */
enum class UnitSystem {
    METRIC_KMH, METRIC_MS, IMPERIAL, NAUTICAL
}

/**
 * Represents a snapshot of processed movement data.
 * 
 * @param unitSystem The unit system used for conversions.
 * @param accelX X-axis acceleration in m/s².
 * @param accelY Y-axis acceleration in m/s².
 * @param accelZ Z-axis acceleration in m/s².
 * @param speedIMU Speed estimated from inertial sensors (m/s).
 * @param speedGPS Speed provided by GPS (m/s).
 * @param speedFused Speed resulting from sensor fusion (m/s).
 * @param averageSpeed Rolling average of speed (m/s).
 * @param sog Speed Over Ground (filtered) in m/s.
 * @param cog Course Over Ground in degrees.
 * @param azimuth Compass heading in degrees.
 * @param altitude Altitude in meters.
 * @param accuracy Horizontal GPS accuracy in meters.
 */
class MovementResult(
    private val unitSystem: UnitSystem,
    private val accelX: Float,
    private val accelY: Float,
    private val accelZ: Float,
    private val speedIMU: Float,
    private val speedGPS: Float,
    private val speedFused: Float,
    private val averageSpeed: Float,
    private val sog: Float,
    private val cog: Float,
    private val azimuth: Float,
    private val altitude: Double,
    private val accuracy: Float,
    private val  pitch: Int,
    private val roll: Int
) {
    /** 
     * Rounds a float to a specified number of decimals based on the global rounding factor.
     */
    private fun Float.roundToConfiguredDecimal(): Float = 
        round(this * Option.Movement.ROUNDING_FACTOR) / Option.Movement.ROUNDING_FACTOR

    /** Returns raw X-axis acceleration. */
    fun getAccelX(): Float = accelX
    /** Returns raw Y-axis acceleration. */
    fun getAccelY(): Float = accelY
    /** Returns raw Z-axis acceleration. */
    fun getAccelZ(): Float = accelZ

    /** Returns Speed Over Ground in the active unit system. */
    fun getSog(): Float = convertSpeed(sog).roundToConfiguredDecimal()
    /** Returns IMU speed in the active unit system. */
    fun getSpeedIMU(): Float = convertSpeed(speedIMU).roundToConfiguredDecimal()
    /** Returns GPS speed in the active unit system. */
    fun getSpeedGPS(): Float = convertSpeed(speedGPS).roundToConfiguredDecimal()
    /** Returns fused speed in the active unit system. */
    fun getSpeedFused(): Float = convertSpeed(speedFused).roundToConfiguredDecimal()
    /** Returns average speed in the active unit system. */
    fun getMoyspeed(): Float = convertSpeed(averageSpeed).roundToConfiguredDecimal()

    /** 
     * Returns the localized unit string for speed.
     */
    fun getSpeedUnit(): String = when (unitSystem) {
        UnitSystem.METRIC_KMH -> "km/h"
        UnitSystem.METRIC_MS -> "m/s"
        UnitSystem.IMPERIAL -> "mph"
        UnitSystem.NAUTICAL -> "kn"
    }

    /** Returns the Course Over Ground in degrees. */
    fun getCog(): Float = cog
    /** Returns the Azimuth (heading) in degrees. */
    fun getAzimuth(): Float = azimuth

    /** 
     * Returns the altitude converted to the active unit system.
     */
    fun getAltitude(): Double = when (unitSystem) {
        UnitSystem.METRIC_KMH, UnitSystem.METRIC_MS, UnitSystem.NAUTICAL -> altitude
        UnitSystem.IMPERIAL -> altitude * Option.Movement.METERS_TO_FEET
    }
    /** Returns the Pitch in degrees. */
    fun getPitch(): Int = pitch

    /** Returns the Roll in degrees. */
    fun getRoll(): Int = roll
    /** 
     * Returns the unit label for altitude.
     */
    fun getAltitudeUnit(): String = when (unitSystem) {
        UnitSystem.METRIC_KMH, UnitSystem.METRIC_MS, UnitSystem.NAUTICAL -> "m"
        UnitSystem.IMPERIAL -> "ft"
    }

    /** 
     * Returns the GPS accuracy converted to the active unit system.
     */
    fun getAccuracy(): Float = when (unitSystem) {
        UnitSystem.METRIC_KMH, UnitSystem.METRIC_MS, UnitSystem.NAUTICAL -> accuracy
        UnitSystem.IMPERIAL -> accuracy * Option.Movement.METERS_TO_FEET_FLOAT
    }

    /** 
     * Returns the unit label for accuracy.
     */
    fun getAccuracyUnit(): String = when (unitSystem) {
        UnitSystem.METRIC_KMH, UnitSystem.METRIC_MS, UnitSystem.NAUTICAL -> "m"
        UnitSystem.IMPERIAL -> "ft"
    }

    /**
     * Internal utility to convert m/s values to the selected unit system.
     */
    private fun convertSpeed(speedMs: Float): Float {
        return when (unitSystem) {
            UnitSystem.METRIC_KMH -> speedMs * Option.Movement.MS_TO_KMH
            UnitSystem.METRIC_MS -> speedMs
            UnitSystem.IMPERIAL -> speedMs * Option.Movement.MS_TO_MPH
            UnitSystem.NAUTICAL -> speedMs * Option.Movement.MS_TO_KNOTS
        }
    }
}
