/**
 * This file contains Kalman filter implementations used for sensor fusion and smoothing.
 * It includes a 1D SimpleKalmanFilter for general smoothing and an EkfSpeedEstimator 
 * specialized for velocity and accelerometer bias tracking.
 */
package com.example.blueboxpro.Process

import kotlin.math.abs
import com.example.blueboxpro.Option

/**
 * A basic 1D Kalman Filter used for smoothing one-dimensional sensor data.
 * 
 * @param q Process noise (prediction uncertainty).
 * @param r Measurement noise (sensor uncertainty).
 * @param p Error covariance (initial estimate uncertainty).
 * @param x Initial estimated value.
 */
class SimpleKalmanFilter(
    var q: Float = 0.1f,
    var r: Float = 0.5f,
    var p: Float = 1.0f,
    var x: Float = 0.0f
) {
    /**
     * Prediction step: updates the estimate based on acceleration and time delta.
     * Use this when you have a known input (acceleration) affecting the state.
     * 
     * @param acceleration The input acceleration in m/s².
     * @param dt Time interval since last update.
     */
    fun predict(acceleration: Float, dt: Float) {
        x += acceleration * dt
        p += q
    }

    /**
     * Update step: corrects the current estimate using a new sensor measurement.
     * 
     * @param measurement The new raw sensor value.
     */
    fun update(measurement: Float) {
        val k = p / (p + r)
        x += k * (measurement - x)
        p *= (1 - k)
    }
}

/**
 * Extended Kalman Filter (EKF) specialized for estimating velocity and accelerometer bias.
 * 
 * Tracks a state vector consisting of [velocity, bias].
 * The bias term allows the filter to learn and compensate for accelerometer offsets over time.
 * 
 * @param qVel Process noise for the velocity state.
 * @param qBias Process noise for the bias state.
 * @param gateThreshold Maximum allowed innovation (m/s) to reject GPS outliers.
 */
class EkfSpeedEstimator(
    private val qVel: Float = 0.001f,
    private val qBias: Float = 0.0001f,
    private val gateThreshold: Float = 5.0f
) {
    /** Current estimated velocity in m/s. */
    var velocity = 0f
        private set
        
    /** Current estimated accelerometer bias in m/s². */
    var bias = 0f
        private set

    /** Injected GPS accuracy for ZUPT validation. */
    var currentGpsAccuracy: Float = Float.MAX_VALUE
    
    /** Injected GPS speed for ZUPT validation. */
    var currentGpsSpeed: Float = 0f

    // Covariance matrix P elements
    private var pVv = 1f
    private var pVb = 0f
    private var pBv = 0f
    private var pBb = 1f

    // Zero Velocity Update (ZUPT) tracking
    private var stillTimeCounter = 0f
    
    companion object {
        private const val ZUPT_ACCEL_THRESHOLD = 0.08f
        private const val ZUPT_TIME_REQUIRED = 0.8f
        private const val MIN_COVARIANCE = 1e-6f
        private const val ZUPT_COVARIANCE = 1e-4f
    }

    /**
     * Predicts the next state using measured acceleration.
     * 
     * @param aMes Raw acceleration measured in the forward direction (m/s²).
     * @param dt Time step since the last prediction (seconds).
     */
    fun predict(aMes: Float, dt: Float) {
        // Apply ZUPT if the device is stationary
        if (abs(aMes) < ZUPT_ACCEL_THRESHOLD) {
            val isStopped = if (currentGpsAccuracy < Option.Process.MAX_ACCEPTABLE_ACCURACY) {
                currentGpsSpeed < Option.Process.ZUPT_SPEED_THRESHOLD
            } else {
                velocity < Option.Process.ZUPT_SPEED_THRESHOLD
            }

            if (isStopped) {
                stillTimeCounter += dt
                if (stillTimeCounter > ZUPT_TIME_REQUIRED) {
                    applyZupt()
                    return
                }
            } else {
                stillTimeCounter = 0f
            }
        } else {
            stillTimeCounter = 0f
        }

        // State update: v = v + (a - bias) * dt
        velocity += (aMes - bias) * dt
        
        // Covariance update: P = FPF' + Q
        val pVvNew = pVv - dt * pBv - dt * (pVb - dt * pBb) + qVel
        val pVbNew = pVb - dt * pBb
        val pBvNew = pBv - dt * pBb
        val pBbNew = pBb + qBias

        pVv = kotlin.math.max(MIN_COVARIANCE, pVvNew)
        pVb = pVbNew
        pBv = pBvNew
        pBb = kotlin.math.max(MIN_COVARIANCE, pBbNew)
        
        velocity = velocity.coerceAtLeast(0f)
    }

    /**
     * Updates the internal state using an external velocity reference (e.g., GPS).
     * The confidence given to the GPS depends on its accuracy and the difference (innovation).
     * 
     * @param refVelocity The reference velocity in m/s.
     * @param rMeasurement The expected measurement noise covariance.
     */
    fun update(refVelocity: Float, rMeasurement: Float) {
        val y = refVelocity - velocity
        var actualR = rMeasurement

        // Adaptive Innovation Gating & Weighting based on User Rules
        if (currentGpsAccuracy <= Option.Process.MAX_ACCEPTABLE_ACCURACY) {
            // GPS is GOOD
            if (abs(y) > gateThreshold) {
                // Large Difference: IMU missed a constant speed.
                // WE FORCE KALMAN TO TRUST GPS (minimize measurement noise R)
                actualR = 0.001f
            } else {
                // Small Difference: IMU is performing well.
                // We let IMU breathe to smooth micro-variations.
                // Leave actualR as calculated (or optionally increase it slightly)
            }
        } else {
            // GPS is BAD
            if (abs(y) > gateThreshold * 1.5f) { // slightly wider gate for bad GPS
                // Large Difference & Bad GPS: Probable glitch. Reject entirely.
                return 
            }
            // Small Difference & Bad GPS: Process normally, giving room to IMU.
        }
        
        val s = pVv + actualR
        val kV = pVv / s
        val kB = pBv / s
        
        velocity += kV * y
        bias += kB * y
        
        val pVvOld = pVv
        val pVbOld = pVb
        pVv = kotlin.math.max(MIN_COVARIANCE, (1f - kV) * pVvOld)
        pVb = (1f - kV) * pVbOld
        pBv = -kB * pVvOld + pBv
        pBb = kotlin.math.max(MIN_COVARIANCE, -kB * pVbOld + pBb)
    }

    /**
     * Internal: Forces velocity to zero when stationarity is detected.
     */
    private fun applyZupt() {
        velocity = 0f
        pVv = ZUPT_COVARIANCE
    }

    /**
     * Resets the filter state and covariance to initial conditions.
     * 
     * @param initialVelocity Optional initial velocity to start with.
     */
    fun reset(initialVelocity: Float = 0f) {
        velocity = initialVelocity
        bias = 0f
        pVv = 1f
        pVb = 0f
        pBv = 0f
        pBb = 1f
        stillTimeCounter = 0f
        currentGpsAccuracy = Float.MAX_VALUE
        currentGpsSpeed = initialVelocity
    }
}
