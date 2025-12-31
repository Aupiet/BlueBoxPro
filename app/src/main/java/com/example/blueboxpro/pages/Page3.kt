package com.example.blueboxpro.pages

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.blueboxpro.R
import com.example.blueboxpro.Save.SessionManager
import com.example.blueboxpro.Process.MovementProcessor

@Composable
fun Page3(
    processor: MovementProcessor,
    refreshTrigger: Int
) {
    val scrollState = rememberScrollState()
    val sessions = SessionManager.sessions
    val context = LocalContext.current
    
    // On utilise l'état global du SessionManager pour la persistance
    val currentRecording = SessionManager.activeRecording
    val isRecording = currentRecording != null

    // Effet pour ajouter des points automatiquement pendant l'enregistrement
    // Cet effet sera relancé à chaque mise à jour des capteurs (refreshTrigger)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.recording_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Section Contrôle d'enregistrement
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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

                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        if (!isRecording) {
                            SessionManager.startRecording("Session ${sessions.size + 1}")
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRecording) stringResource(R.string.stop_recording) else stringResource(R.string.start_recording)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section Historique
        Text(
            text = stringResource(R.string.recent_saves),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
        )

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
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
                    headlineContent = { Text("${session.name} - ${session.date}") },
                    supportingContent = { Text("${stringResource(R.string.duration_label)} ${session.duration} | ${stringResource(R.string.distance_label)} ${session.distance}") },
                    trailingContent = {
                        IconButton(onClick = { /* TODO: Export */ }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.export_label))
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                )
                HorizontalDivider()
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(R.string.export_info),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
