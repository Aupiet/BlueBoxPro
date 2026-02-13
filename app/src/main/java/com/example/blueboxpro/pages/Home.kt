/**
 * This page displays the main movement analysis data, including Speed Over Ground (SOG),
 * Course Over Ground (COG), and compass heading.
 */
package com.example.blueboxpro.pages

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.Process.MovementResult
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    key(refreshTrigger) {
        if (isLandscape) {
            // Landscape Layout: Side-by-side using a Row
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PADDING_MEDIUM),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MovementDataDisplay(result)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TechnicalDetailsDisplay(result, processor)
                }
            }
        } else {
            // Portrait Layout: Vertical list using a Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(PADDING_MEDIUM),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.movement_analysis_title), style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(SPACING_LARGE))
                
                MovementDataDisplay(result)
                
                Spacer(modifier = Modifier.height(SPACING_XLARGE))
                
                TechnicalDetailsDisplay(result, processor)
            }
        }
    }
}

@Composable
private fun MovementDataDisplay(result: MovementResult) {
    // Speed Over Ground (SOG)
    Text(text = stringResource(R.string.sog_label), style = MaterialTheme.typography.titleMedium)
    Text(
        text = "${SOG_FORMAT.format(result.getSog())} ${result.getSpeedUnit()}", 
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary
    )
    
    Spacer(modifier = Modifier.height(SPACING_MEDIUM))
    
    // Course Over Ground (COG)
    Text(text = stringResource(R.string.cog_label), style = MaterialTheme.typography.titleMedium)
    Text(
        text = "${DEGREE_FORMAT.format(result.getCog())}°", 
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.secondary
    )

    Spacer(modifier = Modifier.height(SPACING_MEDIUM))

    // Compass
    Text(text = stringResource(R.string.compass_label), style = MaterialTheme.typography.titleMedium)
    Text(
        text = "${DEGREE_FORMAT.format(result.getAzimuth())}°",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.tertiary
    )
}

@Composable
private fun TechnicalDetailsDisplay(
    result: MovementResult,
    processor: MovementProcessor
) {
    Text(text = stringResource(R.string.technical_details), style = MaterialTheme.typography.labelLarge)
    Text(text = "${stringResource(R.string.average_label)} ${SOG_FORMAT.format(result.getMoyspeed())} ${result.getSpeedUnit()}")
    Text(text = "${stringResource(R.string.gps_label)} ${SOG_FORMAT.format(result.getSpeedGPS())} ${result.getSpeedUnit()}")
    Text(text = "${stringResource(R.string.imu_label)} ${SOG_FORMAT.format(result.getSpeedIMU())} ${result.getSpeedUnit()}")
    
    Spacer(modifier = Modifier.height(SPACING_LARGE))

    Button(onClick = { processor.reset() }) {
        Text(stringResource(R.string.reset_button))
    }
}

private val PADDING_MEDIUM = 16.dp
private val SPACING_MEDIUM = 16.dp
private val SPACING_LARGE = 24.dp
private val SPACING_XLARGE = 32.dp
private const val SOG_FORMAT = "%.2f"
private const val DEGREE_FORMAT = "%.1f"
