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

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {
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
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { loc ->
                processor.updateWithGPS(
                    loc.latitude, 
                    loc.longitude,
                    loc.altitude,
                    if (loc.hasSpeed()) loc.speed else 0f,
                    onDataUpdated
                )
            }
        }
    }

    fun start() {
        val linearAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        sensorManager.registerListener(sensorEventListener, linearAccel, SensorManager.SENSOR_DELAY_GAME)
        
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
