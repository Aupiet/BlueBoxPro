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
    // --- ACCESSEURS ACCÉLÉRATION ---
    fun getAccelX(): Float = accelX
    fun getAccelY(): Float = accelY
    fun getAccelZ(): Float = accelZ

    // --- ACCESSEURS VITESSE (Respectent le système d'unité) ---
    fun getSog(): Float = convertSpeed(sog)
    fun getSpeedIMU(): Float = convertSpeed(speedIMU)
    fun getSpeedGPS(): Float = convertSpeed(speedGPS)
    fun getSpeedFused(): Float = convertSpeed(speedFused)
    fun getMoyspeed(): Float = convertSpeed(moyspeed)

    fun getSpeedUnit(): String = when (unitSystem) {
        UnitSystem.METRIC_KMH -> "km/h"
        UnitSystem.METRIC_MS -> "m/s"
        UnitSystem.IMPERIAL -> "mph"
        UnitSystem.NAUTICAL -> "kn"
    }

    // --- ACCESSEURS SPÉCIFIQUES ---
    fun getSogMs(): Float = sog
    fun getSogKmh(): Float = sog * 3.6f
    fun getSogMph(): Float = sog * 2.23694f
    fun getSogKnots(): Float = sog * 1.94384f

    // --- ACCESSEURS NAVIGATION ---
    fun getCog(): Float = cog
    fun getAzimuth(): Float = azimuth

    // --- ACCESSEURS ALTITUDE & PRÉCISION ---
    fun getAltitude(): Double = when (unitSystem) {
        UnitSystem.METRIC_KMH, UnitSystem.METRIC_MS, UnitSystem.NAUTICAL -> altitude
        UnitSystem.IMPERIAL -> altitude * 3.28084
    }

    fun getAltitudeUnit(): String = when (unitSystem) {
        UnitSystem.METRIC_KMH, UnitSystem.METRIC_MS, UnitSystem.NAUTICAL -> "m"
        UnitSystem.IMPERIAL -> "ft"
    }

    fun getAccuracy(): Float = when (unitSystem) {
        UnitSystem.METRIC_KMH, UnitSystem.METRIC_MS, UnitSystem.NAUTICAL -> accuracy
        UnitSystem.IMPERIAL -> accuracy * 3.28084f
    }

    fun getAccuracyUnit(): String = when (unitSystem) {
        UnitSystem.METRIC_KMH, UnitSystem.METRIC_MS, UnitSystem.NAUTICAL -> "m"
        UnitSystem.IMPERIAL -> "ft"
    }

    // --- MÉTHODES PRIVÉES DE CONVERSION ---
    private fun convertSpeed(speedMs: Float): Float {
        return when (unitSystem) {
            UnitSystem.METRIC_KMH -> speedMs * 3.6f
            UnitSystem.METRIC_MS -> speedMs
            UnitSystem.IMPERIAL -> speedMs * 2.23694f
            UnitSystem.NAUTICAL -> speedMs * 1.94384f
        }
    }
}
