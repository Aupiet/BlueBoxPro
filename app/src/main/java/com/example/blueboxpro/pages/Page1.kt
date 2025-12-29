package com.example.blueboxpro.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blueboxpro.Process.MovementProcessor

@Composable
fun Page1(
    processor: MovementProcessor,
    refreshTrigger: Int,
    onNavigateToMap: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val scrollState = rememberScrollState()
    key(refreshTrigger) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Analyse de mouvement (Page 1)", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(24.dp))
            
            // Affichage du SOG (Speed Over Ground)
            Text(text = "SOG (Vitesse fond)", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${"%.2f".format(processor.sog)} m/s", 
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Affichage du COG (Course Over Ground)
            Text(text = "COG (Route fond)", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${"%.1f".format(processor.cog)}°", 
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Affichage de la Boussole (Compass)
            Text(text = "Boussole (Orientation)", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${"%.1f".format(processor.moyaz)}°",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(text = "Détails techniques :", style = MaterialTheme.typography.labelLarge)
            Text(text = "Moyenne: ${"%.2f".format(processor.moyspeed)} m/s")
            Text(text = "GPS: ${"%.2f".format(processor.speedGPS)} m/s")
            Text(text = "IMU: ${"%.2f".format(processor.speedIMU)} m/s")
            
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { processor.reset() }) {
                Text("Réinitialiser")
            }
        }
    }
}
