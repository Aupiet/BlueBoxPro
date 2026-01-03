/**
 * This class holds the processed movement data and provides methods to access
 * them in various unit systems (Metric, Imperial, Nautical).
 */
package com.example.blueboxpro.Process

enum class UnitSystem {
    METRIC_KMH, METRIC_MS, IMPERIAL, NAUTICAL
}

class MovementResult(
    private val unitSystem: UnitSystem,
    // Accélérations (m/s²)
    private val accelX: Float,
    private val accelY: Float,
    private val accelZ: Float,
    // Vitesses (m/s)
    private val speedIMU: Float,
    private val speedGPS: Float,
    private val speedFused: Float,
    private val moyspeed: Float,
    private val sog: Float,
    // Navigation
    private val cog: Float,      // degrés
    private val azimuth: Float,  // degrés
    // Position/Précision
    private val altitude: Double, // mètres
    private val accuracy: Float   // mètres
) {
    companion object {
        private const val MS_TO_KMH = 3.6f
        private const val MS_TO_MPH = 2.23694f
        private const val MS_TO_KNOTS = 1.94384f
        private const val METERS_TO_FEET = 3.28084
        private const val METERS_TO_FEET_FLOAT = 3.28084f
    }

    /** Returns the X-axis acceleration. */
    fun getAccelX(): Float = accelX
    /** Returns the Y-axis acceleration. */
    fun getAccelY(): Float = accelY
    /** Returns the Z-axis acceleration. */
    fun getAccelZ(): Float = accelZ

    /** Returns the Speed Over Ground in the current unit system. */
    fun getSog(): Float = convertSpeed(sog)
    /** Returns the IMU-derived speed in the current unit system. */
    fun getSpeedIMU(): Float = convertSpeed(speedIMU)
    /** Returns the GPS-derived speed in the current unit system. */
    fun getSpeedGPS(): Float = convertSpeed(speedGPS)
    /** Returns the fused speed in the current unit system. */
    fun getSpeedFused(): Float = convertSpeed(speedFused)
    /** Returns the average speed in the current unit system. */
    fun getMoyspeed(): Float = convertSpeed(moyspeed)

    /** Returns the string representation of the speed unit. */
    fun getSpeedUnit(): String = when (unitSystem) {
        UnitSystem.METRIC_KMH -> "km/h"
        UnitSystem.METRIC_MS -> "m/s"
        UnitSystem.IMPERIAL -> "mph"
        UnitSystem.NAUTICAL -> "kn"
    }

    /** Returns Speed Over Ground in m/s. */
    fun getSogMs(): Float = sog
    /** Returns Speed Over Ground in km/h. */
    fun getSogKmh(): Float = sog * MS_TO_KMH
    /** Returns Speed Over Ground in mph. */
    fun getSogMph(): Float = sog * MS_TO_MPH
    /** Returns Speed Over Ground in knots. */
    fun getSogKnots(): Float = sog * MS_TO_KNOTS

    /** Returns the Course Over Ground in degrees. */
    fun getCog(): Float = cog
    /** Returns the Azimuth in degrees. */
    fun getAzimuth(): Float = azimuth

    /** Returns the altitude in the current unit system. */
    fun getAltitude(): Double = when (unitSystem) {
        UnitSystem.METRIC_KMH, UnitSystem.METRIC_MS, UnitSystem.NAUTICAL -> altitude
        UnitSystem.IMPERIAL -> altitude * METERS_TO_FEET
    }

    /** Returns the string representation of the altitude unit. */
    fun getAltitudeUnit(): String = when (unitSystem) {
        UnitSystem.METRIC_KMH, UnitSystem.METRIC_MS, UnitSystem.NAUTICAL -> "m"
        UnitSystem.IMPERIAL -> "ft"
    }

    /** Returns the accuracy in the current unit system. */
    fun getAccuracy(): Float = when (unitSystem) {
        UnitSystem.METRIC_KMH, UnitSystem.METRIC_MS, UnitSystem.NAUTICAL -> accuracy
        UnitSystem.IMPERIAL -> accuracy * METERS_TO_FEET_FLOAT
    }

    /** Returns the string representation of the accuracy unit. */
    fun getAccuracyUnit(): String = when (unitSystem) {
        UnitSystem.METRIC_KMH, UnitSystem.METRIC_MS, UnitSystem.NAUTICAL -> "m"
        UnitSystem.IMPERIAL -> "ft"
    }

    /**
     * Converts a speed value from m/s to the selected unit system.
     */
    private fun convertSpeed(speedMs: Float): Float {
        return when (unitSystem) {
            UnitSystem.METRIC_KMH -> speedMs * MS_TO_KMH
            UnitSystem.METRIC_MS -> speedMs
            UnitSystem.IMPERIAL -> speedMs * MS_TO_MPH
            UnitSystem.NAUTICAL -> speedMs * MS_TO_KNOTS
        }
    }
}
