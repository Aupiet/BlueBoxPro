package com.example.blueboxpro.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.blueboxpro.Process.MovementProcessor
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

object MapComponents {

    /**
     * Layout spécifique pour la Page 2 évitant les chevauchements.
     */
    @Composable
    fun Page2Layout(
        location: GeoPoint?,
        processor: MovementProcessor,
        onBack: () -> Unit,
        onMapClick: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (location != null) {
                    Text(text = "Latitude : ${"%.6f".format(location.latitude)}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Longitude : ${"%.6f".format(location.longitude)}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Altitude : ${"%.1f".format(processor.altitude)} m", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Précision : ${"%.1f".format(processor.gpsAccuracy)} m", style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text(text = "Recherche GPS...", style = MaterialTheme.typography.bodyLarge)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(onClick = onBack) {
                    Text("Retour")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                ReusableMapCard(
                    location = location,
                    widthFraction = 0.9f,
                    heightFraction = 1f,
                    isLocked = true,
                    autoCenter = true, // Recentrage auto pour la minicarte
                    onClick = onMapClick
                )
            }
        }
    }

    @Composable
    fun ReusableMapCard(
        location: GeoPoint?,
        modifier: Modifier = Modifier,
        widthFraction: Float = 0.9f,
        heightFraction: Float = 1f,
        alignment: Alignment = Alignment.Center,
        cornerRadius: Int = 16,
        isLocked: Boolean = true,
        autoCenter: Boolean = true, // Nouveau paramètre
        onClick: (() -> Unit)? = null
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = alignment
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .fillMaxHeight(heightFraction)
                    .clip(RoundedCornerShape(cornerRadius.dp))
            ) {
                MapContainer(
                    location = location,
                    modifier = Modifier.fillMaxSize(),
                    isLocked = isLocked,
                    autoCenter = autoCenter // Transmis ici
                )
                
                if (onClick != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                            .clickable { onClick() }
                    )
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Composable
    fun MapContainer(
        location: GeoPoint?,
        modifier: Modifier = Modifier,
        zoomLevel: Double = 17.0,
        isLocked: Boolean = true,
        autoCenter: Boolean = true // Nouveau paramètre
    ) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(!isLocked)
                    if (isLocked) setOnTouchListener { _, _ -> true }
                    controller.setZoom(zoomLevel)
                    
                    // On centre une première fois
                    location?.let { controller.setCenter(it) }
                }
            },
            update = { mapView ->
                location?.let { geoPoint ->
                    // On ne fait l'animation de recentrage QUE si autoCenter est vrai
                    if (autoCenter) {
                        mapView.controller.animateTo(geoPoint)
                    }
                    
                    mapView.overlays.clear()
                    val marker = Marker(mapView)
                    marker.position = geoPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Ma position"
                    mapView.overlays.add(marker)
                    mapView.invalidate()
                }
            },
            modifier = modifier
        )
    }
}
