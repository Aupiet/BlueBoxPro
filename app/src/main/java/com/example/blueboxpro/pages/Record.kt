/**
 * This page manages session recordings and displays the history of saved sessions.
 */
package com.example.blueboxpro.pages

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.blueboxpro.R
import com.example.blueboxpro.Save.SessionManager
import com.example.blueboxpro.Save.Recording
import com.example.blueboxpro.Save.Session
import com.example.blueboxpro.Process.MovementProcessor

/**
 * Composable for the third page of the application, focused on session management.
 */
@Composable
fun Page3(
    processor: MovementProcessor,
    refreshTrigger: Int,
    onSessionClick: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    val historyScrollState = rememberScrollState()
    val sessions = SessionManager.sessions
    val context = LocalContext.current
    
    val currentRecording = SessionManager.activeRecording
    val isRecording = currentRecording != null

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(refreshTrigger) {
        if (isRecording) {
            currentRecording?.addPoint(
                latitude = processor.lastLocation?.latitude ?: 0.0,
                longitude = processor.lastLocation?.longitude ?: 0.0,
                altitude = processor.altitude,
                sog = processor.sog,
                cog = processor.cog
            )
        }
    }

    if (isLandscape) {
        // Landscape Layout: Controls on the left, History on the right
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(PADDING_MEDIUM),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = PADDING_MEDIUM),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.recording_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = SPACING_LARGE),
                    textAlign = TextAlign.Left
                )
                
                RecordingControlCard(isRecording, currentRecording, sessions.size, context)
                
                //Spacer(modifier = Modifier.height(SPACING_MEDIUM))
                
                Text(
                    text = stringResource(R.string.export_info),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(historyScrollState),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.recent_saves),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = SPACING_SMALL)
                )
                
                HistorySection(sessions, onSessionClick)
            }
        }
    } else {
        // Portrait Layout: Vertical stack
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(PADDING_MEDIUM),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.recording_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = SPACING_LARGE)
            )

            RecordingControlCard(isRecording, currentRecording, sessions.size, context)

            Spacer(modifier = Modifier.height(SPACING_LARGE))

            Text(
                text = stringResource(R.string.recent_saves),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start).padding(bottom = SPACING_SMALL)
            )

            HistorySection(sessions, onSessionClick)

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.export_info),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = SPACING_MEDIUM)
            )
        }
    }
}

@Composable
private fun RecordingControlCard(
    isRecording: Boolean,
    currentRecording: Recording?,
    sessionCount: Int,
    context: android.content.Context
) {
    Card(
        modifier = Modifier
            .size(width = 300.dp, height = 200.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(PADDING_MEDIUM),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween

        ) {
            Text(
                text = if (isRecording) "Enregistrement en cours..." else stringResource(R.string.new_session),
                style = MaterialTheme.typography.titleMedium
            )
            
            if (isRecording) {
                Text(
                    text = "Points capturés : ${currentRecording?.points?.size ?: 0}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (isRecording) {
                Spacer(modifier = Modifier.height(85.dp))
            }
            else {
                Spacer(modifier = Modifier.height(100.dp))
            }
            
            Button(
                onClick = {
                    if (!isRecording) {
                        SessionManager.startRecording("Session ${sessionCount + 1}")
                    } else {
                        SessionManager.stopRecording(context)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = if (isRecording) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors()
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Close else Icons.Default.PlayArrow, 
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(SPACING_SMALL))
                Text(
                    text = if (isRecording) stringResource(R.string.stop_recording) else stringResource(R.string.start_recording)
                )
            }
        }
    }
}

@Composable
private fun HistorySection(
    sessions: List<Session>,
    onSessionClick: (Int) -> Unit
) {
    if (sessions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = EMPTY_STATE_VERTICAL_PADDING),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_saves),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        sessions.reversed().forEach { session ->
            ListItem(
                modifier = Modifier.clickable { onSessionClick(session.id) },
                headlineContent = { Text("${session.name} - ${session.date}") },
                supportingContent = { Text("${stringResource(R.string.duration_label)} ${session.duration} | ${stringResource(R.string.distance_label)} ${session.distance}") },
                trailingContent = {
                    IconButton(onClick = { /* TODO: Implement export functionality */ }) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.export_label))
                    }
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
            )
            HorizontalDivider()
        }
    }
}

private val PADDING_MEDIUM = 16.dp
private val SPACING_SMALL = 8.dp
private val SPACING_MEDIUM = 16.dp
private val SPACING_LARGE = 24.dp
private val EMPTY_STATE_VERTICAL_PADDING = 40.dp
