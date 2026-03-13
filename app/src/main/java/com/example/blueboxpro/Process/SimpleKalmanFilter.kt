/**
 * This file contains Kalman filter implementations used for sensor fusion and smoothing.
 * It includes a 1D SimpleKalmanFilter and an EkfSpeedEstimator for velocity and bias tracking.
 */
package com.example.blueboxpro.Process

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
 */
class EkfSpeedEstimator(
    private val qVel: Float = 0.001f,
    private val qBias: Float = 0.0001f
) {
    // State: [velocity, bias]
    var velocity = 0f
        private set
    var bias = 0f
        private set

    // Covariance matrix P
    private var pVv = 1f
    private var pVb = 0f
    private var pBv = 0f
    private var pBb = 1f

    /**
     * Prediction Step: updates state and covariance based on measured acceleration and time delta.
     * Formula: v_k+1 = v_k + (a_mes - bias) * dt
     */
    fun predict(aMes: Float, dt: Float) {
        // State transition
        velocity += (aMes - bias) * dt
        
        // Covariance extrapolation: P = FPF' + Q
        // Jacobian F = [1, -dt; 0, 1]
        val pVvNew = pVv - dt * pBv - dt * (pVb - dt * pBb) + qVel
        val pVbNew = pVb - dt * pBb
        val pBvNew = pBv - dt * pBb
        val pBbNew = pBb + qBias

        pVv = pVvNew
        pVb = pVbNew
        pBv = pBvNew
        pBb = pBbNew
        
        // Ensure non-negative velocity
        if (velocity < 0f) velocity = 0f
    }

    /**
     * Update Step: corrects state and covariance using a reference velocity (e.g., from GPS).
     */
    fun update(refVelocity: Float, rMeasurement: Float) {
        // Innovation
        val y = refVelocity - velocity
        
        // Innovation Covariance S = HPH' + R where H = [1, 0]
        val s = pVv + rMeasurement
        
        // Kalman Gain K = PH' / S
        val kV = pVv / s
        val kB = pBv / s
        
        // Update State
        velocity += kV * y
        bias += kB * y
        
        // Update Covariance P = (I - KH)P
        val pVvOld = pVv
        val pVbOld = pVb
        pVv = (1f - kV) * pVvOld
        pVb = (1f - kV) * pVbOld
        pBv = -kB * pVvOld + pBv
        pBb = -kB * pVbOld + pBb
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
    }
}
