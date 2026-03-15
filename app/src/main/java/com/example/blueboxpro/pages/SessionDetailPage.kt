/**
 * This page displays detailed information about a specific recorded session.
 * It includes a map trace, speed statistics, interactive charts for SOG and Altitude,
 * and point-level details when a marker is tapped.
 */
package com.example.blueboxpro.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blueboxpro.Option
import com.example.blueboxpro.Process.Converter
import com.example.blueboxpro.Process.MovementResult
import com.example.blueboxpro.R
import com.example.blueboxpro.Save.GpsPoint
import com.example.blueboxpro.Save.Session
import com.example.blueboxpro.Save.SessionManager
import com.example.blueboxpro.ui.components.MapComponents
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Detailed view for a specific session.
 * 
 * @param session The session to display.
 * @param unitSystem Current unit system for conversion.
 * @param onBack Callback for back navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailPage(
    session: Session?,
    unitSystem: String = Option.UI.unitSystem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedPointIndex by remember { mutableIntStateOf(-1) }

    if (showDeleteDialog && session != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_session_title)) },
            text = { Text(stringResource(R.string.delete_session_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        SessionManager.deleteSession(context, session)
                        showDeleteDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete_label))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel_label))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.name ?: stringResource(R.string.session_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.content_desc_back)
                        )
                    }
                },
                actions = {
                    if (session != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.content_desc_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (session == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.session_not_found))
            }
            return@Scaffold
        }

        val scrollState = rememberScrollState()
        val points = session.points

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = PADDING_MEDIUM),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SessionInfoHeader(session)

            Spacer(modifier = Modifier.height(SPACING_MEDIUM))

            if (points.isNotEmpty()) {
                SpeedStatisticsCard(points, unitSystem)

                Spacer(modifier = Modifier.height(SPACING_MEDIUM))

                SectionHeader(stringResource(R.string.header_gps_trace))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MAP_HEIGHT)
                        .clip(RoundedCornerShape(MAP_CORNER_RADIUS))
                ) {
                    MapComponents.SessionMapContainer(
                        points = points,
                        unitSystem = unitSystem,
                        modifier = Modifier.fillMaxSize(),
                        selectedPointIndex = selectedPointIndex,
                        onPointTapped = { idx -> selectedPointIndex = idx }
                    )
                }

                if (selectedPointIndex in points.indices) {
                    Spacer(modifier = Modifier.height(SPACING_SMALL))
                    SelectedPointCard(points[selectedPointIndex], unitSystem)
                }

                Spacer(modifier = Modifier.height(SPACING_MEDIUM))

                SectionHeader(stringResource(R.string.header_speed_chart))
                SpeedChart(points, unitSystem)

                Spacer(modifier = Modifier.height(SPACING_MEDIUM))

                SectionHeader(stringResource(R.string.header_altitude_chart))
                AltitudeChart(points, unitSystem)

                Spacer(modifier = Modifier.height(SPACING_LARGE))
            } else {
                Spacer(modifier = Modifier.height(SPACING_LARGE))
                Text(
                    text = stringResource(R.string.no_points_recorded),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SessionInfoHeader(session: Session) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(PADDING_MEDIUM)) {
            Text(text = session.name, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(SPACING_SMALL))
            Text(text = "${stringResource(R.string.label_date)} ${session.date}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "${stringResource(R.string.duration_label)} ${session.duration}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "${stringResource(R.string.distance_label)} ${session.distance}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "${stringResource(R.string.label_avg_speed)} ${session.averageSpeed}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(R.string.label_points_count, session.points.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SpeedStatisticsCard(points: List<GpsPoint>, unitSystemStr: String) {
    val unitSystem = Converter.getUnitSystem(unitSystemStr)
    val resultTemplate = MovementResult(
        unitSystem = unitSystem,
        accelX = 0f, accelY = 0f, accelZ = 0f,
        speedIMU = 0f, speedGPS = 0f, speedFused = 0f,
        averageSpeed = 0f, sog = 0f, cog = 0f, azimuth = 0f, altitude = 0.0, accuracy = 0f, pitch = 0, roll = 0
    )
    
    val speedValues = points.map { Converter.convertSpeed(it.sog, unitSystem) }
    val avgSpeed = speedValues.average().toFloat()
    val maxSpeed = speedValues.maxOrNull() ?: 0f
    val minSpeed = speedValues.minOrNull() ?: 0f
    val unit = resultTemplate.getSpeedUnit()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PADDING_MEDIUM),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatColumn(stringResource(R.string.stat_min), STAT_FORMAT.format(minSpeed), unit)
            StatColumn(stringResource(R.string.stat_avg), STAT_FORMAT.format(avgSpeed), unit)
            StatColumn(stringResource(R.string.stat_max), STAT_FORMAT.format(maxSpeed), unit)
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun SelectedPointCard(point: GpsPoint, unitSystemStr: String) {
    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(point.timestamp))
    val unitSystem = Converter.getUnitSystem(unitSystemStr)
    
    val convertedSog = Converter.convertSpeed(point.sog, unitSystem)
    
    val resultTemplate = MovementResult(
        unitSystem = unitSystem,
        accelX = 0f, accelY = 0f, accelZ = 0f,
        speedIMU = 0f, speedGPS = 0f, speedFused = 0f,
        averageSpeed = 0f, sog = point.sog, cog = point.cog, azimuth = 0f, altitude = point.altitude, accuracy = 0f, pitch = point.pitch, roll = point.roll
    )
    
    val speedUnit = resultTemplate.getSpeedUnit()
    val convertedAlt = resultTemplate.getAltitude()
    val altUnit = resultTemplate.getAltitudeUnit()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(PADDING_SMALL)) {
            Text(
                text = stringResource(R.string.point_detail_header, point.id, timeStr),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SOG: ${STAT_FORMAT.format(convertedSog)} $speedUnit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "COG: ${DEGREE_FORMAT.format(point.cog)}°",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Alt: ${STAT_FORMAT.format(convertedAlt)} $altUnit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pitch: ${point.pitch}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Roll: ${point.roll}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SPACING_SMALL)
    )
}

/**
 * Line chart visualizing Speed Over Ground (SOG) relative to session duration.
 */
@Composable
private fun SpeedChart(points: List<GpsPoint>, unitSystemStr: String) {
    if (points.size < 2) return

    val modelProducer = remember { CartesianChartModelProducer() }
    val startTime = points.first().timestamp
    val unitSystem = Converter.getUnitSystem(unitSystemStr)

    LaunchedEffect(points, unitSystemStr) {
        modelProducer.runTransaction {
            lineSeries {
                val xValues = points.map { (it.timestamp - startTime).toDouble() / 1000.0 }
                val yValues = points.map { Converter.convertSpeed(it.sog, unitSystem).toDouble() }
                series(x = xValues, y = yValues)
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, value, _ -> formatDuration(value) }
            )
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(CHART_HEIGHT)
    )
}

/**
 * Line chart visualizing Altitude relative to session duration.
 */
@Composable
private fun AltitudeChart(points: List<GpsPoint>, unitSystemStr: String) {
    if (points.size < 2) return

    val modelProducer = remember { CartesianChartModelProducer() }
    val startTime = points.first().timestamp
    val unitSystem = Converter.getUnitSystem(unitSystemStr)

    LaunchedEffect(points, unitSystemStr) {
        modelProducer.runTransaction {
            lineSeries {
                val xValues = points.map { (it.timestamp - startTime).toDouble() / 1000.0 }
                val yValues = points.map { 
                    Converter.convertAlt(it.altitude, unitSystem)
                }
                series(x = xValues, y = yValues)
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, value, _ -> formatDuration(value) }
            )
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(CHART_HEIGHT)
    )
}

/**
 * Formats seconds into a MM:SS string.
 */
private fun formatDuration(seconds: Double): String {
    val totalSeconds = seconds.toLong()
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}

private val PADDING_MEDIUM = 16.dp
private val PADDING_SMALL = 12.dp
private val SPACING_SMALL = 8.dp
private val SPACING_MEDIUM = 16.dp
private val SPACING_LARGE = 24.dp
private val MAP_HEIGHT = 280.dp
private val CHART_HEIGHT = 200.dp
private val MAP_CORNER_RADIUS = 16.dp
private const val STAT_FORMAT = "%.1f"
private const val DEGREE_FORMAT = "%.0f"
