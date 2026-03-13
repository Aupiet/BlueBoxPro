/**
 * This page displays detailed information about a specific recorded session,
 * including a map trace, speed statistics, interactive charts, and point-level details.
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
 * Composable for the session detail screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailPage(
    session: Session?,
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (session != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer",
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
            // --- Session Info Header ---
            SessionInfoHeader(session)

            Spacer(modifier = Modifier.height(SPACING_MEDIUM))

            // --- Speed Statistics ---
            if (points.isNotEmpty()) {
                SpeedStatisticsCard(points)

                Spacer(modifier = Modifier.height(SPACING_MEDIUM))

                // --- Map with Trace ---
                SectionHeader("Tracé GPS")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MAP_HEIGHT)
                        .clip(RoundedCornerShape(MAP_CORNER_RADIUS))
                ) {
                    MapComponents.SessionMapContainer(
                        points = points,
                        modifier = Modifier.fillMaxSize(),
                        selectedPointIndex = selectedPointIndex,
                        onPointTapped = { idx -> selectedPointIndex = idx }
                    )
                }

                // --- Selected Point Info ---
                if (selectedPointIndex in points.indices) {
                    Spacer(modifier = Modifier.height(SPACING_SMALL))
                    SelectedPointCard(points[selectedPointIndex])
                }

                Spacer(modifier = Modifier.height(SPACING_MEDIUM))

                // --- SOG Chart ---
                SectionHeader("Vitesse (SOG)")
                SpeedChart(points)

                Spacer(modifier = Modifier.height(SPACING_MEDIUM))

                // --- COG Chart ---
                SectionHeader("Cap (COG)")
                CogChart(points)

                Spacer(modifier = Modifier.height(SPACING_LARGE))
            } else {
                Spacer(modifier = Modifier.height(SPACING_LARGE))
                Text(
                    text = "Aucun point GPS enregistré pour cette session.",
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
            Text(text = "Date : ${session.date}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Durée : ${session.duration}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Distance : ${session.distance}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Vitesse moy. : ${session.averageSpeed}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${session.points.size} points GPS",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SpeedStatisticsCard(points: List<GpsPoint>) {
    val sogValues = points.map { it.sog * Option.Movement.MS_TO_KMH }
    val avgSpeed = sogValues.average().toFloat()
    val maxSpeed = sogValues.maxOrNull() ?: 0f
    val minSpeed = sogValues.minOrNull() ?: 0f

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
            StatColumn("Min", STAT_FORMAT.format(minSpeed), "km/h")
            StatColumn("Moy", STAT_FORMAT.format(avgSpeed), "km/h")
            StatColumn("Max", STAT_FORMAT.format(maxSpeed), "km/h")
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
private fun SelectedPointCard(point: GpsPoint) {
    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(point.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(PADDING_SMALL)) {
            Text(
                text = "Point #${point.id} — $timeStr",
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
                    text = "SOG: ${STAT_FORMAT.format(point.sog * Option.Movement.MS_TO_KMH)} km/h",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "COG: ${DEGREE_FORMAT.format(point.cog)}°",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Alt: ${STAT_FORMAT.format(point.altitude)} m",
                    style = MaterialTheme.typography.bodyMedium,
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
 * SOG line chart using Vico library.
 */
@Composable
private fun SpeedChart(points: List<GpsPoint>) {
    if (points.size < 2) return

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(points) {
        modelProducer.runTransaction {
            lineSeries {
                series(points.map { it.sog * Option.Movement.MS_TO_KMH })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom()
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(CHART_HEIGHT)
    )
}

/**
 * COG line chart using Vico library.
 */
@Composable
private fun CogChart(points: List<GpsPoint>) {
    if (points.size < 2) return

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(points) {
        modelProducer.runTransaction {
            lineSeries {
                series(points.map { it.cog.toDouble() })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom()
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(CHART_HEIGHT)
    )
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
