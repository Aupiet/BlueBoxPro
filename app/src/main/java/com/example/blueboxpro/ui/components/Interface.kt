/**
 * Reusable UI components for map displays, navigation panels, and spatial data visualization.
 * This file provides specialized components like a circular map with an integrated compass,
 * as well as generic map containers using Osmdroid.
 * 
 * It supports adaptive layouts for portrait and landscape orientations, matching the 
 * provided design sketches.
 */
package com.example.blueboxpro.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.blueboxpro.Option
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.Process.MovementResult
import com.example.blueboxpro.Process.UnitSystem
import com.example.blueboxpro.R
import com.example.blueboxpro.Save.GpsPoint
import com.example.blueboxpro.Save.SessionManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Collection of map-related UI components and layout logic.
 */
object MapComponents {
    // UI Scaling and Layout constants
    private const val DEFAULT_ZOOM_LEVEL = 17.0
    private const val SESSION_MAP_ZOOM = 16.0
    private const val BOUNDING_BOX_PADDING = 50
    private const val LAT_LON_FORMAT = "%.6f"
    private const val STAT_FORMAT = "%.1f"
    private const val DEGREE_FORMAT = "%.0f"

    private const val COMPASS_RING_WIDTH_RATIO = 0.12f
    private const val COMPASS_ANIM_DURATION_MS = 300
    
    // Drawing constants
    private const val COMPASS_BACKGROUND_ALPHA = 0.75f
    private const val COMPASS_BORDER_ALPHA = 0.3f
    private const val COMPASS_BORDER_WIDTH = 1.5f
    private const val COMPASS_TICK_STEP = 5
    private const val COMPASS_MAJOR_TICK_STEP = 30
    private const val COMPASS_MID_TICK_STEP = 10
    
    private const val COMPASS_MAJOR_TICK_LENGTH_RATIO = 0.15f
    private const val COMPASS_MID_TICK_LENGTH_RATIO = 0.3f
    private const val COMPASS_MINOR_TICK_LENGTH_RATIO = 0.45f
    private const val COMPASS_TICK_OUTER_GAP_RATIO = 0.05f
    
    private const val COMPASS_MAJOR_TICK_WIDTH = 2.5f
    private const val COMPASS_MINOR_TICK_WIDTH = 1.0f
    
    private const val INDICATOR_OFFSET_PX = 4f
    private const val INDICATOR_HALF_WIDTH_PX = 8f
    
    private const val TRACE_LINE_WIDTH = 6f
    private const val SESSION_TRACE_LINE_WIDTH = 8f
    private const val OUTLINE_STROKE_WIDTH = 3f
    
    private const val POSITION_MARKER_SIZE = 60
    private const val TRIANGLE_TOP_MARGIN_RATIO = 0.15f
    private const val TRIANGLE_SIDE_MARGIN_RATIO = 0.25f
    private const val TRIANGLE_BOTTOM_MARGIN_RATIO = 0.8f

