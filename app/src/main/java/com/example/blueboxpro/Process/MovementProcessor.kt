package com.example.blueboxpro.Process

import org.osmdroid.util.GeoPoint
import kotlin.math.round
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
    private val speeds = FloatArray(10)
    var moyspeed: Float = 0f

    var altitude: Double = 0.0
    var lastLocation: GeoPoint? = null
    var gpsAccuracy: Float = 0f
    
    private var lastGpsUpdateMillis: Long = 0L
    private val GPS_TIMEOUT_MS = 5000L
    private val MIN_GPS_ACCURACY = 50f // Précision minimum acceptable en mètres

    // SOG (Speed Over Ground) et COG (Course Over Ground)
    var sog: Float = 0f // Vitesse fond (souvent en m/s ou nœuds)
    var cog: Float = 0f // Route fond (en degrés 0-360)
    
    // Boussole (Compass)
    var azimuth: Float = 0f // Orientation du téléphone par rapport au Nord magnétique
    private val azarray = FloatArray(3)
    var newaz: Float = 0f

    var moyaz: Float = 0f

    // Seuils de vitesse (offsets) différents pour l'IMU et le GPS
    private val speedThresholdIMU = 1.0f
    private val speedThresholdGPS = 1.0f

    fun processAcceleration(ax: Float, ay: Float, az: Float, dt: Float) {
        checkGpsTimeout()
        
        accelX = ax
        accelY = ay
        accelZ = az
        
        kalmanX.predict(ax, dt)
        kalmanY.predict(ay, dt)
        kalmanZ.predict(az, dt)

        val rawSpeedIMU = sqrt(kalmanX.x * kalmanX.x + kalmanY.x * kalmanY.x + kalmanZ.x * kalmanZ.x)
        speedIMU = if (rawSpeedIMU < speedThresholdIMU) 0f else rawSpeedIMU
        // La vitesse fusionnée suit initialement le seuil de l'IMU avant correction GPS
        speedFused = if (rawSpeedIMU < speedThresholdIMU) 0f else rawSpeedIMU
        // Moyenne des dernières vitesses
        speeds.forEachIndexed { index, _ ->
            if (index < speeds.size - 1) {
                speeds[index] = speeds[index + 1]
            }
        }
        speeds[speeds.size - 1] = speedFused
        moyspeed = speeds.average().toFloat()
        sog = moyspeed
    }

    private fun checkGpsTimeout() {
        if (lastGpsUpdateMillis != 0L && System.currentTimeMillis() - lastGpsUpdateMillis > GPS_TIMEOUT_MS) {
            speedGPS = 0f
            lastGpsUpdateMillis = 0L
        }
    }

    fun updateWithGPS(lat: Double, lon: Double, alt: Double, gpsS: Float, gpsBearing: Float, accuracy: Float, onUpdate: () -> Unit) {
        // On ne traite les données que si la précision est suffisante
        if (accuracy > MIN_GPS_ACCURACY) {
            gpsAccuracy = accuracy
            onUpdate()
            return
        }

        lastGpsUpdateMillis = System.currentTimeMillis()
        lastLocation = GeoPoint(lat, lon)
        altitude = alt
        speedGPS = if (gpsS < speedThresholdGPS) 0f else gpsS
        gpsAccuracy = accuracy
        
        // Le COG (Course Over Ground) est donné par le 'bearing' du GPS
        if (gpsS > speedThresholdGPS) {
            cog = gpsBearing
        }

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
        speedFused = if (rawSpeedFused < speedThresholdGPS) 0f else rawSpeedFused
        
        // Moyenne des dernières vitesses
        speeds.forEachIndexed { index, _ ->
            if (index < speeds.size - 1) {
                speeds[index] = speeds[index + 1]
            }
        }
        speeds[speeds.size - 1] = speedFused
        moyspeed = speeds.average().toFloat()
        sog = moyspeed
        onUpdate()
    }

    private val ALPHA = 0.15f
    fun updateOrientation(newaz: Float, onUpdate: () -> Unit) {
        var diffaz: Float = newaz - moyaz

        while (diffaz < -180f) diffaz += 360f
        while (diffaz > 180f) diffaz -= 360f

        moyaz = (moyaz + ALPHA * diffaz)

        if (moyaz < 0) moyaz += 360f
        if (moyaz >= 360) moyaz -= 360f
        azimuth = round(moyaz)
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
        sog = 0f
        cog = 0f
        azimuth = 0f
        moyspeed = 0f
        altitude = 0.0
        gpsAccuracy = 0f
        lastGpsUpdateMillis = 0L
    }
}
