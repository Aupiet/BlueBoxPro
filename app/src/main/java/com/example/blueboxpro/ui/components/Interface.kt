/**
 * Reusable UI components for map displays, navigation panels, and spatial data visualization.
 * Also includes standard containers and styling for consistency across pages.
 */
package com.example.blueboxpro.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.blueboxpro.Process.Converter
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.Process.UnitSystem
import com.example.blueboxpro.Process.WeatherData
import com.example.blueboxpro.Process.WeatherManager
import com.example.blueboxpro.R
import com.example.blueboxpro.Save.GpsPoint
import com.example.blueboxpro.Save.SessionManager
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.TilesOverlay
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.awaitCancellation
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import androidx.core.graphics.drawable.toDrawable

/**
 * A standard card container used throughout the app to group information.
 */
@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

/**
 * A standard header for sections.
 */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = color,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(start = 4.dp, bottom = 8.dp, top = 0.dp)
    )
}

/**
 * A card for dashboard-like displays.
 */
@Composable
fun DashboardCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

/**
 * Collection of map-related UI components and layout logic.
 */
object MapComponents {
    private const val DEFAULT_ZOOM_LEVEL = 17.0
    private const val SESSION_MAP_ZOOM = 16.0
    private const val BOUNDING_BOX_PADDING = 50
    private const val LAT_LON_FORMAT = "%.6f"
    private const val STAT_FORMAT = "%.1f"
    private const val DEGREE_FORMAT = "%.0f"

    private const val COMPASS_RING_WIDTH_RATIO = 0.12f
    private const val COMPASS_ANIM_DURATION_MS = 300
    
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

