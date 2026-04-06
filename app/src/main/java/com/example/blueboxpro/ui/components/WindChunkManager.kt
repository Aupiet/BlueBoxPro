package com.example.blueboxpro.ui.components

import com.example.blueboxpro.Process.WeatherManager
import kotlinx.coroutines.*
import org.osmdroid.util.BoundingBox
import java.util.concurrent.ConcurrentHashMap

/**
 * Key representing a map tile chunk at a specific zoom level.
 */
data class ChunkKey(val z: Int, val x: Int, val y: Int)

/**
 * Cached wind data for a specific chunk.
 */
data class CachedChunk(
    val vectors: List<WindVector>,
    val timestampMs: Long
)

/**
 * Manages chunk-based caching and fetching of wind data to optimize Open-Meteo API usage.
 */
object WindChunkManager {
    private val cache = ConcurrentHashMap<ChunkKey, CachedChunk>()
    private val inFlightRequests = ConcurrentHashMap<ChunkKey, Deferred<List<WindVector>>>()

    /**
     * Clear all cached chunks.
     */
    fun clearCache() {
        cache.clear()
        inFlightRequests.clear()
    }

    /**
     * Converts a lat/lon coordinate to an OSM slippy map X tile index.
     */
    private fun lonToX(lon: Double, z: Int): Int {
        val n = 1 shl z
        return ((lon + 180.0) / 360.0 * n).toInt()
    }

    /**
     * Converts a lat/lon coordinate to an OSM slippy map Y tile index.
     */
    private fun latToY(lat: Double, z: Int): Int {
        val n = 1 shl z
        val latRad = Math.toRadians(lat)
        return ((1.0 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI) / 2.0 * n).toInt()
    }
    
    /**
     * Converts an OSM slippy map X tile index to longitude.
     */
    private fun xToLon(x: Int, z: Int): Double {
        val n = 1 shl z
        return x / n.toDouble() * 360.0 - 180.0
    }

    /**
     * Converts an OSM slippy map Y tile index to latitude.
     */
    private fun yToLat(y: Int, z: Int): Double {
        val n = 1 shl z
        val nPi = Math.PI - 2.0 * Math.PI * y / n.toDouble()
        return Math.toDegrees(Math.atan(0.5 * (Math.exp(nPi) - Math.exp(-nPi))))
    }

    /**
     * Retrieves visible wind vectors for the given bounding box and zoom level.
     * Combines cached and newly fetched chunks.
     */
    suspend fun getVisibleWindVectors(
        bbox: BoundingBox,
        actualZoom: Double,
        ttlMinutes: Int,
        apiDensityCols: Int = 3,
        apiDensityRows: Int = 3,
        visualDensity: Int = 15
    ): List<WindVector> = coroutineScope {
        val currentTime = System.currentTimeMillis()
        val ttlMs = ttlMinutes * 60 * 1000L

        // To prevent massive API calls at high zooms, we cap the logic zoom
        // at Z=6 (macro weather level). All zooms above 6 share the same macro chunks.
        val logicZoom = Math.min(actualZoom.toInt(), 6)

        // Find visible tile edges
        val xMin = lonToX(bbox.lonWest, logicZoom)
        val xMax = lonToX(bbox.lonEast, logicZoom)
        val yMin = latToY(bbox.latNorth, logicZoom)
        val yMax = latToY(bbox.latSouth, logicZoom)

        val neededChunks = mutableListOf<ChunkKey>()
        for (x in xMin..xMax) {
            for (y in yMin..yMax) {
                neededChunks.add(ChunkKey(logicZoom, x, y))
            }
        }

        // Prepare deferred tasks for chunks we need to fetch
        val fetchTasks = mutableListOf<Deferred<List<WindVector>>>()

        for (chunk in neededChunks) {
            val cached = cache[chunk]
            if (cached != null && (currentTime - cached.timestampMs < ttlMs)) {
                // Cache hit and valid
                continue
            }

            // Cache miss or expired. Fetch if not already in-flight.
            synchronized(inFlightRequests) {
                var inFlight = inFlightRequests[chunk]
                if (inFlight == null) {
                    val latNorth = yToLat(chunk.y, chunk.z)
                    val latSouth = yToLat(chunk.y + 1, chunk.z)
                    val lonWest = xToLon(chunk.x, chunk.z)
                    val lonEast = xToLon(chunk.x + 1, chunk.z)

                    inFlight = async(Dispatchers.IO) {
                        try {
                            val rawVectors = WeatherManager.fetchWindGrid(
                                latSouth, latNorth, lonWest, lonEast,
                                apiDensityCols, apiDensityRows
                            )
                            if (rawVectors.isNotEmpty()) {
                                cache[chunk] = CachedChunk(rawVectors, System.currentTimeMillis())
                            }
                            rawVectors
                        } catch (e: Exception) {
                            emptyList()
                        } finally {
                            inFlightRequests.remove(chunk)
                        }
                    }
                    inFlightRequests[chunk] = inFlight
                }
                fetchTasks.add(inFlight)
            }
        }

        // Wait for all missing chunks to fetch
        fetchTasks.awaitAll()

        // Combine all valid chunks currently in the target bounding box
        val allRawVectors = mutableListOf<WindVector>()
        for (chunk in neededChunks) {
            cache[chunk]?.let { allRawVectors.addAll(it.vectors) }
        }

        if (allRawVectors.isEmpty()) return@coroutineScope emptyList()

        // Now interpolate locally the dense visual grid matching strictly the screen bbox
        val targetCols = visualDensity
        val targetRows = (visualDensity * 1.2).toInt()
        
        return@coroutineScope WeatherManager.interpolateWindGrid(
            allRawVectors,
            bbox.latSouth, bbox.latNorth,
            bbox.lonWest, bbox.lonEast,
            targetCols, targetRows
        )
    }
}
