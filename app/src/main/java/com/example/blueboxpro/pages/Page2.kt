package com.example.blueboxpro.pages

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.ui.components.MapComponents
import org.osmdroid.util.GeoPoint

@SuppressLint("MissingPermission")
@Composable
fun Page2(
    location: GeoPoint?, 
    processor: MovementProcessor,
    unitSystem: String,
    onBack: () -> Unit,
    onOpenFullScreenMap: () -> Unit
) {
    MapComponents.Page2Layout(
        location = location,
        processor = processor,
        unitSystem = unitSystem,
        onBack = onBack,
        onMapClick = onOpenFullScreenMap
    )
}
