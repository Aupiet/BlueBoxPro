/**
 * Core engine for processing motion data from various sensors (Accelerometer, GPS, Compass).
 * It uses an Extended Kalman Filter (EKF) to fuse inertial data with GPS updates for
 * high-precision speed estimation, and applies low-pass filtering for orientation smoothing.
 */
package com.example.blueboxpro.Process

import com.example.blueboxpro.Option
import org.osmdroid.util.GeoPoint
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

/**
 * Handles the calculation and filtering of movement parameters like Speed Over Ground (SOG),
 * Course Over Ground (COG), and device orientation (Azimuth).
 */
class MovementProcessor {

    private val ekfEstimator = EkfSpeedEstimator(
        qVel = Option.Process.Q_VEL, 
        qBias = Option.Process.Q_BIAS
    )
    private var lastFilteredAccel = 0f
    
    // Raw acceleration values (body frame)
    var accelX: Float = 0f
    var accelY: Float = 0f
    var accelZ: Float = 0f

    // Intermediate speed estimations
    var speedIMU: Float = 0f
    var speedGPS: Float = 0f
    private val speedHistory = FloatArray(Option.Process.SPEED_HISTORY_SIZE)
    var averageSpeed: Float = 0f

    // Positional data
    var altitude: Double = 0.0
    var lastLocation: GeoPoint? = null
    var gpsAccuracy: Float = 0f
    
    private var lastGpsUpdateMillis: Long = 0L
    private var timeAccumulator = 0f

    // Final navigation outputs
    var sog: Float = 0f
    var cog: Float = 0f
    
    var azimuth: Float = 0f
    private var averageAzimuth: Float = 0f

    //angles
    var pitch: Int = 0
    var roll: Int = 0

    /**
     * Captures the current state of movement into a immutable MovementResult object.
     * Use this to get a snapshot of data formatted for a specific unit system.
     * 
     * @param unitSystemStr The key identifying the desired unit system (e.g., "METRIC_KMH").
     * @return A MovementResult containing converted values.
     */
    fun getResult(unitSystemStr: String): MovementResult {
        val unitSystem = when {
            unitSystemStr.contains("km/h") -> UnitSystem.METRIC_KMH
            unitSystemStr.contains("m/s") -> UnitSystem.METRIC_MS
            unitSystemStr.contains("Imperial") || unitSystemStr.contains("Impérial") -> UnitSystem.IMPERIAL
            unitSystemStr.contains("Nautical") || unitSystemStr.contains("Nautique") -> UnitSystem.NAUTICAL
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
            averageSpeed = averageSpeed,
            sog = sog,
            cog = cog,
            azimuth = azimuth,
            altitude = altitude,
            accuracy = gpsAccuracy,
            pitch = this.pitch,
            roll = this.roll
        )
    }

    /**
     * Projects body-frame acceleration into the world frame using a rotation matrix.
     * 
     * @param ax Raw X acceleration.
     * @param ay Raw Y acceleration.
     * @param az Raw Z acceleration.
     * @param rotMatrix 3x3 rotation matrix (row-major).
     * @return Forward acceleration in m/s².
     */
    private fun computeForwardAcceleration(
        ax: Float, ay: Float, az: Float, rotMatrix: FloatArray?
    ): Float {
        if (rotMatrix == null) {
            return ay // Fallback
        }
        val worldNorth = rotMatrix[0] * ax + rotMatrix[1] * ay + rotMatrix[2] * az
        val worldEast  = rotMatrix[3] * ax + rotMatrix[4] * ay + rotMatrix[5] * az

        val headingRad = Math.toRadians(averageAzimuth.toDouble()).toFloat()
        return worldNorth * cos(headingRad) + worldEast * sin(headingRad)
    }

