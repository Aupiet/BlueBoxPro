package com.example.blueboxpro.Process

import org.osmdroid.util.GeoPoint
import kotlin.math.sqrt

class MovementProcessor {
    val kalmanX = SimpleKalmanFilter()
    val kalmanY = SimpleKalmanFilter()
    val kalmanZ = SimpleKalmanFilter()

    var accelX: Float = 0f
    var accelY: Float = 0f
    var accelZ: Float = 0f
    var speedIMU: Float = 0f
    var speedGPS: Float = 0f
    var speedFused: Float = 0f
    var altitude: Double = 0.0
    var lastLocation: GeoPoint? = null

    // Seuil de vitesse (offset) pour ignorer le bruit à l'arrêt
    private val speedThreshold = 1.0f

    fun processAcceleration(ax: Float, ay: Float, az: Float, dt: Float) {
        accelX = ax
        accelY = ay
        accelZ = az
        
        kalmanX.predict(ax, dt)
        kalmanY.predict(ay, dt)
        kalmanZ.predict(az, dt)

        val rawSpeedIMU = sqrt(kalmanX.x * kalmanX.x + kalmanY.x * kalmanY.x + kalmanZ.x * kalmanZ.x)
        speedIMU = if (rawSpeedIMU < speedThreshold) 0f else rawSpeedIMU
        speedFused = if (rawSpeedIMU < speedThreshold) 0f else rawSpeedIMU
    }

    fun updateWithGPS(lat: Double, lon: Double, alt: Double, gpsS: Float, onUpdate: () -> Unit) {
        lastLocation = GeoPoint(lat, lon)
        altitude = alt
        speedGPS = if (gpsS < speedThreshold) 0f else gpsS
        
        val currentIMUSpeed = sqrt(kalmanX.x * kalmanX.x + kalmanY.x * kalmanY.x + kalmanZ.x * kalmanZ.x)
        if (currentIMUSpeed > 0.1f) {
            val ratio = gpsS / currentIMUSpeed
            kalmanX.update(kalmanX.x * ratio)
            kalmanY.update(kalmanY.x * ratio)
            kalmanZ.update(kalmanZ.x * ratio)
        } else {
            kalmanX.update(0f)
            kalmanY.update(0f)
            kalmanZ.update(0f)
        }
        
        val rawSpeedFused = sqrt(kalmanX.x * kalmanX.x + kalmanY.x * kalmanY.x + kalmanZ.x * kalmanZ.x)
        speedFused = if (rawSpeedFused < speedThreshold) 0f else rawSpeedFused
        
        onUpdate()
    }

    fun reset() {
        kalmanX.x = 0f
        kalmanY.x = 0f
        kalmanZ.x = 0f
        accelX = 0f
        accelY = 0f
        accelZ = 0f
        speedIMU = 0f
        speedGPS = 0f
        speedFused = 0f
        altitude = 0.0
    }
}
