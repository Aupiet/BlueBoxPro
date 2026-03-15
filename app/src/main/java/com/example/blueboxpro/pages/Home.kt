/**
 * This page serves as the application's dashboard, displaying the current date/time,
 * real-time movement data (SOG/COG), statistics from the last recorded session,
 * and a quick-start recording button.
 */
package com.example.blueboxpro.pages

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.blueboxpro.Process.Converter
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.Process.WeatherData
import com.example.blueboxpro.Process.WeatherManager
import com.example.blueboxpro.R
import com.example.blueboxpro.Save.SessionManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * The landing page of the application (Home Page).
 */
@Composable
fun Page1(
    processor: MovementProcessor,
    refreshTrigger: Int,
    unitSystem: String,
    weatherData: WeatherData?,
    onNavigateToMap: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current
    
    val sessions = SessionManager.sessions
    val lastSession = sessions.lastOrNull()
    val isRecording = SessionManager.activeRecording != null
    val result = processor.getResult(unitSystem)

    // Date/Time State
    var currentTime by remember { mutableStateOf(Calendar.getInstance().time) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance().time
            kotlinx.coroutines.delay(1000)
        }
    }

    val dateFormatter = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isLandscape) Modifier else Modifier.verticalScroll(scrollState))
            .padding(if (isLandscape) 8.dp else HOME_PADDING_MEDIUM),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (isLandscape) Arrangement.SpaceEvenly else Arrangement.Top
    ) {
        // Unified Header Block
        CombinedHeaderCard(
            dateStr = dateFormatter.format(currentTime),
            timeStr = timeFormatter.format(currentTime),
            weatherData = weatherData,
            result = result,
            isLandscape = isLandscape
        )

        Spacer(modifier = Modifier.height(HOME_SPACING_SMALL))

        // Wind Information Block - Always shown
        WindInfoCard(weatherData, unitSystem, isLandscape)
        
        Spacer(modifier = Modifier.height(HOME_SPACING_SMALL))

        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HOME_SPACING_MEDIUM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.last_session_header), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LastSessionCard(lastSession, isLandscape = true)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.start_new_session_label), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    StartRecordingCard(isRecording, sessions.size, context, isLandscape = true)
                }
            }
        } else {
            Spacer(modifier = Modifier.height(HOME_SPACING_LARGE))
            Text(text = stringResource(R.string.last_session_header), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(HOME_SPACING_SMALL))
            LastSessionCard(lastSession)
            Spacer(modifier = Modifier.height(HOME_SPACING_MEDIUM))
            StartRecordingCard(isRecording, sessions.size, context)
        }
    }
}

@Composable
private fun WindInfoCard(
    weatherData: WeatherData?,
    unitSystemStr: String,
    isLandscape: Boolean
) {
    val unitSystem = Converter.getUnitSystem(unitSystemStr)
    
    // Create a temporary result to get the unit string correctly
    val resultTemplate = com.example.blueboxpro.Process.MovementResult(
        unitSystem = unitSystem,
        accelX = 0f, accelY = 0f, accelZ = 0f,
        speedIMU = 0f, speedGPS = 0f, speedFused = 0f,
        averageSpeed = 0f, sog = 0f, cog = 0f, azimuth = 0f, altitude = 0.0, accuracy = 0f, pitch = 0, roll = 0
    )
    val speedUnit = resultTemplate.getSpeedUnit()

    Card(
        modifier = Modifier.fillMaxWidth(if (isLandscape) 0.9f else 1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        if (weatherData != null) {
            val windSpeedKmh = weatherData.currentWeather.windSpeed.toFloat()
            val windSpeedMs = windSpeedKmh / 3.6f
            val convertedWindSpeed = Converter.convertSpeed(windSpeedMs, unitSystem)
            val windDir = weatherData.currentWeather.windDirection
            val cardinal = WeatherManager.getWindDirectionCardinal(windDir)

            Row(
                modifier = Modifier.padding(if (isLandscape) 8.dp else 12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "VENT", style = MaterialTheme.typography.labelSmall)
                    Text(text = "%.1f %s".format(convertedWindSpeed, speedUnit), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = cardinal, style = MaterialTheme.typography.bodySmall)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "DIRECTION", style = MaterialTheme.typography.labelSmall)
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(if (isLandscape) 40.dp else 50.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = Color.Gray.copy(alpha = 0.3f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
                        }
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(if (isLandscape) 24.dp else 30.dp).rotate(windDir.toFloat() - 90f),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Text(text = "%.0f°".format(windDir), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VENT : En attente...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun CombinedHeaderCard(
    dateStr: String,
    timeStr: String,
    weatherData: WeatherData?,
    result: com.example.blueboxpro.Process.MovementResult,
    isLandscape: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(if (isLandscape) 0.9f else 1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(if (isLandscape) 8.dp else HOME_PADDING_MEDIUM).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = dateStr, style = if (isLandscape) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge)
            Text(text = timeStr, style = if (isLandscape) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
            
            val weatherLabel = if (weatherData != null) {
                val description = WeatherManager.getWeatherDescription(weatherData.currentWeather.weatherCode)
                "$description, ${weatherData.currentWeather.temperature.toInt()}°C"
            } else {
                "En attente..."
            }
            
            Text(
                text = weatherLabel, 
                style = if (isLandscape) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyLarge,
                color = if (weatherData == null) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f) else Color.Unspecified
            )
            
            Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else HOME_SPACING_MEDIUM))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "SOG", style = MaterialTheme.typography.labelSmall)
                    Text(text = "%.1f %s".format(result.getSog(), result.getSpeedUnit()), style = if (isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "COG", style = MaterialTheme.typography.labelSmall)
                    Text(text = "%.0f°".format(result.getCog()), style = if (isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

@Composable
private fun LastSessionCard(lastSession: com.example.blueboxpro.Save.Session?, isLandscape: Boolean = false) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        if (lastSession != null) {
            Column(modifier = Modifier.padding(if (isLandscape) 8.dp else HOME_PADDING_MEDIUM)) {
                Text(text = lastSession.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(if (isLandscape) 2.dp else 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = lastSession.date, style = MaterialTheme.typography.labelSmall)
                    Text(text = lastSession.duration, style = MaterialTheme.typography.labelSmall)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = lastSession.distance, style = MaterialTheme.typography.labelSmall)
                    Text(text = lastSession.averageSpeed, style = MaterialTheme.typography.labelSmall)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().padding(if (isLandscape) 8.dp else HOME_PADDING_MEDIUM), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.no_saves), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun StartRecordingCard(isRecording: Boolean, sessionCount: Int, context: android.content.Context, isLandscape: Boolean = false) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(if (isLandscape) 8.dp else HOME_PADDING_MEDIUM), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            if (!isLandscape) {
                Text(text = if (isRecording) stringResource(R.string.recording_in_progress) else stringResource(R.string.start_new_session_label), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(HOME_SPACING_MEDIUM))
            }
            Button(onClick = { if (!isRecording) SessionManager.startRecording("Session ${sessionCount + 1}") else SessionManager.stopRecording(context) }, modifier = if (isLandscape) Modifier.fillMaxWidth().height(48.dp) else Modifier, colors = if (isRecording) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors()) {
                Text(text = if (isRecording) stringResource(R.string.stop_recording) else stringResource(R.string.start_recording), style = if (isLandscape) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelLarge)
            }
        }
    }
}

private val HOME_PADDING_MEDIUM = 16.dp
private val HOME_SPACING_SMALL = 8.dp
private val HOME_SPACING_MEDIUM = 16.dp
private val HOME_SPACING_LARGE = 24.dp
