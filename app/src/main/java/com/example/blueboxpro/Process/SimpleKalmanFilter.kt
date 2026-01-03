/**
 * A basic 1D Kalman Filter used for smoothing sensor data.
 */
package com.example.blueboxpro.Process

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
