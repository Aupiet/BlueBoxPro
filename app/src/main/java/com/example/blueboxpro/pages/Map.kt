/**
 * This page provides a full-screen map view with manual recentering capability.
 * It uses the reusable MapContainer from MapComponents.
 */
package com.example.blueboxpro.pages

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blueboxpro.ui.components.MapComponents
import com.example.blueboxpro.R
import com.example.blueboxpro.Process.MovementProcessor
import org.osmdroid.util.GeoPoint

/**
 * Composable that displays a full-screen interactive map.
 * 
 * Includes a back button to return to the previous screen and a recenter button
 * to snap the camera back to the current user location.
 * 
 * @param location The current GPS location to display and center on.
 * @param processor Optional movement processor to display live speed/heading.
 * @param unitSystem The unit system for display.
 * @param onBack Callback invoked when the user taps the back button.
 */
@Composable
fun Page4(
    location: GeoPoint?, 
    processor: MovementProcessor? = null,
    unitSystem: String = "Metric (km/h, m)",
    onBack: () -> Unit
) {
    var recenterTrigger by remember { mutableStateOf(0) }
    
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val result = processor?.getResult(unitSystem)

    Box(modifier = Modifier.fillMaxSize()) {
        key(recenterTrigger) {
            MapComponents.MapContainer(
                location = location,
                modifier = Modifier.fillMaxSize(),
                isLocked = false,
                autoCenter = (recenterTrigger == 0)
            )
        }
        
        // Info Overlay (Speed/Heading) - Only shown if processor is provided
        if (result != null) {
            val overlayAlignment = if (isLandscape) Alignment.TopCenter else Alignment.TopCenter
            val overlayPadding = if (isLandscape) 8.dp else 48.dp // Avoid status bar in portrait

            Surface(
                modifier = Modifier
                    .align(overlayAlignment)
                    .padding(top = overlayPadding)
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%.1f".format(result.getSog()),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = result.getSpeedUnit(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    
                    VerticalDivider(modifier = Modifier.height(32.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%.0f°".format(result.getCog()),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "COG",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
        
        // Adjust FAB positions based on orientation to maximize map visibility
        val backButtonAlignment = if (isLandscape) Alignment.BottomStart else Alignment.BottomStart
        val recenterButtonAlignment = if (isLandscape) Alignment.BottomEnd else Alignment.BottomEnd
        val fabPadding = if (isLandscape) 16.dp else FAB_PADDING

        FloatingActionButton(
            onClick = onBack,
            modifier = Modifier
                .padding(fabPadding)
                .navigationBarsPadding() // Avoid system nav bar
                .align(backButtonAlignment),
            containerColor = MaterialTheme.colorScheme.secondary
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                contentDescription = stringResource(R.string.content_desc_back)
            )
        }

        FloatingActionButton(
            onClick = { recenterTrigger++ },
            modifier = Modifier
                .padding(fabPadding)
                .navigationBarsPadding()
                .align(recenterButtonAlignment),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn, 
                contentDescription = stringResource(R.string.content_desc_recenter)
            )
        }
    }
}

private val FAB_PADDING = 24.dp
