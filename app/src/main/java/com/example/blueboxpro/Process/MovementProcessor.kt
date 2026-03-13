/**
 * This class processes motion data from sensors (accelerometer, GPS, compass)
 * to estimate speed and orientation using an Extended Kalman Filter (EKF) and Low Pass Filtering.
 */
package com.example.blueboxpro.Process

import org.osmdroid.util.GeoPoint
import kotlin.math.round
import kotlin.math.sqrt

class MovementProcessor {
    companion object {
        private const val GPS_TIMEOUT_MS = 5000L
        private const val MIN_GPS_ACCURACY = 50f
        private const val MAX_ACCEPTABLE_ACCURACY = 10f
        private const val AZIMUTH_ALPHA = 0.15f
        private const val SPEED_HISTORY_SIZE = 10
        private const val FULL_CIRCLE_DEGREES = 360f
        private const val HALF_CIRCLE_DEGREES = 180f
        
        // EKF & Filter Constants
        private const val CALCULATION_FREQUENCY_HZ = 50f
        private const val FIXED_DT = 1f / CALCULATION_FREQUENCY_HZ
        private const val LPF_ACCEL_ALPHA = 0.1f
        private const val Q_VEL = 0.001f
        private const val Q_BIAS = 0.0001f
        private const val R_BASE_GPS = 0.1f
    }

    private val ekfEstimator = EkfSpeedEstimator(qVel = Q_VEL, qBias = Q_BIAS)
    private var lastFilteredAccel = 0f
    
    var accelX: Float = 0f
    var accelY: Float = 0f
    var accelZ: Float = 0f

    var speedIMU: Float = 0f
    var speedGPS: Float = 0f
    private val speeds = FloatArray(SPEED_HISTORY_SIZE)
    var moyspeed: Float = 0f

    var altitude: Double = 0.0
    var lastLocation: GeoPoint? = null
    var gpsAccuracy: Float = 0f
    
    private var lastGpsUpdateMillis: Long = 0L
    private var timeAccumulator = 0f

    var sog: Float = 0f
    var cog: Float = 0f
    
    var azimuth: Float = 0f
    var moyaz: Float = 0f

    /**
     * Converts current movement state into a MovementResult.
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
            speedFused = ekfEstimator.velocity,
            moyspeed = moyspeed,
            sog = sog,
            cog = cog,
            azimuth = azimuth,
            altitude = altitude,
            accuracy = gpsAccuracy
        )
    }

    /**
     * Processes acceleration at 50Hz using the EKF estimator.
     */
    fun processAcceleration(ax: Float, ay: Float, az: Float, dt: Float) {
        checkGpsTimeout()
        
        accelX = ax
        accelY = ay
        accelZ = az

        val rawMag = sqrt(ax * ax + ay * ay + az * az)
        val filteredMag = LPF_ACCEL_ALPHA * rawMag + (1f - LPF_ACCEL_ALPHA) * lastFilteredAccel
        lastFilteredAccel = filteredMag

        timeAccumulator += dt
        
        while (timeAccumulator >= FIXED_DT) {
            ekfEstimator.predict(filteredMag, FIXED_DT)
            timeAccumulator -= FIXED_DT
        }
        
        speedIMU = ekfEstimator.velocity
        updateSpeedAverage(ekfEstimator.velocity)
    }

    private fun updateSpeedAverage(currentSpeed: Float) {
        for (i in 0 until speeds.size - 1) {
            speeds[i] = speeds[i + 1]
        }
        speeds[speeds.size - 1] = currentSpeed
        moyspeed = speeds.average().toFloat()
        sog = moyspeed
    }

    private fun checkGpsTimeout() {
        if (lastGpsUpdateMillis != 0L && System.currentTimeMillis() - lastGpsUpdateMillis > GPS_TIMEOUT_MS) {
            speedGPS = 0f
            lastGpsUpdateMillis = 0L
        }
    }

    /**
     * Updates EKF state using GPS data as the reference.
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
        speedGPS = gpsS
        gpsAccuracy = accuracy
        
        if (gpsS > 0.5f) {
            cog = gpsBearing
        }

        val rGps = R_BASE_GPS * (accuracy / MAX_ACCEPTABLE_ACCURACY).coerceAtLeast(1f)
        ekfEstimator.update(gpsS, rGps)
        
        updateSpeedAverage(ekfEstimator.velocity)
        onUpdate()
    }

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
     * Resets all internal states, including the EKF estimator.
     */
    fun reset() {
        ekfEstimator.reset()
        lastFilteredAccel = 0f
        timeAccumulator = 0f
        accelX = 0f
        accelY = 0f
        accelZ = 0f
        speedIMU = 0f
        speedGPS = 0f
        sog = 0f
        cog = 0f
        azimuth = 0f
        moyspeed = 0f
        altitude = 0.0
        gpsAccuracy = 0f
        lastGpsUpdateMillis = 0L
    }
}
