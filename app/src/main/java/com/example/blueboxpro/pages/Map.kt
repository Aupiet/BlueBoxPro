/**
 * This page provides a full-screen map view with manual recentering capability.
 */
package com.example.blueboxpro.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blueboxpro.ui.components.MapComponents
import org.osmdroid.util.GeoPoint

/**
 * Composable that displays a full-screen map.
 */
@Composable
fun Page4(location: GeoPoint?, onBack: () -> Unit) {
    var recenterTrigger by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        key(recenterTrigger) {
            MapComponents.MapContainer(
                location = location,
                modifier = Modifier.fillMaxSize(),
                isLocked = false,
                autoCenter = (recenterTrigger == 0)
            )
        }
        
        FloatingActionButton(
            onClick = onBack,
            modifier = Modifier
                .padding(FAB_PADDING)
                .align(Alignment.BottomStart),
            containerColor = MaterialTheme.colorScheme.secondary
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
        }

        FloatingActionButton(
            onClick = { recenterTrigger++ },
            modifier = Modifier
                .padding(FAB_PADDING)
                .align(Alignment.BottomEnd),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = "Recentrer")
        }
    }
}

private val FAB_PADDING = 24.dp