    /**
     * Processes a new acceleration sample. Usually called at high frequency (~50Hz).
     * 
     * @param ax Raw X acceleration (m/s²).
     * @param ay Raw Y acceleration (m/s²).
     * @param az Raw Z acceleration (m/s²).
     * @param dt Time since last sample in seconds.
     * @param rotMatrix Optional rotation matrix for frame transformation.
     */
    fun processAcceleration(ax: Float, ay: Float, az: Float, dt: Float, rotMatrix: FloatArray? = null) {
        checkGpsTimeout()
        
        accelX = ax
        accelY = ay
        accelZ = az

        val rawForward = computeForwardAcceleration(ax, ay, az, rotMatrix)

        val filteredAccel = Option.Process.LPF_ACCEL_ALPHA * rawForward +
                (1f - Option.Process.LPF_ACCEL_ALPHA) * lastFilteredAccel
        lastFilteredAccel = filteredAccel

        timeAccumulator += dt
        while (timeAccumulator >= Option.Process.FIXED_DT) {
            ekfEstimator.predict(filteredAccel, Option.Process.FIXED_DT)
            timeAccumulator -= Option.Process.FIXED_DT
        }
        
        speedIMU = ekfEstimator.velocity
        updateSpeedStatistics(ekfEstimator.velocity)
    }

    /**
     * Updates rolling averages and median filtering for speed.
     * 
     * @param currentSpeed The latest estimated velocity.
     */
    private fun updateSpeedStatistics(currentSpeed: Float) {
        val cleanSpeed = if (currentSpeed < Option.Process.DEAD_ZONE_SPEED) 0f else currentSpeed

        for (i in 0 until speedHistory.size - 1) {
            speedHistory[i] = speedHistory[i + 1]
        }
        speedHistory[speedHistory.size - 1] = cleanSpeed
        averageSpeed = speedHistory.average().toFloat()

        val windowSize = Option.Process.MEDIAN_WINDOW_SIZE.coerceAtMost(speedHistory.size)
        val recentSpeeds = speedHistory.takeLast(windowSize).sorted()
        sog = recentSpeeds[recentSpeeds.size / 2]
    }

    /**
     * Resets GPS-dependent values if signal is lost for too long.
     */
    private fun checkGpsTimeout() {
        if (lastGpsUpdateMillis != 0L && System.currentTimeMillis() - lastGpsUpdateMillis > Option.Process.GPS_TIMEOUT_MS) {
            speedGPS = 0f
            lastGpsUpdateMillis = 0L
        }
    }

    /**
     * Integrates new GPS data into the EKF and updates navigation state.
     * 
     * @param lat Latitude.
     * @param lon Longitude.
     * @param alt Altitude.
     * @param gpsS Speed from GPS (m/s).
     * @param gpsBearing Bearing from GPS (degrees).
     * @param accuracy Horizontal accuracy (m).
     * @param onUpdate Callback to notify UI of changes.
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
        
        updateSpeedStatistics(ekfEstimator.velocity)
        onUpdate()
    }

    /**
     * Updates the smoothed azimuth using the latest compass reading.
     * 
     * @param newAzimuth Latest raw azimuth in degrees.
     * @param onUpdate Callback to notify UI.
     */
    fun updateOrientation(newAzimuth: Float, onUpdate: () -> Unit) {
        var diffAzimuth: Float = newAzimuth - averageAzimuth

        while (diffAzimuth < -Option.Process.HALF_CIRCLE_DEGREES) diffAzimuth += Option.Process.FULL_CIRCLE_DEGREES
        while (diffAzimuth > Option.Process.HALF_CIRCLE_DEGREES) diffAzimuth -= Option.Process.FULL_CIRCLE_DEGREES

        averageAzimuth = (averageAzimuth + Option.Process.AZIMUTH_ALPHA * diffAzimuth)

        if (averageAzimuth < 0) averageAzimuth += Option.Process.FULL_CIRCLE_DEGREES
        if (averageAzimuth >= Option.Process.FULL_CIRCLE_DEGREES) averageAzimuth -= Option.Process.FULL_CIRCLE_DEGREES
        azimuth = round(averageAzimuth)
        onUpdate()
    }

    /**
     * Resets all internal buffers and filters to their initial state.
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
        averageSpeed = 0f
        altitude = 0.0
        gpsAccuracy = 0f
        lastGpsUpdateMillis = 0L
    }
}
