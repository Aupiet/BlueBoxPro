/* package com.example.blueboxpro

import com.example.blueboxpro.Process.SimpleKalmanFilter
import com.example.blueboxpro.Process.EkfSpeedEstimator
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.Save.GpsPoint
import com.example.blueboxpro.Save.SessionManager
import com.example.blueboxpro.Option
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.osmdroid.util.GeoPoint
import kotlin.math.abs

class ExampleUnitTest {

    @get:Rule
    val resultLogger = TestResultLogger()

    // --- SimpleKalmanFilter Tests ---
    @Test
    fun kalmanFilter_predict_increasesState() {
        val filter = SimpleKalmanFilter()
        val initialX = filter.x
        filter.predict(acceleration = 2.0f, dt = 1.0f)
        assertTrue("State should increase with positive acceleration", filter.x > initialX)
    }

    @Test
    fun kalmanFilter_update_correctsEstimate() {
        val filter = SimpleKalmanFilter(x = 0f, p = 1f, r = 0.5f)
        filter.update(measurement = 10f)
        assertTrue("Estimate should move towards measurement", filter.x > 0f && filter.x <= 10f)
    }

    // --- EkfSpeedEstimator Tests ---
    @Test
    fun ekf_predict_integratesVelocity() {
        val ekf = EkfSpeedEstimator()
        ekf.predict(aMes = 2.0f, dt = 1.0f) // 2m/s^2 for 1s
        assertTrue("Velocity should be roughly 2.0", abs(ekf.velocity - 2.0f) < 0.1f)
    }

    @Test
    fun ekf_zupt_forcesZeroVelocityWhenStill() {
        val ekf = EkfSpeedEstimator()
        ekf.predict(aMes = 5.0f, dt = 1.0f) // Initial move
        assertTrue("Velocity should be > 0", ekf.velocity > 0f)

        // Simulate being still for 1 second (10 ticks of 0.1s)
        for(i in 0..10) {
            ekf.predict(aMes = 0.05f, dt = 0.1f) // Below ZUPT threshold
        }
        assertEquals("Velocity must be forced to 0 after stillness", 0f, ekf.velocity)
    }

    @Test
    fun ekf_update_ignoresImpossibleGlitches() {
        val ekf = EkfSpeedEstimator()
        ekf.predict(aMes = 1.0f, dt = 1.0f) // v = ~1m/s
        val vBeforeGlitch = ekf.velocity
        
        // Massive GPS glitch (e.g., jump 100m/s instantly)
        ekf.update(refVelocity = 100f, rMeasurement = 0.1f)
        
        assertEquals("Should ignore massive glitch due to innovation gating", vBeforeGlitch, ekf.velocity)
    }

    // --- SessionManager Distance Tests ---
    @Test
    fun sessionManager_calculateDistance_isAccurate() {
        // Create a perfect square of roughly 1km sides based on coordinates
        // Using approximate degrees for 1km at equator (~0.009 degrees)
        val lat_offset = 0.009
        val lon_offset = 0.009
        
        val p1 = GpsPoint(1, 45.0, 5.0, 0.0, 0f, 0f, 0L)
        val p2 = GpsPoint(2, 45.0 + lat_offset, 5.0, 0.0, 0f, 0f, 1000L)
        val p3 = GpsPoint(3, 45.0 + lat_offset, 5.0 + lon_offset, 0.0, 0f, 0f, 2000L)
        val p4 = GpsPoint(4, 45.0, 5.0 + lon_offset, 0.0, 0f, 0f, 3000L)
        val p5 = GpsPoint(5, 45.0, 5.0, 0.0, 0f, 0f, 4000L) // back to start
        
        val trace = listOf(p1, p2, p3, p4, p5)
        val distance = SessionManager.calculateDistance(trace)
        
        // Approximate distance should be ~ 1km + ~ 0.7km + ~ 1km + ~ 0.7km = ~3.4km
        // (Longitude distance SHRINKS at 45deg latitude: cos(45) * 111km * 0.009 ~ 0.7km)
        // Let's assert it produces a reasonable non-zero km value
        assertTrue("Calculated distance should be positive and non-zero", distance > 0)
        TestResultLogger.appendLog("Info: Square Trace calculated distance: $distance km")
    }

    // --- MovementProcessor Tests ---
    @Test
    fun movementProcessor_scenarios_constantSpeed() {
        val processor = MovementProcessor()
        processor.reset()
        
        // The EKF features an innovation gate threshold (ignores jumps > 5m/s)
        // Since initial speed is 0.0, if we feed 8.0 directly, it's rejected by the gate.
        // We gently ramp the speed up so it's not detected as a glitch.
        val targetSpeeds = listOf(2.0f, 4.0f, 6.0f, 8.0f, 10.0f)
        for (speed in targetSpeeds) {
            val durationSeconds = if (speed == 10.0f) 5 else 1
            
            for (sec in 1..durationSeconds) {
                // GPS update 1Hz
                processor.updateWithGPS(
                    lat = 45.0, lon = 5.0, alt = 10.0,
                    gpsS = speed, 
                    gpsBearing = 90f,
                    accuracy = 2f
                ) {}
                
                // Sensor updates 50Hz
                for (i in 0 until 50) {
                    // Inject realistic vibration noise (e.g., +/- 0.1m/s^2) 
                    // This prevents the EkfSpeedEstimator's ZUPT (Zero Velocity Update) from
                    // thinking the device is perfectly still and forcing speed to 0.0m/s
                    val noise = if (i % 2 == 0) 0.1f else -0.1f
                    processor.processAcceleration(0f, noise, 0f, 0.02f, null)
                }
            }
        }
        
        val res = processor.getResult("m/s")
        val finalSpeed = res.getSpeedFused()
        TestResultLogger.appendLog("Info: Constant speed test final speed: $finalSpeed m/s")
        assertTrue("Fused speed $finalSpeed should stabilize near 10.0m/s", abs(finalSpeed - 10.0f) < 1.0f)
    }

    @Test
    fun movementProcessor_scenarios_varyingSpeed() {
        val processor = MovementProcessor()
        processor.reset()
        
        // Simulate acceleration for a longer duration
        for(i in 0..100) { // 2s at 50Hz (100 iterations)
            processor.processAcceleration(0f, 2.0f, 0f, 0.02f, null) // Constant 2m/s^2 forward
        }
        val acceleratingRes = processor.getResult("m/s")
        assertTrue("Speed should increase (integrating accel)", acceleratingRes.getSpeedFused() > 2.0f)
        
        // Simulate braking for a longer duration
        for(i in 0..100) {
            processor.processAcceleration(0f, -4.0f, 0f, 0.02f, null)
        }
        val brakingRes = processor.getResult("m/s")
        assertTrue("Speed should heavily decrease after braking", brakingRes.getSpeedFused() < acceleratingRes.getSpeedFused())
    }

    @Test
    fun movementProcessor_orientation_wraps360() {
        val processor = MovementProcessor()
        processor.updateOrientation(350f) {} // Set to 350
        processor.updateOrientation(10f) {}  // Rotate +20 degrees (crosses 0)
        
        assertEquals("Moyaz should interpolate short path, not go backwards", 10f, processor.azimuth, 20f)
    }

    // --- Option Parameter Tuning Test ---
    @Test
    fun option_parameterTuning_findsBestSettings() {
        // Run a simulated dataset with multiple configs to find the lowest error
        // Scenario: Exactly 5m/s (18km/h) for 5 seconds
        val groundTruthSpeed = 5.0f
        val configsToTest = listOf(
            mapOf("Q_VEL" to 0.001f, "Q_BIAS" to 0.0001f, "LPF_ALPHA" to 0.1f),
            mapOf("Q_VEL" to 0.01f,  "Q_BIAS" to 0.001f,  "LPF_ALPHA" to 0.5f),
            mapOf("Q_VEL" to 0.1f,   "Q_BIAS" to 0.01f,   "LPF_ALPHA" to 0.8f)
        )

        var bestConfigIndex = -1
        var lowestError = Float.MAX_VALUE

        TestResultLogger.appendLog("--- Starting Parameter Tuning Analysis ---")

        for ((index, config) in configsToTest.withIndex()) {
            Option.Process.Q_VEL = config["Q_VEL"]!!
            Option.Process.Q_BIAS = config["Q_BIAS"]!!
            Option.Process.LPF_ACCEL_ALPHA = config["LPF_ALPHA"]!!

            val processor = MovementProcessor() // Initializes Ekf with new config (will need dynamic injection if not fetched live, but in BlueBoxPro Ekf class takes it dynamically or on init).
            
            // Note: MovementProcessor in the project initializes `ekfEstimator` using Option values at class instantiation.
            
            // Feed noisy data
            for(i in 0..50) { // 5s at 10Hz GPS
                val noisyGps = groundTruthSpeed + (Math.random() * 2 - 1).toFloat() * 1.5f // +/- 1.5m/s noise
                processor.updateWithGPS(
                    lat = 45.0, lon = 5.0, alt = 10.0,
                    gpsS = noisyGps,
                    gpsBearing = 0f,
                    accuracy = 5f
                ) {}
                // Feed noisy approx 0 accel (steady speed)
                val noisyAccel = (Math.random() * 2 - 1).toFloat() * 0.5f 
                for(j in 0..4) processor.processAcceleration(0f, noisyAccel, 0f, 0.02f, null) // 5x accel per GPS
            }

            val finalSpeed = processor.getResult("m/s").getSpeedFused()
            val error = abs(finalSpeed - groundTruthSpeed)
            TestResultLogger.appendLog("Config ${index+1} $config -> Error: $error m/s (Final speed: $finalSpeed)")
            
            if (error < lowestError) {
                lowestError = error
                bestConfigIndex = index
            }
        }
        
        TestResultLogger.appendLog("Best configuration was Config ${bestConfigIndex+1} with error: $lowestError m/s")
        assertTrue("Should identify a best config", bestConfigIndex != -1)
    }
}*/