/**
 * This file contains Kalman filter implementations used for sensor fusion and smoothing.
 * It includes a 1D SimpleKalmanFilter and an EkfSpeedEstimator for velocity and bias tracking.
 */
package com.example.blueboxpro.Process

import kotlin.math.abs

/**
 * A basic 1D Kalman Filter used for smoothing sensor data.
 */
class SimpleKalmanFilter(
    var q: Float = 0.1f, // Process noise
    var r: Float = 0.5f, // Measurement noise
    var p: Float = 1.0f, // Error covariance
    var x: Float = 0.0f  // Estimated value
) {
    /**
     * Prediction step: updates the estimate based on acceleration and time delta.
     */
    fun predict(acceleration: Float, dt: Float) {
        x += acceleration * dt
        p += q
    }

    /**
     * Update step: corrects the estimate using a new measurement.
     */
    fun update(measurement: Float) {
        val k = p / (p + r)
        x += k * (measurement - x)
        p *= (1 - k)
    }
}

/**
 * Extended Kalman Filter specialized for estimating velocity and accelerometer bias.
 * State vector: [velocity, bias]
 * 
 * Refactored for better numerical stability and sensor glitch protection.
 */
class EkfSpeedEstimator(
    private val qVel: Float = 0.001f,
    private val qBias: Float = 0.0001f,
    private val gateThreshold: Float = 5.0f // Innovation Gating Threshold (m/s)
) {
    // State: [velocity (m/s), bias (m/s²)]
    var velocity = 0f
        private set
    var bias = 0f
        private set

    // Covariance matrix P (Diagonal entries must remain positive)
    private var pVv = 1f
    private var pVb = 0f
    private var pBv = 0f
    private var pBb = 1f

    // ZUPT (Zero Velocity Update) logic variables
    private var stillTimeCounter = 0f
    private val ZUPT_ACCEL_THRESHOLD = 0.08f // m/s² (low = only true stillness triggers ZUPT)
    private val ZUPT_TIME_REQUIRED = 0.8f    // seconds of stillness before forcing zero

    /**
     * Prediction Step: updates state and covariance based on measured acceleration and time delta.
     * Formula: v_k+1 = v_k + (a_mes - bias) * dt
     * 
     * @param aMes Raw acceleration (Linear Acceleration preferred)
     * @param dt Precision time delta in seconds
     */
    fun predict(aMes: Float, dt: Float) {
        // 1. ZUPT check: If acceleration is negligible, track time to force zero velocity
        if (abs(aMes) < ZUPT_ACCEL_THRESHOLD) {
            stillTimeCounter += dt
            if (stillTimeCounter > ZUPT_TIME_REQUIRED) {
                applyZupt()
                return
            }
        } else {
            stillTimeCounter = 0f
        }

        // 2. State transition (Euler Integration)
        // v = v + (a - bias) * dt
        velocity += (aMes - bias) * dt
        
        // 3. Covariance extrapolation: P = FPF' + Q
        // Jacobian F = [1, -dt; 0, 1]
        val dt2 = dt * dt
        val pVvNew = pVv - dt * pBv - dt * (pVb - dt * pBb) + qVel
        val pVbNew = pVb - dt * pBb
        val pBvNew = pBv - dt * pBb
        val pBbNew = pBb + qBias

        // Numerical stability: Ensure diagonal P entries remain positive
        pVv = kotlin.math.max(1e-6f, pVvNew)
        pVb = pVbNew
        pBv = pBvNew
        pBb = kotlin.math.max(1e-6f, pBbNew)
        
        // Clamp to zero for display purposes (filter dynamics already applied)
        velocity = velocity.coerceAtLeast(0f)
    }

    /**
     * Update Step: corrects state and covariance using a reference velocity (GPS).
     * Includes Innovation Gating to protect against GPS glitches.
     */
    fun update(refVelocity: Float, rMeasurement: Float) {
        // 1. Innovation (Difference between GPS and prediction)
        val y = refVelocity - velocity

        // 2. Innovation Gating: Reject GPS measurements that are physically impossible
        // If the gap is > 5m/s (18km/h) in a single update, we ignore it as a glitch.
        if (abs(y) > gateThreshold) {
            return 
        }
        
        // 3. Innovation Covariance S = HPH' + R where H = [1, 0]
        val s = pVv + rMeasurement
        
        // 4. Kalman Gain K = PH' / S
        val kV = pVv / s
        val kB = pBv / s
        
        // 5. Update State
        velocity += kV * y
        bias += kB * y
        
        // 6. Update Covariance P = (I - KH)P
        val pVvOld = pVv
        val pVbOld = pVb
        pVv = kotlin.math.max(1e-6f, (1f - kV) * pVvOld)
        pVb = (1f - kV) * pVbOld
        pBv = -kB * pVvOld + pBv
        pBb = kotlin.math.max(1e-6f, -kB * pVbOld + pBb)
    }

    /**
     * Forces the velocity to zero and resets the bias tracker.
     * Prevents drift when the device is known to be stationary.
     */
    private fun applyZupt() {
        velocity = 0f
        // Reduce covariance as we are certain of the state
        pVv = 1e-4f 
        // We don't reset bias completely to keep the learned offset
    }

    /**
     * Resets the filter to initial values.
     */
    fun reset() {
        velocity = 0f
        bias = 0f
        pVv = 1f
        pVb = 0f
        pBv = 0f
        pBb = 1f
        stillTimeCounter = 0f
    }
}
