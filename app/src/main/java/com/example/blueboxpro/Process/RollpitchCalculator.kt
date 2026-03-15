/**
 * Utility object dedicated to calculating the device's attitude (inclination).
 * It computes the pitch and roll angles using sensor data fusion.
 */
package com.example.blueboxpro.Process

import android.hardware.SensorManager
import kotlin.math.roundToInt

/**
 * Provides mathematical functions to derive pitch and roll from gravity and geomagnetic sensors.
 */
object RollpitchCalculator {
    private const val RAD_TO_DEG = 57.2957795131 // (180 / PI)
    private const val ROTATION_MATRIX_SIZE = 9
    private const val ORIENTATION_ARRAY_SIZE = 3

    /**
     * Calculates the Pitch and Roll from raw sensor data.
     * 
     * @param gravity Accelerometer data (3 floats).
     * @param geomagnetic Magnetometer data (3 floats).
     * @return A pair of integers (Pitch, Roll) or null if the calculation fails.
     */
    fun calculatePitchRoll(gravity: FloatArray, geomagnetic: FloatArray): Pair<Int, Int>? {
        val r = FloatArray(ROTATION_MATRIX_SIZE)
        val i = FloatArray(ROTATION_MATRIX_SIZE)

        // Attempt to generate the rotation matrix
        return if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
            val orientation = FloatArray(ORIENTATION_ARRAY_SIZE)
            SensorManager.getOrientation(r, orientation)

            // orientation[1] is the pitch, orientation[2] is the roll
            val pitch = (orientation[1] * RAD_TO_DEG).roundToInt()
            val roll = (orientation[2] * RAD_TO_DEG).roundToInt()

            Pair(pitch, roll)
        } else {
            null
        }
    }
}
