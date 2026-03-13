/**
 * This page provides a layout for displaying the compass, live trace, and navigation data,
 * delegating the actual rendering to MapComponents.
 */
package com.example.blueboxpro.pages

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.ui.components.MapComponents
import org.osmdroid.util.GeoPoint

/**
 * Composable for the second page of the application, focused on navigation view.
 */
@SuppressLint("MissingPermission")
@Composable
fun Page2(
    location: GeoPoint?, 
    processor: MovementProcessor,
    refreshTrigger: Int,
    unitSystem: String,
    onOpenFullScreenMap: () -> Unit
) {
    MapComponents.Page2Layout(
        location = location,
        processor = processor,
        unitSystem = unitSystem,
        onBack = {},
        onMapClick = onOpenFullScreenMap
    )
}
