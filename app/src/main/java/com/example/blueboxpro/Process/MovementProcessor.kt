/**
 * This class processes motion data from sensors (accelerometer, GPS, compass)
 * to estimate speed, orientation, and location using Kalman filtering and ZUPT logic.
 */
package com.example.blueboxpro.Process

import org.osmdroid.util.GeoPoint
import kotlin.math.round
import kotlin.math.sqrt

class MovementProcessor {
    companion object {
        private const val GPS_TIMEOUT_MS = 5000L
        private const val MIN_GPS_ACCURACY = 50f
        private const val SPEED_THRESHOLD_IMU = 0.5f
        private const val SPEED_THRESHOLD_GPS = 0.5f
        private const val ZUPT_ACCEL_THRESHOLD = 0.15f
        private const val STATIONARY_SAMPLES_REQUIRED = 20
        private const val HPF_ALPHA = 0.98f
        private const val AZIMUTH_ALPHA = 0.15f
        private const val SPEED_HISTORY_SIZE = 10
        private const val FULL_CIRCLE_DEGREES = 360f
        private const val HALF_CIRCLE_DEGREES = 180f
        private const val LOW_SPEED_RATIO_THRESHOLD = 0.1f
    }

    val kalmanX = SimpleKalmanFilter()
    val kalmanY = SimpleKalmanFilter()
    val kalmanZ = SimpleKalmanFilter()

    var accelX: Float = 0f
    var accelY: Float = 0f
    var accelZ: Float = 0f
    var speedIMU: Float = 0f
    var speedGPS: Float = 0f
    var speedFused: Float = 0f
    private val speeds = FloatArray(SPEED_HISTORY_SIZE)
    var moyspeed: Float = 0f

    var altitude: Double = 0.0
    var lastLocation: GeoPoint? = null
    var gpsAccuracy: Float = 0f
    
    private var lastGpsUpdateMillis: Long = 0L

    var sog: Float = 0f // Speed Over Ground
    var cog: Float = 0f // Course Over Ground
    
    var azimuth: Float = 0f
    var moyaz: Float = 0f

    /**
     * Converts current movement state into a MovementResult based on the selected unit system.
     */
    fun getResult(unitSystemStr: String): MovementResult {
        val unitSystem = when {
            unitSystemStr.contains("km/h") -> UnitSystem.METRIC_KMH
            unitSystemStr.contains("m/s") -> UnitSystem.METRIC_MS
            unitSystemStr.contains("Impérial") -> UnitSystem.IMPERIAL
            unitSystemStr.contains("Nautique") -> UnitSystem.NAUTICAL
            else -> UnitSystem.METRIC_KMH
        }
        return MovementResult(
            unitSystem = unitSystem,
            accelX = accelX,
            accelY = accelY,
            accelZ = accelZ,
            speedIMU = speedIMU,
            speedGPS = speedGPS,
            speedFused = speedFused,
            moyspeed = moyspeed,
            sog = sog,
            cog = cog,
            azimuth = azimuth,
            altitude = altitude,
            accuracy = gpsAccuracy
        )
    }

    /**
     * Processes raw acceleration data to estimate speed via integration and Kalman filtering.
     */
    fun processAcceleration(ax: Float, ay: Float, az: Float, dt: Float) {
        checkGpsTimeout()
        
        accelX = ax
        accelY = ay
        accelZ = az

        val accelMag = sqrt(ax * ax + ay * ay + az * az)
        if (accelMag < ZUPT_ACCEL_THRESHOLD) {
            stationaryCount++
        } else {
            stationaryCount = 0
        }

        if (stationaryCount >= STATIONARY_SAMPLES_REQUIRED) {
            kalmanX.x = 0f
            kalmanY.x = 0f
            kalmanZ.x = 0f
        } else {
            kalmanX.predict(ax, dt)
            kalmanY.predict(ay, dt)
            kalmanZ.predict(az, dt)

            kalmanX.x *= HPF_ALPHA
            kalmanY.x *= HPF_ALPHA
            kalmanZ.x *= HPF_ALPHA
        }

        val rawSpeedIMU = sqrt(kalmanX.x * kalmanX.x + kalmanY.x * kalmanY.x + kalmanZ.x * kalmanZ.x)
        speedIMU = if (rawSpeedIMU < SPEED_THRESHOLD_IMU) 0f else rawSpeedIMU
        speedFused = if (rawSpeedIMU < SPEED_THRESHOLD_IMU) 0f else rawSpeedIMU
        
        updateSpeedAverage(speedFused)
    }

    private var stationaryCount = 0

    /**
     * Updates the running average of the speed.
     */
    private fun updateSpeedAverage(currentSpeed: Float) {
        for (i in 0 until speeds.size - 1) {
            speeds[i] = speeds[i + 1]
        }
        speeds[speeds.size - 1] = currentSpeed
        moyspeed = speeds.average().toFloat()
        sog = moyspeed
    }

    /**
     * Resets GPS speed if no update has been received for a certain duration.
     */
    private fun checkGpsTimeout() {
        if (lastGpsUpdateMillis != 0L && System.currentTimeMillis() - lastGpsUpdateMillis > GPS_TIMEOUT_MS) {
            speedGPS = 0f
            lastGpsUpdateMillis = 0L
        }
    }

    /**
     * Updates the processor state with fresh GPS data.
     */
    fun updateWithGPS(lat: Double, lon: Double, alt: Double, gpsS: Float, gpsBearing: Float, accuracy: Float, onUpdate: () -> Unit) {
        if (accuracy > MIN_GPS_ACCURACY) {
            gpsAccuracy = accuracy
            onUpdate()
            return
        }

        lastGpsUpdateMillis = System.currentTimeMillis()
        lastLocation = GeoPoint(lat, lon)
        altitude = alt
        speedGPS = if (gpsS < SPEED_THRESHOLD_GPS) 0f else gpsS
        gpsAccuracy = accuracy
        
        if (gpsS > SPEED_THRESHOLD_GPS) {
            cog = gpsBearing
        }

        val currentIMUSpeed = sqrt(kalmanX.x * kalmanX.x + kalmanY.x * kalmanY.x + kalmanZ.x * kalmanZ.x)
        if (currentIMUSpeed > LOW_SPEED_RATIO_THRESHOLD) {
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
        speedFused = if (rawSpeedFused < SPEED_THRESHOLD_GPS) 0f else rawSpeedFused
        
        updateSpeedAverage(speedFused)
        onUpdate()
    }

    /**
     * Updates the azimuth (compass orientation) with smoothing.
     */
    fun updateOrientation(newaz: Float, onUpdate: () -> Unit) {
        var diffaz: Float = newaz - moyaz

        while (diffaz < -HALF_CIRCLE_DEGREES) diffaz += FULL_CIRCLE_DEGREES
        while (diffaz > HALF_CIRCLE_DEGREES) diffaz -= FULL_CIRCLE_DEGREES

        moyaz = (moyaz + AZIMUTH_ALPHA * diffaz)

        if (moyaz < 0) moyaz += FULL_CIRCLE_DEGREES
        if (moyaz >= FULL_CIRCLE_DEGREES) moyaz -= FULL_CIRCLE_DEGREES
        azimuth = round(moyaz)
        onUpdate()
    }

    /**
     * Resets all internal states of the movement processor.
     */
    fun reset() {
        kalmanX.x = 0f
        kalmanY.x = 0f
        kalmanZ.x = 0f
        stationaryCount = 0
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
