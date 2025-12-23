package com.example.blueboxpro

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.blueboxpro.ui.theme.BlueBoxProTheme
import com.google.android.gms.location.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.math.sqrt

// Simple Kalman Filter for 1D values (like speed)
class SimpleKalmanFilter(
    var q: Float = 0.1f, // Process noise
    var r: Float = 0.5f, // Measurement noise
    var p: Float = 1.0f, // Error covariance
    var x: Float = 0.0f  // Initial value
) {
    fun predict(acceleration: Float, dt: Float) {
        x += acceleration * dt
        p += q
    }

    fun update(measurement: Float) {
        val k = p / (p + r)
        x += k * (measurement - x)
        p *= (1 - k)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Configuration.getInstance().load(
            applicationContext,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        enableEdgeToEdge()
        setContent {
            BlueBoxProTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "page1",
        modifier = modifier
    ) {
        composable("page1") {
            Page1(onNavigateToMap = { navController.navigate("page2") })
        }
        composable("page2") {
            Page2(onBack = { navController.popBackStack() })
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun Page1(onNavigateToMap: () -> Unit) {
    val context = LocalContext.current
    var accelX by remember { mutableStateOf(0f) }
    var accelY by remember { mutableStateOf(0f) }
    var accelZ by remember { mutableStateOf(0f) }
    var speedIMU by remember { mutableStateOf(0f) }
    var speedGPS by remember { mutableStateOf(0f) }
    var speedFused by remember { mutableStateOf(0f) }
    var lastTimestamp by remember { mutableStateOf(0L) }

    // Kalman filters for 3 axes or just global speed
    val kalmanX = remember { SimpleKalmanFilter() }
    val kalmanY = remember { SimpleKalmanFilter() }
    val kalmanZ = remember { SimpleKalmanFilter() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val linearAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                    val dt = if (lastTimestamp != 0L) (event.timestamp - lastTimestamp) / 1_000_000_000f else 0f
                    lastTimestamp = event.timestamp

                    accelX = event.values[0]
                    accelY = event.values[1]
                    accelZ = event.values[2]

                    // Predict step of Kalman
                    kalmanX.predict(accelX, dt)
                    kalmanY.predict(accelY, dt)
                    kalmanZ.predict(accelZ, dt)

                    // Calculate IMU speed (norm)
                    speedIMU = sqrt(kalmanX.x * kalmanX.x + kalmanY.x * kalmanY.x + kalmanZ.x * kalmanZ.x)
                    speedFused = speedIMU
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { loc ->
                    if (loc.hasSpeed()) {
                        speedGPS = loc.speed
                        // Update step of Kalman using GPS speed as measurement
                        // We approximate the update by scaling the vector
                        val currentIMUSpeed = sqrt(kalmanX.x * kalmanX.x + kalmanY.x * kalmanY.x + kalmanZ.x * kalmanZ.x)
                        if (currentIMUSpeed > 0.1f) {
                            val ratio = speedGPS / currentIMUSpeed
                            kalmanX.update(kalmanX.x * ratio)
                            kalmanY.update(kalmanY.x * ratio)
                            kalmanZ.update(kalmanZ.x * ratio)
                        } else {
                            // If stopped, force update toward zero
                            kalmanX.update(0f)
                            kalmanY.update(0f)
                            kalmanZ.update(0f)
                        }
                    }
                }
            }
        }

        sensorManager.registerListener(sensorEventListener, linearAccel, SensorManager.SENSOR_DELAY_GAME)
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Analyse de mouvement (Page 1)", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = "Accélération (m/s²):", style = MaterialTheme.typography.titleMedium)
        Text(text = "X: ${"%.2f".format(accelX)} | Y: ${"%.2f".format(accelY)} | Z: ${"%.2f".format(accelZ)}")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "Vitesse (m/s):", style = MaterialTheme.typography.titleMedium)
        Text(text = "IMU (Brute): ${"%.2f".format(speedIMU)}")
        Text(text = "GPS: ${"%.2f".format(speedGPS)}")
        Text(text = "Fusionnée (Kalman): ${"%.2f".format(speedFused)}", color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = { 
            kalmanX.x = 0f; kalmanY.x = 0f; kalmanZ.x = 0f 
            speedIMU = 0f; speedGPS = 0f; speedFused = 0f
        }) {
            Text("Réinitialiser")
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onNavigateToMap) {
            Text("Aller à la Carte (Page 2)")
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun Page2(onBack: () -> Unit) {
    val context = LocalContext.current
    var location by remember { mutableStateOf<GeoPoint?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    DisposableEffect(Unit) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    location = GeoPoint(it.latitude, it.longitude)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (location != null) 
                    "Position : Lat ${"%.4f".format(location!!.latitude)}, Lon ${"%.4f".format(location!!.longitude)}"
                    else "Recherche de la position GPS...",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onBack) {
                Text("Retour à la Page 1")
            }
        }

        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                }
            },
            update = { mapView ->
                location?.let { geoPoint ->
                    mapView.controller.animateTo(geoPoint)
                    mapView.overlays.clear()
                    val marker = Marker(mapView)
                    marker.position = geoPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Vous êtes ici"
                    mapView.overlays.add(marker)
                    mapView.invalidate()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
