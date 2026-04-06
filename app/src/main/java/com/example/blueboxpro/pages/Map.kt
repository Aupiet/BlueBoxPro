/**
 * This page provides a full-screen map view with manual recentering capability.
 * It uses the reusable MapContainer from MapComponents.
 */
package com.example.blueboxpro.pages

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blueboxpro.ui.components.MapComponents
import com.example.blueboxpro.R
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.Process.Converter
import com.example.blueboxpro.Process.WeatherData
import com.example.blueboxpro.Process.WeatherManager
import org.osmdroid.util.GeoPoint
import kotlin.math.cos
import kotlin.math.sin

/**
 * Composable that displays a full-screen interactive map.
 */
@Composable
fun Page4(
    location: GeoPoint?, 
    processor: MovementProcessor? = null,
    weatherData: WeatherData? = null,
    unitSystem: String = "Metric (km/h, m)",
    onBack: () -> Unit
) {
    var recenterTrigger by remember { mutableStateOf(0) }
    var showTopoLayer by rememberSaveable { mutableStateOf(false) }
    var showPrecipLayer by rememberSaveable { mutableStateOf(false) }
    var showWindLayer by rememberSaveable { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val result = processor?.getResult(unitSystem)

    Box(modifier = Modifier.fillMaxSize()) {
        MapComponents.MapContainer(
            location = location,
            modifier = Modifier.fillMaxSize(),
            isLocked = false,
            recenterTrigger = recenterTrigger,
            showTopoLayer = showTopoLayer,
            showPrecipLayer = showPrecipLayer,
            showWindLayer = showWindLayer
        )
        
        // Info Overlays
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = if (isLandscape) 4.dp else 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Wind Legend
            if (showWindLayer) {
                WindLegend(modifier = Modifier.fillMaxWidth())
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Speed and Course Card
            if (result != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MetricItem("%.1f".format(result.getSog()), result.getSpeedUnit())
                        VerticalDivider(modifier = Modifier.height(32.dp))
                        MetricItem("%.0f°".format(result.getCog()), "COG")
                    }
                }
            }

            // Weather Overlay (Circular chips)
            if (weatherData != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Temperature
                    WeatherChip(text = "${weatherData.currentWeather.temperature.toInt()}°C")
                    
                    // Wind Speed & Direction (Point on perimeter)
                    val unitSystemEnum = Converter.getUnitSystem(unitSystem)
                    val windSpeedKmh = weatherData.currentWeather.windSpeed.toFloat()
                    val convertedWindSpeed = Converter.convertSpeed(windSpeedKmh / 3.6f, unitSystemEnum)
                    
                    val dirPoints = listOf(MapComponents.DirectionPoint(angle = weatherData.currentWeather.windDirection.toFloat(), sizeDp = 3f))
                    MapComponents.CircularGaugeWithDirectionPoint(
                        label = "%s".format( result?.getSpeedUnit() ?: "km/h"),
                        value = "%.1f".format(convertedWindSpeed),
                        points = dirPoints,
                        gaugeSize = 40,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        labelFontSize = 0.sp,
                        valueFontSize = 12.sp,
                        circleStrokeWidth = 1.5f
                    )
                }
                }
            }
        }
        
        // FABs
        val fabPadding = if (isLandscape) 16.dp else 24.dp

        FloatingActionButton(
            onClick = onBack,
            modifier = Modifier
                .padding(fabPadding)
                .navigationBarsPadding()
                .align(Alignment.BottomStart),
            containerColor = MaterialTheme.colorScheme.secondary
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
        }

        FloatingActionButton(
            onClick = { recenterTrigger++ },
            modifier = Modifier
                .padding(fabPadding)
                .navigationBarsPadding()
                .align(Alignment.BottomEnd),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.content_desc_recenter))
        }
        // Layer toggle chips
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (isLandscape) 16.dp else 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = showTopoLayer,
                onClick = { showTopoLayer = !showTopoLayer },
                label = { Text(stringResource(R.string.layer_topo)) }
            )
            FilterChip(
                selected = showPrecipLayer,
                onClick = { showPrecipLayer = !showPrecipLayer },
                label = { Text(stringResource(R.string.layer_precip)) }
            )
            FilterChip(
                selected = showWindLayer,
                onClick = { showWindLayer = !showWindLayer },
                label = { Text(stringResource(R.string.layer_wind)) }
            )
        }
    }
}

@Composable
private fun MetricItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun WeatherChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f),
        shape = CircleShape,
        tonalElevation = 2.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
fun WindLegend(modifier: Modifier = Modifier) {
    val stops = listOf(
        Color(0, 150, 255), // 0
        Color(0, 200, 150), // 5
        Color(0, 220, 0),   // 10
        Color(150, 220, 0), // 15
        Color(220, 200, 0), // 20
        Color(255, 120, 0), // 25
        Color(255, 50, 0),  // 30
        Color(220, 0, 50),  // 35
        Color(150, 0, 150)  // 40+
    )
    
    Box(
        modifier = modifier
            .height(24.dp)
            .background(Brush.horizontalGradient(stops))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("kts 0", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            for (i in 5..45 step 5) {
                Text(i.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

