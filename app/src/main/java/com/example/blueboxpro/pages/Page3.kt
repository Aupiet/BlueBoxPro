package com.example.blueboxpro.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.blueboxpro.Save.SessionManager

@Composable
fun Page3() {
    val scrollState = rememberScrollState()
    val sessions = SessionManager.sessions

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enregistrement de Course",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Section Contrôle d'enregistrement
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Nouvelle Session", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        /* TODO Pouvoir démarer l'enregistrement et arrêter l'enregistrement */
                        // Test : on ajoute une session au clic pour vérifier que ça s'affiche
                        SessionManager.addSession("12/05/2024", "45 min", "12.4 km")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Démarrer l'enregistrement")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section Historique
        Text(
            text = "Sauvegardes récentes",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
        )

        if (sessions.isEmpty()) {
            // Affichage si aucune sauvegarde
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune sauvegarde disponible",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Liste des sauvegardes réelles
            sessions.forEach { session ->
                ListItem(
                    headlineContent = { Text("Session #${session.id} - ${session.date}") },
                    supportingContent = { Text("Durée: ${session.duration} | Distance: ${session.distance}") },
                    trailingContent = {
                        IconButton(onClick = { /* TODO: Export */ }) {
                            Icon(Icons.Default.Share, contentDescription = "Exporter")
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                )
                HorizontalDivider()
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Les données seront exportées au format CSV ou GPX.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
