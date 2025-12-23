package com.example.blueboxpro

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.blueboxpro.ui.theme.BlueBoxProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlueBoxProTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SpeedDisplay(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SpeedDisplay(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var vx by remember { mutableStateOf(0f) }
    var vy by remember { mutableStateOf(0f) }
    var vz by remember { mutableStateOf(0f) }
    var lastTimestamp by remember { mutableStateOf(0L) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val linearAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                    if (lastTimestamp != 0L) {
                        // Temps écoulé en secondes (nanosecondes -> secondes)
                        val dt = (event.timestamp - lastTimestamp) / 1_000_000_000f
                        
                        // v = v + a * dt
                        vx += event.values[0] * dt
                        vy += event.values[1] * dt
                        vz += event.values[2] * dt
                    }
                    lastTimestamp = event.timestamp
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            sensorEventListener,
            linearAccel,
            SensorManager.SENSOR_DELAY_GAME
        )

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Vitesse estimée (m/s) :",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "VX: ${"%.2f".format(vx)} m/s")
        Text(text = "VY: ${"%.2f".format(vy)} m/s")
        Text(text = "VZ: ${"%.2f".format(vz)} m/s")
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = {
            vx = 0f
            vy = 0f
            vz = 0f
        }) {
            Text("Réinitialiser la vitesse")
        }
        
        Text(
            text = "Note: La précision dépend des capteurs et la dérive est rapide.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
