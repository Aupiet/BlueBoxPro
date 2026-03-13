/**
 * This file contains reusable UI components related to maps and spatial data display.
 */
package com.example.blueboxpro.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color as AndroidColor
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.Process.MovementResult
import com.example.blueboxpro.R
import com.example.blueboxpro.Save.GpsPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

object MapComponents {
    private const val MAP_CORNER_RADIUS_DP = 16
    private const val DEFAULT_ZOOM_LEVEL = 17.0
    private const val LAT_LON_FORMAT = "%.6f"
    private const val STAT_FORMAT = "%.1f"
    private const val PAGE2_INFO_WEIGHT = 2f
    private const val PAGE2_MAP_WEIGHT = 1f
    private const val MAP_WIDTH_FRACTION = 0.9f
    private const val SESSION_MAP_ZOOM = 16.0
    private const val BOUNDING_BOX_PADDING = 50

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
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        
        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize().padding(PADDING_MEDIUM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LocationInfo(location, result, onBack)
                }
                
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ReusableMapCard(
                        location = location,
                        widthFraction = 1f,
                        heightFraction = 0.9f,
                        isLocked = true,
                        autoCenter = true, 
                        onClick = onMapClick
                    )
                }
            }
        } else {
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
                    LocationInfo(location, result, onBack)
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
    }

    @Composable
    private fun LocationInfo(location: GeoPoint?, result: MovementResult, onBack: () -> Unit) {
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

    /**
     * Displays a session trace on the map with a polyline connecting GPS points,
     * start/end markers, and tap-to-select nearest point functionality.
     *
     * @param points List of GPS points from the recorded session
     * @param onPointTapped Callback when a point on the map is tapped, returns the nearest GpsPoint index
     */
    @SuppressLint("ClickableViewAccessibility")
    @Composable
    fun SessionMapContainer(
        points: List<GpsPoint>,
        modifier: Modifier = Modifier,
        selectedPointIndex: Int = -1,
        onPointTapped: (Int) -> Unit = {}
    ) {
        if (points.isEmpty()) return

        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(SESSION_MAP_ZOOM)
                }
            },
            update = { mapView ->
                mapView.overlays.clear()

                // Draw polyline connecting all points
                val geoPoints = points.map { GeoPoint(it.latitude, it.longitude) }
                val polyline = Polyline().apply {
                    setPoints(geoPoints)
                    outlinePaint.color = AndroidColor.rgb(33, 150, 243)
                    outlinePaint.strokeWidth = 8f
                }
                mapView.overlays.add(polyline)

                // Start marker (green)
                val startMarker = Marker(mapView).apply {
                    position = geoPoints.first()
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Départ"
                    icon = mapView.context.getDrawable(org.osmdroid.library.R.drawable.marker_default)
                }
                mapView.overlays.add(startMarker)

                // End marker (red)
                if (geoPoints.size > 1) {
                    val endMarker = Marker(mapView).apply {
                        position = geoPoints.last()
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Arrivée"
                    }
                    mapView.overlays.add(endMarker)
                }

                // Selected point marker
                if (selectedPointIndex in points.indices) {
                    val selectedPoint = points[selectedPointIndex]
                    val selectedMarker = Marker(mapView).apply {
                        position = GeoPoint(selectedPoint.latitude, selectedPoint.longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        title = "Point #${selectedPoint.id}"
                        snippet = "SOG: %.1f km/h | COG: %.0f°".format(
                            selectedPoint.sog * 3.6f,
                            selectedPoint.cog
                        )
                    }
                    mapView.overlays.add(selectedMarker)
                    selectedMarker.showInfoWindow()
                }

                // Fit map to show the full trace
                if (geoPoints.size >= 2) {
                    try {
                        val boundingBox = BoundingBox.fromGeoPoints(geoPoints)
                        mapView.post {
                            mapView.zoomToBoundingBox(boundingBox, true, BOUNDING_BOX_PADDING)
                        }
                    } catch (_: Exception) {
                        mapView.controller.setCenter(geoPoints.first())
                    }
                } else {
                    mapView.controller.setCenter(geoPoints.first())
                }

                // Tap listener: find nearest point
                mapView.setOnTouchListener { _, event ->
                    if (event.action == android.view.MotionEvent.ACTION_UP) {
                        val projection = mapView.projection
                        val tappedGeo = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                        
                        var minDist = Double.MAX_VALUE
                        var nearestIdx = 0
                        for (i in points.indices) {
                            val dist = tappedGeo.distanceToAsDouble(GeoPoint(points[i].latitude, points[i].longitude))
                            if (dist < minDist) {
                                minDist = dist
                                nearestIdx = i
                            }
                        }
                        onPointTapped(nearestIdx)
                    }
                    false
                }

                mapView.invalidate()
            },
            modifier = modifier
        )
    }

    private val PADDING_MEDIUM = 16.dp
    private val SPACING_LARGE = 24.dp
}