    @Composable
    fun Page2Layout(
        location: GeoPoint?,
        processor: MovementProcessor,
        unitSystem: String,
        weatherData: WeatherData? = null,
        onBack: () -> Unit,
        onMapClick: () -> Unit
    ) {
        val result = processor.getResult(unitSystem)
        val activeRecording = SessionManager.activeRecording
        val recordingPoints = activeRecording?.points ?: emptyList()
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1.2f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "COG: ${DEGREE_FORMAT.format(result.getCog())}°", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        CircularMapWithCompass(location, result.getCog(), result.getAzimuth(), recordingPoints, onMapClick)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(2.2f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                    Column(modifier = Modifier.padding(bottom = 4.dp)) {
                        CoordinateText("Latitude", LAT_LON_FORMAT.format(location?.latitude ?: 0.0), fontSize = 10.sp)
                        CoordinateText("Altitude", "${STAT_FORMAT.format(result.getAltitude())} ${result.getAltitudeUnit()}", fontSize = 10.sp)
                        CoordinateText("Longitude", LAT_LON_FORMAT.format(location?.longitude ?: 0.0), fontSize = 10.sp)
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            CircularGaugeWithDirectionPoint("SOG (${result.getSpeedUnit()})", STAT_FORMAT.format(result.getSog()), emptyList(), 90, MaterialTheme.colorScheme.tertiary)
                            val windS = weatherData?.currentWeather?.windSpeed?.let { 
                                val conv = Converter.convertSpeed(it.toFloat() / 3.6f, unitSystem)
                                STAT_FORMAT.format(conv)
                            } ?: "..."
                            CircularGaugeWithDirectionPoint("VENT (${result.getSpeedUnit()})", windS, emptyList(), 90, MaterialTheme.colorScheme.tertiary)
                            val windD = weatherData?.currentWeather?.windDirection?.toFloat()
                            val dirPoints = mutableListOf<DirectionPoint>()
                            windD?.let { dirPoints.add(DirectionPoint(angle = it, color = MaterialTheme.colorScheme.error)) }
                            dirPoints.add(DirectionPoint(angle = result.getCog(), color = MaterialTheme.colorScheme.primary))
                            CircularGaugeWithDirectionPoint("DIR. (°)", windD?.let { "${it.toInt()}" } ?: "...", dirPoints, 90, MaterialTheme.colorScheme.tertiary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            val temp = weatherData?.currentWeather?.temperature?.let { "${it.toInt()}" } ?: "..."
                            CircularGaugeWithDirectionPoint("TEMP (°C)", temp, emptyList(), 90, MaterialTheme.colorScheme.primary)
                            CircularGaugeWithDirectionPoint("PITCH (°)", "${result.getPitch()}", emptyList(), 90, MaterialTheme.colorScheme.primary)
                            CircularGaugeWithDirectionPoint("ROLL (°)", "${result.getRoll()}", emptyList(), 90, MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "COG: ${DEGREE_FORMAT.format(result.getCog())}°", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(0.8f).aspectRatio(1f)) {
                    CircularMapWithCompass(location, result.getCog(), result.getAzimuth(), recordingPoints, onMapClick)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text(text = "Long: ${LAT_LON_FORMAT.format(location?.longitude ?: 0.0)}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp))
                    Text(text = "Lat: ${LAT_LON_FORMAT.format(location?.latitude ?: 0.0)}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp))
                    Text(text = "Alt: ${STAT_FORMAT.format(result.getAltitude())}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        CircularGaugeWithDirectionPoint("SOG (${result.getSpeedUnit()})", STAT_FORMAT.format(result.getSog()), emptyList(), 78, MaterialTheme.colorScheme.primary)
                        val windS = weatherData?.currentWeather?.windSpeed?.let { 
                            val conv = Converter.convertSpeed(it.toFloat() / 3.6f, unitSystem)
                            STAT_FORMAT.format(conv)
                        } ?: "..."
                        CircularGaugeWithDirectionPoint("VENT (${result.getSpeedUnit()})", windS, emptyList(), 78, MaterialTheme.colorScheme.tertiary)
                        val windD = weatherData?.currentWeather?.windDirection?.toFloat()
                        val dirPoints = mutableListOf<DirectionPoint>()
                        windD?.let { dirPoints.add(DirectionPoint(angle = it, color = MaterialTheme.colorScheme.error)) }
                        dirPoints.add(DirectionPoint(angle = result.getCog(), color = MaterialTheme.colorScheme.secondary))
                        CircularGaugeWithDirectionPoint("DIR. (°)", windD?.let { "${it.toInt()}" } ?: "...", dirPoints, 78, MaterialTheme.colorScheme.tertiary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        CircularGaugeWithDirectionPoint("PITCH (°)", "${result.getPitch()}", emptyList(), 78, MaterialTheme.colorScheme.primary)
                        CircularGaugeWithDirectionPoint("ROLL (°)", "${result.getRoll()}", emptyList(), 78, MaterialTheme.colorScheme.secondary)
                        val temp = weatherData?.currentWeather?.temperature?.let { "${it.toInt()}" } ?: "..."
                        CircularGaugeWithDirectionPoint("TEMP (°C)", temp, emptyList(), 78, MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    @Composable
    private fun CoordinateText(label: String, value: String, fontSize: androidx.compose.ui.unit.TextUnit) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 0.5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize), fontWeight = FontWeight.SemiBold)
            Text(text = value, style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize))
        }
    }

    data class DirectionPoint(val angle: Float, val sizeDp: Float = 4f, val color: Color = Color.Red)

    @Composable
    fun CircularGaugeWithDirectionPoint(
        label: String, 
        value: String, 
        points: List<DirectionPoint>, 
        gaugeSize: Int, 
        color: Color = MaterialTheme.colorScheme.onSurface,
        labelFontSize: androidx.compose.ui.unit.TextUnit = 8.sp,
        valueFontSize: androidx.compose.ui.unit.TextUnit = 32.sp,
        circleStrokeWidth: Float = 2f
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(gaugeSize.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.width / 2
                    drawCircle(color = color, style = Stroke(width = circleStrokeWidth.dp.toPx()))
                    
                    for (point in points) {
                        val angleRad = Math.toRadians(point.angle.toDouble() - 90.0)
                        val dotRadius = point.sizeDp.dp.toPx()
                        val dotX = (size.width / 2) + (radius) * cos(angleRad).toFloat()
                        val dotY = (size.height / 2) + (radius) * sin(angleRad).toFloat()
                        drawCircle(color = point.color, radius = dotRadius, center = Offset(dotX, dotY))
                    }
                }
                Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontSize = valueFontSize), fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center)
            }
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = labelFontSize), color = color)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Composable
    private fun CircularMapWithCompass(location: GeoPoint?, cog: Float, azimuth: Float, recordingPoints: List<GpsPoint>, onMapClick: () -> Unit) {
        val animatedCog by animateFloatAsState(targetValue = cog, animationSpec = tween(durationMillis = COMPASS_ANIM_DURATION_MS), label = "cog")
        val primaryColor = MaterialTheme.colorScheme.primary; val onSurfaceColor = MaterialTheme.colorScheme.onSurface; val surfaceColor = MaterialTheme.colorScheme.surface; val errorColor = MaterialTheme.colorScheme.error; val textMeasurer = rememberTextMeasurer()
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.fillMaxSize().clip(CircleShape)) {
                AndroidView(factory = { ctx -> MapView(ctx).apply { setTileSource(TileSourceFactory.MAPNIK); maxZoomLevel = 17.0; setMultiTouchControls(false); setOnTouchListener { _, _ -> true }; controller.setZoom(DEFAULT_ZOOM_LEVEL); location?.let { controller.setCenter(it) } } },
                    update = { mapView ->
                        mapView.mapOrientation = -animatedCog
                        mapView.overlays.removeAll { it is Marker || it is Polyline }
                        if (recordingPoints.isNotEmpty()) {
                            val tracePoints = recordingPoints.map { GeoPoint(it.latitude, it.longitude) }
                            mapView.overlays.add(Polyline().apply { setPoints(tracePoints); outlinePaint.color = primaryColor.toArgb(); outlinePaint.strokeWidth = TRACE_LINE_WIDTH })
                        }
                        location?.let { geoPoint ->
                            mapView.controller.setCenter(geoPoint)
                            val triangleBitmap = createTriangleBitmap(POSITION_MARKER_SIZE, 0f, primaryColor.toArgb())
                            mapView.overlays.add(Marker(mapView).apply { position = geoPoint; setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER); icon =
                                triangleBitmap.toDrawable(mapView.context.resources); setInfoWindow(null) })
                        }
                        mapView.invalidate()
                    }, modifier = Modifier.fillMaxSize())
                Box(modifier = Modifier.fillMaxSize().background(Color.Transparent).clickable { onMapClick() })
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2; val cy = size.height / 2; val outerRadius = min(cx, cy); val ringWidth = outerRadius * COMPASS_RING_WIDTH_RATIO; val midRadius = outerRadius - ringWidth / 2; val innerRadius = outerRadius - ringWidth
                drawCircle(color = surfaceColor.copy(alpha = COMPASS_BACKGROUND_ALPHA), radius = outerRadius, center = Offset(cx, cy), style = Stroke(width = ringWidth))
                drawCircle(color = onSurfaceColor.copy(alpha = COMPASS_BORDER_ALPHA), radius = outerRadius, center = Offset(cx, cy), style = Stroke(width = COMPASS_BORDER_WIDTH))
                drawCircle(color = onSurfaceColor.copy(alpha = COMPASS_BORDER_ALPHA), radius = innerRadius, center = Offset(cx, cy), style = Stroke(width = COMPASS_BORDER_WIDTH))
                for (i in 0 until 360 step COMPASS_TICK_STEP) {
                    val angleRad = Math.toRadians((i - animatedCog).toDouble()).toFloat()
                    val isMajor = i % COMPASS_MAJOR_TICK_STEP == 0; val isMid = i % COMPASS_MID_TICK_STEP == 0
                    val tickInner = if (isMajor) innerRadius + ringWidth * COMPASS_MAJOR_TICK_LENGTH_RATIO else if (isMid) innerRadius + ringWidth * COMPASS_MID_TICK_LENGTH_RATIO else innerRadius + ringWidth * COMPASS_MINOR_TICK_LENGTH_RATIO
                    val tickOuter = outerRadius - ringWidth * COMPASS_TICK_OUTER_GAP_RATIO
                    drawLine(color = onSurfaceColor.copy(alpha = if (isMajor) 0.9f else if (isMid) 0.5f else 0.25f), start = Offset(cx + sin(angleRad) * tickInner, cy - cos(angleRad) * tickInner), end = Offset(cx + sin(angleRad) * tickOuter, cy - cos(angleRad) * tickOuter), strokeWidth = if (isMajor) COMPASS_MAJOR_TICK_WIDTH else COMPASS_MINOR_TICK_WIDTH, cap = StrokeCap.Round)
                }
                val cardinals = mapOf(0f to "N", 45f to "NE", 90f to "E", 135f to "SE", 180f to "S", 225f to "SW", 270f to "W", 315f to "NW")
                for (i in 0 until 360 step COMPASS_MAJOR_TICK_STEP) {
                    val angleRad = Math.toRadians((i - animatedCog).toDouble()).toFloat()
                    val cardinalLabel = cardinals[i.toFloat()]; val displayText = cardinalLabel ?: "${i}°"; val isCardinal = cardinalLabel != null && cardinalLabel.length == 1
                    val style = TextStyle(fontWeight = if (isCardinal) FontWeight.ExtraBold else FontWeight.Medium, fontSize = if (isCardinal) 14.sp else 9.sp, color = if (cardinalLabel == "N") errorColor else onSurfaceColor)
                    val measured = textMeasurer.measure(displayText, style)
                    drawText(textLayoutResult = measured, topLeft = Offset(cx + sin(angleRad) * midRadius - measured.size.width / 2f, cy - cos(angleRad) * midRadius - measured.size.height / 2f))
                }
                val indicatorPath = Path().apply { moveTo(cx, cy - outerRadius - INDICATOR_OFFSET_PX); lineTo(cx - INDICATOR_HALF_WIDTH_PX, cy - innerRadius + INDICATOR_OFFSET_PX); lineTo(cx + INDICATOR_HALF_WIDTH_PX, cy - innerRadius + INDICATOR_OFFSET_PX); close() }
                drawPath(indicatorPath, color = primaryColor)
            }
        }
    }

    private fun createTriangleBitmap(size: Int, rotation: Float, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888); val canvas = AndroidCanvas(bitmap); val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }; val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = AndroidColor.WHITE; style = Paint.Style.STROKE; strokeWidth = OUTLINE_STROKE_WIDTH }
        canvas.save(); canvas.rotate(rotation, size / 2f, size / 2f)
        val path = android.graphics.Path().apply { moveTo(size / 2f, size * TRIANGLE_TOP_MARGIN_RATIO); lineTo(size * TRIANGLE_SIDE_MARGIN_RATIO, size * TRIANGLE_BOTTOM_MARGIN_RATIO); lineTo(size * (1f - TRIANGLE_SIDE_MARGIN_RATIO), size * TRIANGLE_BOTTOM_MARGIN_RATIO); close() }
        canvas.drawPath(path, paint); canvas.drawPath(path, outlinePaint); canvas.restore(); return bitmap
    }

    @SuppressLint("ClickableViewAccessibility")
    @Composable
    fun MapContainer(
        location: GeoPoint?,
        modifier: Modifier = Modifier,
        zoomLevel: Double = DEFAULT_ZOOM_LEVEL,
        isLocked: Boolean = true,
        recenterTrigger: Int = 0,
        showTopoLayer: Boolean = false,
        showPrecipLayer: Boolean = false,
        showWindLayer: Boolean = false
    ) {
        val positionLabel = stringResource(R.string.marker_position)
        val topoInfo = remember { WeatherManager.topoLayerInfo }

        // Keep references to MapView and overlay instances for proper lifecycle
        var mapViewRef by remember { mutableStateOf<MapView?>(null) }
        var topoOverlayRef by remember { mutableStateOf<TilesOverlay?>(null) }
        var precipOverlayRef by remember { mutableStateOf<TilesOverlay?>(null) }
        var windOverlayRef by remember { mutableStateOf<WindOverlay?>(null) }

        // Recenter without recreating the MapView
        LaunchedEffect(recenterTrigger) {
            if (recenterTrigger > 0) {
                location?.let { mapViewRef?.controller?.animateTo(it) }
            }
        }

        // --- Topo overlay: add/remove by tracked reference ---
        LaunchedEffect(showTopoLayer, mapViewRef) {
            val mv = mapViewRef ?: return@LaunchedEffect
            // Always remove the old overlay if it exists
            topoOverlayRef?.let { mv.overlays.remove(it) }
            topoOverlayRef = null
            if (showTopoLayer) {
                val source = XYTileSource(
                    topoInfo.name, topoInfo.minZoom, topoInfo.maxZoom,
                    topoInfo.tileSize, topoInfo.extension, topoInfo.baseUrls
                )
                val provider = MapTileProviderBasic(mv.context, source)
                val overlay = TilesOverlay(provider, mv.context)
                mv.overlays.add(0, overlay)
                topoOverlayRef = overlay
            }
            mv.invalidate()
        }

        // --- Precip overlay: fetch RainViewer URL, add/remove by tracked reference ---
        LaunchedEffect(showPrecipLayer, mapViewRef) {
            val mv = mapViewRef ?: return@LaunchedEffect
            // Always remove the old overlay if it exists
            precipOverlayRef?.let { mv.overlays.remove(it) }
            precipOverlayRef = null
            if (showPrecipLayer) {
                val tileBase = WeatherManager.fetchRainViewerTileBase()
                if (tileBase != null) {
                    val info = WeatherManager.buildPrecipLayerInfo(tileBase)
                    val source = XYTileSource(
                        info.name, info.minZoom, info.maxZoom,
                        info.tileSize, info.extension, info.baseUrls
                    )
                    val provider = MapTileProviderBasic(mv.context, source)
                    val overlay = TilesOverlay(provider, mv.context)
                    overlay.loadingBackgroundColor = android.graphics.Color.TRANSPARENT
                    overlay.loadingLineColor = android.graphics.Color.TRANSPARENT
                    mv.overlays.add(overlay)
                    precipOverlayRef = overlay
                }
            }
            mv.invalidate()
        }

        // --- Wind overlay: tracks map position + fetch ---
        LaunchedEffect(showWindLayer, mapViewRef) {
            val mv = mapViewRef ?: return@LaunchedEffect
            windOverlayRef?.let { mv.overlays.remove(it) }
            windOverlayRef = null

            if (showWindLayer) {
                val overlay = WindOverlay()
                mv.overlays.add(overlay)
                windOverlayRef = overlay

                var fetchJob: Job? = null

                val listener = object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean { triggerFetch(); return false }
                    override fun onZoom(event: ZoomEvent?): Boolean { triggerFetch(); return false }

                    fun triggerFetch() {
                        fetchJob?.cancel()
                        fetchJob = launch {
                            delay(500) // Debounce
                            val bbox = mv.boundingBox ?: return@launch
                            
                            val logicZoom = mv.zoomLevelDouble
                            val minZoom = com.example.blueboxpro.Option.UI.windMinZoom
                            
                            if (logicZoom < minZoom) {
                                overlay.clear()
                                mv.invalidate()
                                return@launch
                            }
                            
                            val visualDensity = com.example.blueboxpro.Option.UI.windDensity
                            val ttl = com.example.blueboxpro.Option.UI.windCacheTtlMinutes
                            
                            val denseVectors = WindChunkManager.getVisibleWindVectors(
                                bbox = bbox,
                                actualZoom = logicZoom,
                                ttlMinutes = ttl,
                                apiDensityCols = 3,
                                apiDensityRows = 3,
                                visualDensity = visualDensity
                            )
                            
                            overlay.updateVectors(denseVectors)
                            mv.invalidate()
                        }
                    }
                }
                mv.addMapListener(listener)
                listener.triggerFetch()

                try {
                    awaitCancellation()
                } finally {
                    mv.removeMapListener(listener)
                    mv.overlays.remove(overlay)
                    mv.invalidate()
                }
            }
        }

        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    maxZoomLevel = 17.0
                    setMultiTouchControls(!isLocked)
                    if (isLocked) setOnTouchListener { _, _ -> true }
                    controller.setZoom(zoomLevel)
                    location?.let { controller.setCenter(it) }
                    mapViewRef = this
                    // Scale bar
                    overlays.add(ScaleBarOverlay(this).apply {
                        setCentred(true)
                        setAlignBottom(true)
                    })
                }
            },
            update = { mapView ->
                mapViewRef = mapView
                // Only remove markers (position), keep tile overlays and scale bar stable
                mapView.overlays.removeAll { it is Marker }

                // --- Position marker ---
                location?.let { geoPoint ->
                    if (recenterTrigger == 0) mapView.controller.setCenter(geoPoint)
                    val marker = Marker(mapView).apply {
                        position = geoPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = positionLabel
                    }
                    mapView.overlays.add(marker)
                }
                mapView.invalidate()
            },
            modifier = modifier
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    @Composable
    fun SessionMapContainer(points: List<GpsPoint>, unitSystem: String = "METRIC_KMH", modifier: Modifier = Modifier, selectedPointIndex: Int = -1, onPointTapped: (Int) -> Unit = {}) {
        if (points.isEmpty()) return
        val primaryColor = MaterialTheme.colorScheme.primary
        val startLabel = stringResource(R.string.marker_start); val endLabel = stringResource(R.string.marker_end); val pointFormat = stringResource(R.string.marker_point_format); val snippetFormat = stringResource(R.string.marker_snippet_format)
        val unitSystemEnum = when (unitSystem) { "METRIC_KMH" -> UnitSystem.METRIC_KMH; "METRIC_MS" -> UnitSystem.METRIC_MS; "IMPERIAL" -> UnitSystem.IMPERIAL; "NAUTICAL" -> UnitSystem.NAUTICAL; else -> UnitSystem.METRIC_KMH }
        AndroidView(factory = { ctx -> MapView(ctx).apply { setTileSource(TileSourceFactory.MAPNIK); setMultiTouchControls(true); controller.setZoom(SESSION_MAP_ZOOM) } }, update = { mapView ->
            mapView.overlays.clear(); val geoPoints = points.map { GeoPoint(it.latitude, it.longitude) }; mapView.overlays.add(Polyline().apply { setPoints(geoPoints); outlinePaint.color = primaryColor.toArgb(); outlinePaint.strokeWidth = SESSION_TRACE_LINE_WIDTH }); mapView.overlays.add(Marker(mapView).apply { position = geoPoints.first(); setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); title = startLabel; icon = AppCompatResources.getDrawable(mapView.context, org.osmdroid.library.R.drawable.marker_default) })
            if (geoPoints.size > 1) mapView.overlays.add(Marker(mapView).apply { position = geoPoints.last(); setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); title = endLabel })
            if (selectedPointIndex in points.indices) {
                val p = points[selectedPointIndex]; val convertedSog = when (unitSystemEnum) { UnitSystem.METRIC_KMH -> p.sog * 3.6f; UnitSystem.METRIC_MS -> p.sog; UnitSystem.IMPERIAL -> p.sog * 2.23694f; UnitSystem.NAUTICAL -> p.sog * 1.94384f; else -> p.sog }; val speedUnit = when (unitSystemEnum) { UnitSystem.METRIC_KMH -> "km/h"; UnitSystem.METRIC_MS -> "m/s"; UnitSystem.IMPERIAL -> "mph"; UnitSystem.NAUTICAL -> "kn"; else -> "km/h" }
                val m = Marker(mapView).apply { position = GeoPoint(p.latitude, p.longitude); setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER); title = pointFormat.format(p.id); snippet = snippetFormat.format(convertedSog, speedUnit, p.cog) }; mapView.overlays.add(m); m.showInfoWindow()
            }
            if (geoPoints.size >= 2) try { mapView.post { mapView.zoomToBoundingBox(BoundingBox.fromGeoPoints(geoPoints), true, BOUNDING_BOX_PADDING) } } catch (_: Exception) { mapView.controller.setCenter(geoPoints.first()) } else mapView.controller.setCenter(geoPoints.first())
            mapView.setOnTouchListener { _, event -> if (event.action == android.view.MotionEvent.ACTION_UP) { val tappedGeo = mapView.projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint; var minDist = Double.MAX_VALUE; var nearestIdx = 0; for (i in points.indices) { val d = tappedGeo.distanceToAsDouble(GeoPoint(points[i].latitude, points[i].longitude)); if (d < minDist) { minDist = d; nearestIdx = i } }; onPointTapped(nearestIdx) }; false }
            mapView.invalidate()
        }, modifier = modifier)
    }
}
