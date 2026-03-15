/**
 * This page provides a layout for displaying the navigation dashboard.
 * It integrates the circular map, compass, and live performance data.
 */
package com.example.blueboxpro.pages

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.Process.WeatherData
import com.example.blueboxpro.ui.components.MapComponents
import org.osmdroid.util.GeoPoint

/**
 * The navigation and orientation dashboard (Page 2).
 * 
 * Delegates rendering to MapComponents.Page2Layout to maintain consistent styling.
 * 
 * @param location Current GPS location.
 * @param processor Movement processor providing live speed and orientation data.
 * @param refreshTrigger Trigger to force UI updates when data changes.
 * @param unitSystem The unit system currently in use.
 * @param weatherData Weather data for wind display.
 * @param onOpenFullScreenMap Callback to navigate to the full screen map view.
 */
@SuppressLint("MissingPermission")
@Composable
fun Page2(
    location: GeoPoint?, 
    processor: MovementProcessor,
    refreshTrigger: Int,
    unitSystem: String,
    weatherData: WeatherData? = null,
    onOpenFullScreenMap: () -> Unit
) {
    MapComponents.Page2Layout(
        location = location,
        processor = processor,
        unitSystem = unitSystem,
        weatherData = weatherData,
        onBack = {},
        onMapClick = onOpenFullScreenMap
    )
}
