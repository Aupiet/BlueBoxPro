/**
 * This page manages session recordings and displays the history of saved sessions.
 * It provides UI for starting, stopping, and sharing tracking sessions.
 */
package com.example.blueboxpro.pages

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
 * The recording and history page (Page 3).
 * 
 * @param processor The movement processor instance.
 * @param refreshTrigger Trigger to force recomposition.
 * @param onSessionClick Callback when a session is selected.
 */
@Composable
fun Page3(
    processor: MovementProcessor,
    refreshTrigger: Int,
    onSessionClick: (Int) -> Unit
) {
    val historyScrollState = rememberScrollState()
    val sessions = SessionManager.sessions
    val context = LocalContext.current
    
    val currentRecording = SessionManager.activeRecording
    val isRecording = currentRecording != null

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(REC_PADDING_MEDIUM),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(REC_WEIGHT_HALF)
                    .fillMaxHeight()
                    .padding(end = REC_PADDING_MEDIUM),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.recording_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = REC_SPACING_LARGE),
                    textAlign = TextAlign.Center
                )
                
                RecordingControlCard(isRecording, currentRecording, sessions.size, context)
                
                Text(
                    text = stringResource(R.string.export_info),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = REC_SPACING_MEDIUM)
                )
            }

            Column(
                modifier = Modifier
                    .weight(REC_WEIGHT_HALF)
                    .fillMaxHeight()
                    .verticalScroll(historyScrollState),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.recent_saves),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = REC_SPACING_SMALL)
                )
                
                HistorySection(sessions, onSessionClick)
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.padding(REC_PADDING_MEDIUM),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.recording_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = REC_SPACING_LARGE)
                )

                RecordingControlCard(isRecording, currentRecording, sessions.size, context)
            }

            Column(
                modifier = Modifier
                    .weight(REC_WEIGHT_HALF)
                    .verticalScroll(historyScrollState)
                    .padding(horizontal = REC_PADDING_MEDIUM),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.recent_saves),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = REC_SPACING_SMALL)
                )

                HistorySection(sessions, onSessionClick)

                Spacer(modifier = Modifier.height(REC_SPACING_MEDIUM))

                Text(
                    text = stringResource(R.string.export_info),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(bottom = REC_SPACING_MEDIUM),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Card containing controls for starting and stopping a recording.
 */
@Composable
private fun RecordingControlCard(
    isRecording: Boolean,
    currentRecording: Recording?,
    sessionCount: Int,
    context: android.content.Context
) {
    Card(
        modifier = Modifier.size(width = REC_CARD_WIDTH, height = REC_CARD_HEIGHT),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(REC_PADDING_MEDIUM),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isRecording) stringResource(R.string.recording_title) else stringResource(R.string.new_session),
                style = MaterialTheme.typography.titleMedium
            )
            
            if (isRecording) {
                Text(
                    text = "Points: ${currentRecording?.points?.size ?: 0}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(REC_WEIGHT_HALF))
            
            Button(
                onClick = {
                    if (!isRecording) {
                        SessionManager.startRecording("Session ${sessionCount + 1}")
                    } else {
                        SessionManager.stopRecording(context)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = if (isRecording) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors()
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Close else Icons.Default.PlayArrow, 
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(REC_SPACING_SMALL))
                Text(
                    text = if (isRecording) stringResource(R.string.stop_recording) else stringResource(R.string.start_recording)
                )
            }
        }
    }
}

/**
 * List displaying previous tracking sessions.
 */
@Composable
private fun HistorySection(
    sessions: List<Session>,
    onSessionClick: (Int) -> Unit
) {
    val context = LocalContext.current
    if (sessions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = REC_EMPTY_PADDING),
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
                supportingContent = { 
                    Text("${stringResource(R.string.duration_label)} ${session.duration} | ${stringResource(R.string.distance_label)} ${session.distance}") 
                },
                trailingContent = {
                    IconButton(
                        onClick = {
                            val file = SessionManager.exportSessionToCsv(context, session)
                            SessionManager.shareFile(context, file)
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.export_label))
                    }
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
            )
            HorizontalDivider()
        }
    }
}

private val REC_PADDING_MEDIUM = 16.dp
private val REC_SPACING_SMALL = 8.dp
private val REC_SPACING_MEDIUM = 16.dp
private val REC_SPACING_LARGE = 24.dp
private val REC_EMPTY_PADDING = 40.dp
private val REC_CARD_WIDTH = 300.dp
private val REC_CARD_HEIGHT = 180.dp
private const val REC_WEIGHT_HALF = 1f
