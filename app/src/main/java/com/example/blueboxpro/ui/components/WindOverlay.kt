/**
 * Custom OSMDroid Overlay that renders wind vectors as directional arrows on the map.
 * Each arrow's length is proportional to wind speed, direction follows meteorological convention.
 */
package com.example.blueboxpro.ui.components

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Represents a single wind measurement at a geographic point.
 * @param direction Meteorological direction: where the wind comes FROM (degrees).
 */
data class WindVector(
    val lat: Double,
    val lon: Double,
    val speed: Float,
    val direction: Float
)

/**
 * Overlay that draws wind arrows on the map canvas.
 */
class WindOverlay : Overlay() {

    private var vectors: List<WindVector> = emptyList()

    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val headPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 2f
    }

    private val speedTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 255, 255, 255)
        textSize = 22f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        setShadowLayer(3f, 1f, 1f, Color.argb(150, 0, 0, 0))
    }

    /** Replace all vectors and request a redraw. */
    fun updateVectors(newVectors: List<WindVector>) {
        vectors = newVectors
    }

    /** Clear all vectors. */
    fun clear() {
        vectors = emptyList()
    }
    
    /** Returns a color based on wind speed in knots */
    fun getWindColor(speedKnots: Float): Int {
        return when {
            speedKnots < 5f -> Color.argb(255, 0, 150, 255)       // Blue
            speedKnots < 10f -> Color.argb(255, 0, 200, 150)      // Teal
            speedKnots < 15f -> Color.argb(255, 0, 220, 0)        // Green
            speedKnots < 20f -> Color.argb(255, 150, 220, 0)      // Yellow-Green
            speedKnots < 25f -> Color.argb(255, 220, 200, 0)      // Yellow
            speedKnots < 30f -> Color.argb(255, 255, 120, 0)      // Orange
            speedKnots < 35f -> Color.argb(255, 255, 50, 0)       // Red-Orange
            speedKnots < 40f -> Color.argb(255, 220, 0, 50)       // Red
            else -> Color.argb(255, 150, 0, 150)                  // Purple
        }
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow || vectors.isEmpty()) return

        val projection = mapView.projection
        val point = android.graphics.Point()
        val arrowPath = Path()

        for (v in vectors) {
            // Project geo → screen
            projection.toPixels(GeoPoint(v.lat, v.lon), point)
            val cx = point.x.toFloat()
            val cy = point.y.toFloat()

            // Skip if outside visible area (with margin)
            if (cx < -50 || cy < -50 || cx > canvas.width + 50 || cy > canvas.height + 50) continue

            val color = getWindColor(v.speed)
            val scale = com.example.blueboxpro.Option.UI.windArrowSize
            arrowPaint.color = color
            arrowPaint.strokeWidth = 4f * scale
            headPaint.color = color

            // Arrow length: base 15px + scale by speed (capped at 50px)
            val length = (15f + min(v.speed * 2f, 50f)) * scale

            // Meteorological convention: direction = where wind comes FROM
            // Arrow points in the direction the wind GOES TO = direction + 180°
            val windToRad = Math.toRadians((v.direction + 180.0) % 360.0)
            val dx = sin(windToRad).toFloat()
            val dy = -cos(windToRad).toFloat()  // Screen Y is inverted

            // Arrow shaft: from center to tip
            val tipX = cx + dx * length
            val tipY = cy + dy * length

            canvas.drawLine(cx, cy, tipX, tipY, arrowPaint)

            // Arrowhead
            val headLen = 8f * scale
            val headAngle = Math.toRadians(25.0)
            val backAngle1 = windToRad + Math.PI - headAngle
            val backAngle2 = windToRad + Math.PI + headAngle

            arrowPath.reset()
            arrowPath.moveTo(tipX, tipY)
            arrowPath.lineTo(
                tipX + (sin(backAngle1).toFloat() * headLen),
                tipY + (-cos(backAngle1).toFloat() * headLen)
            )
            arrowPath.lineTo(
                tipX + (sin(backAngle2).toFloat() * headLen),
                tipY + (-cos(backAngle2).toFloat() * headLen)
            )
            arrowPath.close()
            canvas.drawPath(arrowPath, headPaint)

            // Speed label near base
            canvas.drawText(
                "%.0f".format(v.speed),
                cx,
                cy - 8f,
                speedTextPaint
            )
        }
    }
}
