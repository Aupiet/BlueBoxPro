package com.example.blueboxpro.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsPage(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres Avancés") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Paramètres de traitement",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // On pourra ajouter ici des sliders pour les seuils de vitesse, etc.
            Text("Seuil de vitesse IMU : 1.0 m/s")
            Text("Seuil de vitesse GPS : 1.0 m/s")
            Text("Précision GPS minimum : 30.0 m")
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Cette page permettra de calibrer finement les capteurs et les filtres de fusion.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
