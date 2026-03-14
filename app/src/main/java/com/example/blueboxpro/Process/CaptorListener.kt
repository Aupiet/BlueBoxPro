/**
 * This class listens to Android sensor events (accelerometer, magnetometer, GPS) 
 * and forwards the data to the MovementProcessor for computation.
 * It manages the registration and unregistration of system sensor listeners.
 */
package com.example.blueboxpro.Process

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import com.google.android.gms.location.*

/**
 * Orchestrates sensor data collection.
 * 
 * @param context Android context for accessing system services.
 * @param processor The movement processor that will receive and filter raw data.
 * @param onDataUpdated Callback invoked whenever a sensor update has been processed.
 */
class CaptorListener(
    private val context: Context,
    private val processor: MovementProcessor,
    private val onDataUpdated: () -> Unit
) {
    companion object {
        private const val NANO_TO_SECOND = 1_000_000_000f
        private const val FULL_CIRCLE_DEGREES = 360f
        private const val LOCATION_INTERVAL_MS = 1000L
        private const val ROTATION_MATRIX_SIZE = 9
        private const val ORIENTATION_ARRAY_SIZE = 3
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var lastTimestamp = 0L

    private val gravityData = FloatArray(3)
    private val geomagneticData = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false
    private val rotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
    private var hasRotationMatrix = false

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            when (event?.sensor?.type) {
                Sensor.TYPE_LINEAR_ACCELERATION -> {
                    val dt = if (lastTimestamp != 0L) (event.timestamp - lastTimestamp) / NANO_TO_SECOND else 0f
                    lastTimestamp = event.timestamp

                    processor.processAcceleration(
                        event.values[0],
                        event.values[1],
                        event.values[2],
                        dt,
                        if (hasRotationMatrix) rotationMatrix.copyOf() else null
                    )
                    onDataUpdated()
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(event.values, 0, gravityData, 0, 3)
                    hasGravity = true
                    updateCompass()
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(event.values, 0, geomagneticData, 0, 3)
                    hasGeomagnetic = true
                    updateCompass()
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    /**
     * Computes the device's orientation using gravity and geomagnetic sensor data.
     * Updates the processor with the new azimuth.
     */
    private fun updateCompass() {
        if (hasGravity && hasGeomagnetic) {
            val i = FloatArray(ROTATION_MATRIX_SIZE)
            if (SensorManager.getRotationMatrix(rotationMatrix, i, gravityData, geomagneticData)) {
                hasRotationMatrix = true
                val orientation = FloatArray(ORIENTATION_ARRAY_SIZE)
                SensorManager.getOrientation(rotationMatrix, orientation)
                
                var azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (azimuthDeg < 0) azimuthDeg += FULL_CIRCLE_DEGREES
                processor.updateOrientation(azimuthDeg, onDataUpdated)
            }
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { loc ->
                processor.updateWithGPS(
                    lat = loc.latitude, 
                    lon = loc.longitude,
                    alt = loc.altitude,
                    gpsS = if (loc.hasSpeed()) loc.speed else 0f,
                    gpsBearing = if (loc.hasBearing()) loc.bearing else 0f,
                    accuracy = loc.accuracy,
                    onUpdate = onDataUpdated
                )
            }
        }
    }

    /**
     * Registers sensor listeners for linear acceleration, gravity, and magnetic field.
     * Also initiates periodic GPS location updates.
     */
    fun start() {
        val linearAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        sensorManager.registerListener(sensorEventListener, linearAccel, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI)
        
        requestLocationUpdates()
    }

    /**
     * Configures and starts high-accuracy location tracking.
     */
    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL_MS).build()
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: Exception) { 
            // Silent fail if permission not granted during this specific call
        }
    }

    /**
     * Unregisters all sensor listeners and stops GPS updates.
     * Call this when the monitoring activity is destroyed or stopped.
     */
    fun stop() {
        sensorManager.unregisterListener(sensorEventListener)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
