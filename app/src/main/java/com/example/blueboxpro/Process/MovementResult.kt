package com.example.blueboxpro.Process

enum class UnitSystem {
    METRIC, IMPERIAL, NAUTICAL
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
        UnitSystem.METRIC -> "km/h"
        UnitSystem.IMPERIAL -> "mph"
        UnitSystem.NAUTICAL -> "kn"
    }

    // --- ACCESSEURS SPÉCIFIQUES (Unités fixes) ---
    fun getSogMs(): Float = sog
    fun getSogKmh(): Float = sog * 3.6f
    fun getSogMph(): Float = sog * 2.23694f
    fun getSogKnots(): Float = sog * 1.94384f

    // --- ACCESSEURS NAVIGATION ---
    fun getCog(): Float = cog
    fun getAzimuth(): Float = azimuth

    // --- ACCESSEURS ALTITUDE & PRÉCISION ---
    fun getAltitude(): Double = when (unitSystem) {
        UnitSystem.METRIC -> altitude
        UnitSystem.IMPERIAL -> altitude * 3.28084
        UnitSystem.NAUTICAL -> altitude // On garde les mètres ou on pourrait utiliser des brasses ? Restons sur mètres.
    }

    fun getAltitudeUnit(): String = when (unitSystem) {
        UnitSystem.METRIC -> "m"
        UnitSystem.IMPERIAL -> "ft"
        UnitSystem.NAUTICAL -> "m"
    }

    fun getAccuracy(): Float = when (unitSystem) {
        UnitSystem.METRIC -> accuracy
        UnitSystem.IMPERIAL -> accuracy * 3.28084f
        UnitSystem.NAUTICAL -> accuracy
    }

    fun getAccuracyUnit(): String = when (unitSystem) {
        UnitSystem.METRIC -> "m"
        UnitSystem.IMPERIAL -> "ft"
        UnitSystem.NAUTICAL -> "m"
    }

    // --- MÉTHODES PRIVÉES DE CONVERSION ---
    private fun convertSpeed(speedMs: Float): Float {
        return when (unitSystem) {
            UnitSystem.METRIC -> speedMs * 3.6f // Vers km/h
            UnitSystem.IMPERIAL -> speedMs * 2.23694f // Vers mph
            UnitSystem.NAUTICAL -> speedMs * 1.94384f // Vers Noeuds (Knots)
        }
    }
}
