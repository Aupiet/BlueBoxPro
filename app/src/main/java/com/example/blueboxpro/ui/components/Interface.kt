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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.R
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
        unitSystem: String,
        onBack: () -> Unit,
        onMapClick: () -> Unit
    ) {
        val result = processor.getResult(unitSystem)
        
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
                    Text(text = "${stringResource(R.string.altitude_label)} ${"%.1f".format(result.getAltitude())} ${result.getAltitudeUnit()}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "${stringResource(R.string.accuracy_label)} ${"%.1f".format(result.getAccuracy())} ${result.getAccuracyUnit()}", style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text(text = stringResource(R.string.gps_not_available), style = MaterialTheme.typography.bodyLarge)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(onClick = onBack) {
                    Text(stringResource(R.string.reset_button)) // On utilise reset_button car c'est "Retour/Back" dans ce contexte ? 
                    // Note: Il serait préférable d'ajouter une string spécifique "back_button"
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
                    autoCenter = true, 
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
        autoCenter: Boolean = true,
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
                    autoCenter = autoCenter
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
        autoCenter: Boolean = true
    ) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(!isLocked)
                    if (isLocked) setOnTouchListener { _, _ -> true }
                    controller.setZoom(zoomLevel)
                    location?.let { controller.setCenter(it) }
                }
            },
            update = { mapView ->
                location?.let { geoPoint ->
                    if (autoCenter) {
                        mapView.controller.animateTo(geoPoint)
                    }
                    
                    mapView.overlays.clear()
                    val marker = Marker(mapView)
                    marker.position = geoPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    // Note: On pourrait aussi traduire "Ma position"
                    marker.title = "Position" 
                    mapView.overlays.add(marker)
                    mapView.invalidate()
                }
            },
            modifier = modifier
        )
    }
}
