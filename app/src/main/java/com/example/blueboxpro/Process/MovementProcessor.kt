/**
 * This class processes motion data from sensors (accelerometer, GPS, compass)
 * to estimate speed and orientation using an Extended Kalman Filter (EKF) and Low Pass Filtering.
 * 
 * Acceleration is projected from the device body frame to the world frame (North/East)
 * and then onto the current heading direction to obtain a signed forward acceleration.
 */
package com.example.blueboxpro.Process

import com.example.blueboxpro.Option
import org.osmdroid.util.GeoPoint
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

class MovementProcessor {

    private val ekfEstimator = EkfSpeedEstimator(qVel = Option.Process.Q_VEL, qBias = Option.Process.Q_BIAS)
    private var lastFilteredAccel = 0f
    
    var accelX: Float = 0f
    var accelY: Float = 0f
    var accelZ: Float = 0f

    var speedIMU: Float = 0f
    var speedGPS: Float = 0f
    private val speeds = FloatArray(Option.Process.SPEED_HISTORY_SIZE)
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
     * Projects body-frame acceleration into the world frame using the rotation matrix,
     * then extracts the forward component along the current heading.
     * Falls back to magnitude-based estimation if no rotation matrix is available.
     *
     * @param rotMatrix 3x3 rotation matrix (row-major) from SensorManager, or null
     * @return Signed forward acceleration in m/s² (positive = accelerating, negative = braking)
     */
    private fun computeForwardAcceleration(
        ax: Float, ay: Float, az: Float, rotMatrix: FloatArray?
    ): Float {
        if (rotMatrix == null) {
            // Fallback: use Y-axis as rough forward proxy (phone in portrait, held upright)
            return ay
        }
        // Transform body-frame acceleration to world frame (North, East, Down)
        // world = R * body  where R is the 3x3 rotation matrix
        val worldNorth = rotMatrix[0] * ax + rotMatrix[1] * ay + rotMatrix[2] * az
        val worldEast  = rotMatrix[3] * ax + rotMatrix[4] * ay + rotMatrix[5] * az

        // Project horizontal world acceleration onto the current heading direction
        val headingRad = Math.toRadians(moyaz.toDouble()).toFloat()
        return worldNorth * cos(headingRad) + worldEast * sin(headingRad)
    }

    /**
     * Processes acceleration at ~50Hz using the EKF estimator.
     * The acceleration is projected into the world frame and along the heading
     * to provide a signed forward acceleration to the EKF.
     *
     * @param rotMatrix Optional rotation matrix from CaptorListener for body→world transform
     */
    fun processAcceleration(ax: Float, ay: Float, az: Float, dt: Float, rotMatrix: FloatArray? = null) {
        checkGpsTimeout()
        
        accelX = ax
        accelY = ay
        accelZ = az

        // Project acceleration into the forward direction (signed)
        val rawForward = computeForwardAcceleration(ax, ay, az, rotMatrix)

        // Low-pass filter to smooth high-frequency noise
        val filteredAccel = Option.Process.LPF_ACCEL_ALPHA * rawForward +
                (1f - Option.Process.LPF_ACCEL_ALPHA) * lastFilteredAccel
        lastFilteredAccel = filteredAccel

        // Fixed time-step integration for numerical stability
        timeAccumulator += dt
        while (timeAccumulator >= Option.Process.FIXED_DT) {
            ekfEstimator.predict(filteredAccel, Option.Process.FIXED_DT)
            timeAccumulator -= Option.Process.FIXED_DT
        }
        
        speedIMU = ekfEstimator.velocity
        updateSpeedAverage(ekfEstimator.velocity)
    }

    /**
     * Updates the speed history and computes median-filtered SOG.
     * Uses median filtering to reject outlier speed values.
     */
    private fun updateSpeedAverage(currentSpeed: Float) {
        // Apply dead zone: speeds below threshold are considered zero
        val cleanSpeed = if (currentSpeed < Option.Process.DEAD_ZONE_SPEED) 0f else currentSpeed

        for (i in 0 until speeds.size - 1) {
            speeds[i] = speeds[i + 1]
        }
        speeds[speeds.size - 1] = cleanSpeed
        moyspeed = speeds.average().toFloat()

        // Median filter over a recent window for SOG display stability
        val windowSize = Option.Process.MEDIAN_WINDOW_SIZE.coerceAtMost(speeds.size)
        val recentSpeeds = speeds.takeLast(windowSize).sorted()
        sog = recentSpeeds[recentSpeeds.size / 2]
    }

    private fun checkGpsTimeout() {
        if (lastGpsUpdateMillis != 0L && System.currentTimeMillis() - lastGpsUpdateMillis > Option.Process.GPS_TIMEOUT_MS) {
            speedGPS = 0f
            lastGpsUpdateMillis = 0L
        }
    }

    /**
     * Updates EKF state using GPS data as the reference.
     */
    fun updateWithGPS(lat: Double, lon: Double, alt: Double, gpsS: Float, gpsBearing: Float, accuracy: Float, onUpdate: () -> Unit) {
        if (accuracy > Option.Process.MIN_GPS_ACCURACY) {
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

        val rGps = Option.Process.R_BASE_GPS * (accuracy / Option.Process.MAX_ACCEPTABLE_ACCURACY).coerceAtLeast(1f)
        ekfEstimator.update(gpsS, rGps)
        
        updateSpeedAverage(ekfEstimator.velocity)
        onUpdate()
    }

    fun updateOrientation(newaz: Float, onUpdate: () -> Unit) {
        var diffaz: Float = newaz - moyaz

        while (diffaz < -Option.Process.HALF_CIRCLE_DEGREES) diffaz += Option.Process.FULL_CIRCLE_DEGREES
        while (diffaz > Option.Process.HALF_CIRCLE_DEGREES) diffaz -= Option.Process.FULL_CIRCLE_DEGREES

        moyaz = (moyaz + Option.Process.AZIMUTH_ALPHA * diffaz)

        if (moyaz < 0) moyaz += Option.Process.FULL_CIRCLE_DEGREES
        if (moyaz >= Option.Process.FULL_CIRCLE_DEGREES) moyaz -= Option.Process.FULL_CIRCLE_DEGREES
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