    /**
     * Dashboard layout for Page 2 (Map/Navigation).
     * Follows the design sketch for both orientations.
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
        val activeRecording = SessionManager.activeRecording
        val recordingPoints = activeRecording?.points ?: emptyList()
        
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            // Landscape: Left = COG + Map, Right = Coords (Top) + Gauges (Bottom)
            Row(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1.2f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "COG: ${DEGREE_FORMAT.format(result.getCog())}°",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        CircularMapWithCompass(location, result.getCog(), result.getAzimuth(), recordingPoints, onMapClick)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                    // Coordinates Section
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        CoordinateText("Latitude", LAT_LON_FORMAT.format(location?.latitude ?: 0.0))
                        CoordinateText("Altitude", "${STAT_FORMAT.format(result.getAltitude())} ${result.getAltitudeUnit()}")
                        CoordinateText("Longitude", LAT_LON_FORMAT.format(location?.longitude ?: 0.0))
                    }

                    // Gauges Section (SOG with Unit, Pitch, Roll)
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        CircularGauge("SOG", "${STAT_FORMAT.format(result.getSog())} ${result.getSpeedUnit()}", MaterialTheme.colorScheme.tertiary)
                        CircularGauge("PITCH", "${result.getPitch()}°", MaterialTheme.colorScheme.primary)
                        CircularGauge("ROLL", "${result.getRoll()}°", MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        } else {
            // Portrait: COG (top), Map (middle), Coordinates (text), Gauges (bottom: SOG, Pitch, Roll)
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "COG: ${DEGREE_FORMAT.format(result.getCog())}°",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                    CircularMapWithCompass(location, result.getCog(), result.getAzimuth(), recordingPoints, onMapClick)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text(text = "Long: ${LAT_LON_FORMAT.format(location?.longitude ?: 0.0)}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Lat: ${LAT_LON_FORMAT.format(location?.latitude ?: 0.0)}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Alt: ${STAT_FORMAT.format(result.getAltitude())}", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    CircularGauge("SOG", "${STAT_FORMAT.format(result.getSog())} ${result.getSpeedUnit()}")
                    CircularGauge("PITCH", "${result.getPitch()}°", MaterialTheme.colorScheme.primary)
                    CircularGauge("ROLL", "${result.getRoll()}°", MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }

    @Composable
    private fun CoordinateText(label: String, value: String) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }

    @Composable
    private fun CircularGauge(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(75.dp).border(2.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.Bold, 
                    color = color,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }

    /**
     * Circular clipped map with an overlayed rotating compass ring.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Composable
    private fun CircularMapWithCompass(
        location: GeoPoint?,
        cog: Float,
        azimuth: Float,
        recordingPoints: List<GpsPoint>,
        onMapClick: () -> Unit
    ) {
        val animatedCog by animateFloatAsState(
            targetValue = cog,
            animationSpec = tween(durationMillis = COMPASS_ANIM_DURATION_MS),
            label = "cog"
        )

        val primaryColor = MaterialTheme.colorScheme.primary
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface
        val surfaceColor = MaterialTheme.colorScheme.surface
        val errorColor = MaterialTheme.colorScheme.error
        val textMeasurer = rememberTextMeasurer()

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.fillMaxSize().clip(CircleShape)) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(false)
                            setOnTouchListener { _, _ -> true }
                            controller.setZoom(DEFAULT_ZOOM_LEVEL)
                            location?.let { controller.setCenter(it) }
                        }
                    },
                    update = { mapView ->
                        mapView.mapOrientation = -animatedCog
                        mapView.overlays.clear()

                        if (recordingPoints.isNotEmpty()) {
                            val tracePoints = recordingPoints.map { GeoPoint(it.latitude, it.longitude) }
                            val polyline = Polyline().apply {
                                setPoints(tracePoints)
                                outlinePaint.color = AndroidColor.rgb(33, 150, 243)
                                outlinePaint.strokeWidth = TRACE_LINE_WIDTH
                            }
                            mapView.overlays.add(polyline)
                        }

                        location?.let { geoPoint ->
                            mapView.controller.animateTo(geoPoint)
                            val triangleBitmap = createTriangleBitmap(POSITION_MARKER_SIZE, 0f, primaryColor.toArgb())
                            val marker = Marker(mapView).apply {
                                position = geoPoint
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                icon = BitmapDrawable(mapView.context.resources, triangleBitmap)
                                setInfoWindow(null)
                            }
                            mapView.overlays.add(marker)
                        }
                        mapView.invalidate()
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Transparent).clickable { onMapClick() })
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val cy = size.height / 2
                val outerRadius = min(cx, cy)
                val ringWidth = outerRadius * COMPASS_RING_WIDTH_RATIO
                val midRadius = outerRadius - ringWidth / 2
                val innerRadius = outerRadius - ringWidth

                drawCircle(color = surfaceColor.copy(alpha = COMPASS_BACKGROUND_ALPHA), radius = outerRadius, center = Offset(cx, cy), style = Stroke(width = ringWidth))
                drawCircle(color = onSurfaceColor.copy(alpha = COMPASS_BORDER_ALPHA), radius = outerRadius, center = Offset(cx, cy), style = Stroke(width = COMPASS_BORDER_WIDTH))
                drawCircle(color = onSurfaceColor.copy(alpha = COMPASS_BORDER_ALPHA), radius = innerRadius, center = Offset(cx, cy), style = Stroke(width = COMPASS_BORDER_WIDTH))

                for (i in 0 until 360 step COMPASS_TICK_STEP) {
                    val angleRad = Math.toRadians((i - animatedCog).toDouble()).toFloat()
                    val isMajor = i % COMPASS_MAJOR_TICK_STEP == 0
                    val isMid = i % COMPASS_MID_TICK_STEP == 0
                    val tickInner = if (isMajor) innerRadius + ringWidth * COMPASS_MAJOR_TICK_LENGTH_RATIO
                                    else if (isMid) innerRadius + ringWidth * COMPASS_MID_TICK_LENGTH_RATIO
                                    else innerRadius + ringWidth * COMPASS_MINOR_TICK_LENGTH_RATIO
                    val tickOuter = outerRadius - ringWidth * COMPASS_TICK_OUTER_GAP_RATIO

                    drawLine(
                        color = onSurfaceColor.copy(alpha = if (isMajor) 0.9f else if (isMid) 0.5f else 0.25f),
                        start = Offset(cx + sin(angleRad) * tickInner, cy - cos(angleRad) * tickInner),
                        end = Offset(cx + sin(angleRad) * tickOuter, cy - cos(angleRad) * tickOuter),
                        strokeWidth = if (isMajor) COMPASS_MAJOR_TICK_WIDTH else COMPASS_MINOR_TICK_WIDTH,
                        cap = StrokeCap.Round
                    )
                }

                val cardinals = mapOf(0f to "N", 45f to "NE", 90f to "E", 135f to "SE", 180f to "S", 225f to "SW", 270f to "W", 315f to "NW")
                for (i in 0 until 360 step COMPASS_MAJOR_TICK_STEP) {
                    val angleRad = Math.toRadians((i - animatedCog).toDouble()).toFloat()
                    val cardinalLabel = cardinals[i.toFloat()]
                    val displayText = cardinalLabel ?: "${i}°"
                    val isCardinal = cardinalLabel != null && cardinalLabel.length == 1
                    val style = TextStyle(fontWeight = if (isCardinal) FontWeight.ExtraBold else FontWeight.Medium, fontSize = if (isCardinal) 14.sp else 9.sp, color = if (cardinalLabel == "N") errorColor else onSurfaceColor)
                    val measured = textMeasurer.measure(displayText, style)
                    drawText(textLayoutResult = measured, topLeft = Offset(cx + sin(angleRad) * midRadius - measured.size.width / 2f, cy - cos(angleRad) * midRadius - measured.size.height / 2f))
                }

                val indicatorPath = Path().apply {
                    moveTo(cx, cy - outerRadius - INDICATOR_OFFSET_PX)
                    lineTo(cx - INDICATOR_HALF_WIDTH_PX, cy - innerRadius + INDICATOR_OFFSET_PX)
                    lineTo(cx + INDICATOR_HALF_WIDTH_PX, cy - innerRadius + INDICATOR_OFFSET_PX)
                    close()
                }
                drawPath(indicatorPath, color = primaryColor)
            }
        }
    }

    private fun createTriangleBitmap(size: Int, rotation: Float, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }
        val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = AndroidColor.WHITE; style = Paint.Style.STROKE; strokeWidth = OUTLINE_STROKE_WIDTH }
        canvas.save()
        canvas.rotate(rotation, size / 2f, size / 2f)
        val path = android.graphics.Path().apply {
            moveTo(size / 2f, size * TRIANGLE_TOP_MARGIN_RATIO)
            lineTo(size * TRIANGLE_SIDE_MARGIN_RATIO, size * TRIANGLE_BOTTOM_MARGIN_RATIO)
            lineTo(size * (1f - TRIANGLE_SIDE_MARGIN_RATIO), size * TRIANGLE_BOTTOM_MARGIN_RATIO)
            close()
        }
        canvas.drawPath(path, paint); canvas.drawPath(path, outlinePaint); canvas.restore()
        return bitmap
    }

    @SuppressLint("ClickableViewAccessibility")
    @Composable
    fun MapContainer(location: GeoPoint?, modifier: Modifier = Modifier, zoomLevel: Double = DEFAULT_ZOOM_LEVEL, isLocked: Boolean = true, autoCenter: Boolean = true) {
        val positionLabel = stringResource(R.string.marker_position)
        AndroidView(
            factory = { ctx -> MapView(ctx).apply { setTileSource(TileSourceFactory.MAPNIK); setMultiTouchControls(!isLocked); if (isLocked) setOnTouchListener { _, _ -> true }; controller.setZoom(zoomLevel); location?.let { controller.setCenter(it) } } },
            update = { mapView ->
                location?.let { geoPoint ->
                    if (autoCenter) mapView.controller.animateTo(geoPoint)
                    mapView.overlays.clear()
                    val marker = Marker(mapView).apply { position = geoPoint; setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); title = positionLabel }
                    mapView.overlays.add(marker); mapView.invalidate()
                }
            },
            modifier = modifier
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    @Composable
    fun SessionMapContainer(
        points: List<GpsPoint>, 
        unitSystem: String = "METRIC_KMH",
        modifier: Modifier = Modifier, 
        selectedPointIndex: Int = -1, 
        onPointTapped: (Int) -> Unit = {}
    ) {
        if (points.isEmpty()) return
        val startLabel = stringResource(R.string.marker_start); val endLabel = stringResource(R.string.marker_end)
        val pointFormat = stringResource(R.string.marker_point_format); val snippetFormat = stringResource(R.string.marker_snippet_format)
        
        val unitSystemEnum = when (unitSystem) {
            "METRIC_KMH" -> UnitSystem.METRIC_KMH
            "METRIC_MS" -> UnitSystem.METRIC_MS
            "IMPERIAL" -> UnitSystem.IMPERIAL
            "NAUTICAL" -> UnitSystem.NAUTICAL
            else -> UnitSystem.METRIC_KMH
        }

        AndroidView(
            factory = { ctx -> MapView(ctx).apply { setTileSource(TileSourceFactory.MAPNIK); setMultiTouchControls(true); controller.setZoom(SESSION_MAP_ZOOM) } },
            update = { mapView ->
                mapView.overlays.clear()
                val geoPoints = points.map { GeoPoint(it.latitude, it.longitude) }
                mapView.overlays.add(Polyline().apply { setPoints(geoPoints); outlinePaint.color = AndroidColor.rgb(33, 150, 243); outlinePaint.strokeWidth = SESSION_TRACE_LINE_WIDTH })
                mapView.overlays.add(Marker(mapView).apply { position = geoPoints.first(); setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); title = startLabel; icon = mapView.context.getDrawable(org.osmdroid.library.R.drawable.marker_default) })
                if (geoPoints.size > 1) mapView.overlays.add(Marker(mapView).apply { position = geoPoints.last(); setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); title = endLabel })
                if (selectedPointIndex in points.indices) {
                    val p = points[selectedPointIndex]
                    
                    val convertedSog = when (unitSystemEnum) {
                        UnitSystem.METRIC_KMH -> p.sog * 3.6f
                        UnitSystem.METRIC_MS -> p.sog
                        UnitSystem.IMPERIAL -> p.sog * 2.23694f
                        UnitSystem.NAUTICAL -> p.sog * 1.94384f
                    }
                    val speedUnit = when (unitSystemEnum) {
                        UnitSystem.METRIC_KMH -> "km/h"
                        UnitSystem.METRIC_MS -> "m/s"
                        UnitSystem.IMPERIAL -> "mph"
                        UnitSystem.NAUTICAL -> "kn"
                    }

                    val m = Marker(mapView).apply { 
                        position = GeoPoint(p.latitude, p.longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        title = pointFormat.format(p.id)
                        snippet = snippetFormat.format(convertedSog, speedUnit, p.cog) 
                    }
                    mapView.overlays.add(m); m.showInfoWindow()
                }
                if (geoPoints.size >= 2) try { mapView.post { mapView.zoomToBoundingBox(BoundingBox.fromGeoPoints(geoPoints), true, BOUNDING_BOX_PADDING) } } catch (_: Exception) { mapView.controller.setCenter(geoPoints.first()) }
                else mapView.controller.setCenter(geoPoints.first())
                mapView.setOnTouchListener { _, event ->
                    if (event.action == android.view.MotionEvent.ACTION_UP) {
                        val tappedGeo = mapView.projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                        var minDist = Double.MAX_VALUE; var nearestIdx = 0
                        for (i in points.indices) {
                            val d = tappedGeo.distanceToAsDouble(GeoPoint(points[i].latitude, points[i].longitude))
                            if (d < minDist) { minDist = d; nearestIdx = i }
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
}
