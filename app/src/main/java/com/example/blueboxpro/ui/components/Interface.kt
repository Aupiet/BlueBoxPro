/**
 * This file contains reusable UI components related to maps and spatial data display.
 */
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
    private const val MAP_CORNER_RADIUS_DP = 16
    private const val DEFAULT_ZOOM_LEVEL = 17.0
    private const val LAT_LON_FORMAT = "%.6f"
    private const val STAT_FORMAT = "%.1f"
    private const val PAGE2_INFO_WEIGHT = 2f
    private const val PAGE2_MAP_WEIGHT = 1f
    private const val MAP_WIDTH_FRACTION = 0.9f

    /**
     * Specialized layout for Page 2, displaying GPS coordinates and a mini-map.
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
                    .weight(PAGE2_INFO_WEIGHT)
                    .padding(PADDING_MEDIUM),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (location != null) {
                    Text(text = "Latitude : ${LAT_LON_FORMAT.format(location.latitude)}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Longitude : ${LAT_LON_FORMAT.format(location.longitude)}", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "${stringResource(R.string.altitude_label)} ${STAT_FORMAT.format(result.getAltitude())} ${result.getAltitudeUnit()}", 
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${stringResource(R.string.accuracy_label)} ${STAT_FORMAT.format(result.getAccuracy())} ${result.getAccuracyUnit()}", 
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(text = stringResource(R.string.gps_not_available), style = MaterialTheme.typography.bodyLarge)
                }
                
                Spacer(modifier = Modifier.height(SPACING_LARGE))
                
                Button(onClick = onBack) {
                    Text(stringResource(R.string.reset_button))
                    // TODO: Create a specific string resource for 'Back' if reset_button is not appropriate
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(PAGE2_MAP_WEIGHT)
                    .padding(bottom = PADDING_MEDIUM),
                contentAlignment = Alignment.Center
            ) {
                ReusableMapCard(
                    location = location,
                    widthFraction = MAP_WIDTH_FRACTION,
                    heightFraction = 1f,
                    isLocked = true,
                    autoCenter = true, 
                    onClick = onMapClick
                )
            }
        }
    }

    /**
     * A reusable card component that displays a map inside a clipped container.
     */
    @Composable
    fun ReusableMapCard(
        location: GeoPoint?,
        modifier: Modifier = Modifier,
        widthFraction: Float = MAP_WIDTH_FRACTION,
        heightFraction: Float = 1f,
        alignment: Alignment = Alignment.Center,
        cornerRadius: Int = MAP_CORNER_RADIUS_DP,
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

    /**
     * The core map container using the Osmdroid MapView via AndroidView interop.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Composable
    fun MapContainer(
        location: GeoPoint?,
        modifier: Modifier = Modifier,
        zoomLevel: Double = DEFAULT_ZOOM_LEVEL,
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
                    marker.title = "Position" 
                    mapView.overlays.add(marker)
                    mapView.invalidate()
                }
            },
            modifier = modifier
        )
    }

    private val PADDING_MEDIUM = 16.dp
    private val SPACING_LARGE = 24.dp
}
