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

/**
 * The main dashboard page (Page 1).
 * 
 * Displays real-time movement statistics calculated by the processor.
 * Adaptive layout for portrait and landscape orientations.
 * 
 * @param processor The movement processor instance.
 * @param refreshTrigger Trigger to force recomposition when sensor data changes.
 * @param unitSystem Current unit system selected by the user.
 * @param onNavigateToMap Callback to switch to the map tab.
 * @param onNavigateToSettings Callback to switch to the settings tab.
 */
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
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(PADDING_MEDIUM),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.movement_analysis_title), 
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(SPACING_LARGE))
                
                MovementDataDisplay(result)
            }
        }
    }
}

/**
 * Displays SOG, COG, and Compass heading in a vertical stack.
 * 
 * @param result The movement result containing the current values and units.
 */
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

private val PADDING_MEDIUM = 16.dp
private val SPACING_MEDIUM = 16.dp
private val SPACING_LARGE = 24.dp
private const val SOG_FORMAT = "%.2f"
private const val DEGREE_FORMAT = "%.1f"
