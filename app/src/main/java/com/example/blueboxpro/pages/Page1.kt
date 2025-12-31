package com.example.blueboxpro.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.R

@Composable
fun Page1(
    processor: MovementProcessor,
    refreshTrigger: Int,
    unitSystem: String,
    onNavigateToMap: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val scrollState = rememberScrollState()
    val result = processor.getResult(unitSystem)
    
    key(refreshTrigger) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(R.string.movement_analysis_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(24.dp))
            
            // Affichage du SOG (Speed Over Ground)
            Text(text = stringResource(R.string.sog_label), style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${"%.2f".format(result.getSog())} ${result.getSpeedUnit()}", 
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Affichage du COG (Course Over Ground)
            Text(text = stringResource(R.string.cog_label), style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${"%.1f".format(result.getCog())}°", 
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Affichage de la Boussole (Compass)
            Text(text = stringResource(R.string.compass_label), style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${"%.1f".format(result.getAzimuth())}°",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(text = stringResource(R.string.technical_details), style = MaterialTheme.typography.labelLarge)
            Text(text = "${stringResource(R.string.average_label)} ${"%.2f".format(result.getMoyspeed())} ${result.getSpeedUnit()}")
            Text(text = "${stringResource(R.string.gps_label)} ${"%.2f".format(result.getSpeedGPS())} ${result.getSpeedUnit()}")
            Text(text = "${stringResource(R.string.imu_label)} ${"%.2f".format(result.getSpeedIMU())} ${result.getSpeedUnit()}")
            
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { processor.reset() }) {
                Text(stringResource(R.string.reset_button))
            }
        }
    }
}
