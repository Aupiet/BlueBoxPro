/**
 * This page provides a full-screen map view with manual recentering capability.
 * It uses the reusable MapContainer from MapComponents.
 */
package com.example.blueboxpro.pages

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
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
    
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val result = processor?.getResult(unitSystem)

    Box(modifier = Modifier.fillMaxSize()) {
        key(recenterTrigger) {
            MapComponents.MapContainer(
                location = location,
                modifier = Modifier.fillMaxSize(),
                isLocked = false,
                autoCenter = (recenterTrigger == 0)
            )
        }
        
        // Info Overlays
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = if (isLandscape) 8.dp else 48.dp)
                .padding(horizontal = 16.dp),
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
                    
                    // Wind Speed & Direction
                    val unitSystemEnum = Converter.getUnitSystem(unitSystem)
                    val windSpeedKmh = weatherData.currentWeather.windSpeed.toFloat()
                    val convertedWindSpeed = Converter.convertSpeed(windSpeedKmh / 3.6f, unitSystemEnum)
                    
                    WeatherChipWithArrow(
                        speedText = "%.1f".format(convertedWindSpeed),
                        unitText = result?.getSpeedUnit() ?: "km/h",
                        angle = weatherData.currentWeather.windDirection.toFloat()
                    )
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
private fun WeatherChipWithArrow(speedText: String, unitText: String, angle: Float) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "$speedText $unitText",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(18.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = Color.Gray.copy(alpha = 0.3f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()))
                }
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp).rotate(angle - 90f),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}
