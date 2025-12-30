package com.example.blueboxpro.Process

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import com.google.android.gms.location.*

class CaptorListener(
    private val context: Context,
    private val processor: MovementProcessor,
    private val onDataUpdated: () -> Unit
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var lastTimestamp = 0L

    // Données pour la boussole
    private val gravityData = FloatArray(3)
    private val geomagneticData = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            when (event?.sensor?.type) {
                Sensor.TYPE_LINEAR_ACCELERATION -> {
                    val dt = if (lastTimestamp != 0L) (event.timestamp - lastTimestamp) / 1_000_000_000f else 0f
                    lastTimestamp = event.timestamp

                    processor.processAcceleration(
                        event.values[0],
                        event.values[1],
                        event.values[2],
                        dt
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

    private fun updateCompass() {
        if (hasGravity && hasGeomagnetic) {
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, gravityData, geomagneticData)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                
                // azimuth est en radians, conversion en degrés (0-360)
                var azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (azimuthDeg < 0) azimuthDeg += 360f
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

    fun start() {
        val linearAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        sensorManager.registerListener(sensorEventListener, linearAccel, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI)
        
        requestLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: Exception) { }
    }

    fun stop() {
        sensorManager.unregisterListener(sensorEventListener)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
