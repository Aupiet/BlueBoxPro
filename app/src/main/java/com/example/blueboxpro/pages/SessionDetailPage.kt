package com.example.blueboxpro.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.blueboxpro.R
import com.example.blueboxpro.Save.Session
import com.example.blueboxpro.Save.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailPage(
    session: Session?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog && session != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer la session") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cette session ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        SessionManager.deleteSession(context, session)
                        showDeleteDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.name ?: "Détails de la session") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (session != null) {
                Text(text = "Session : ${session.name}", style = MaterialTheme.typography.headlineMedium)
                Text(text = "Date : ${session.date}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Durée : ${session.duration}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Distance : ${session.distance}", style = MaterialTheme.typography.bodyLarge)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Statistiques détaillées à venir...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(text = "Session non trouvée")
            }
        }
    }
}
