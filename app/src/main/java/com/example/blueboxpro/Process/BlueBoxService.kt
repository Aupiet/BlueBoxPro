package com.example.blueboxpro.Process

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.compose.runtime.snapshotFlow
import com.example.blueboxpro.Option
import com.example.blueboxpro.Save.SessionManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.system.exitProcess

/**
 * Foreground service to maintain sensor data collection and processing
 * even when the application is in the background.
 */
class BlueBoxService : Service() {

    private val binder = LocalBinder()
    
    // Core logic components moved here for background persistence
    val processor = MovementProcessor()
    private lateinit var captorListener: CaptorListener
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var updateJob: Job? = null
    private var backgroundTimeoutJob: Job? = null

    // For notification updates
    var lastWeatherData: WeatherData? = null
    var unitSystem: String = Option.UI.unitSystem
    
    var isAppInBackground: Boolean = false
        set(value) {
            field = value
            updateTimeoutLogic()
        }

    private var lastNotificationTime: Long = 0
    private var lastNotificationContent: String = ""
    private var lastIsRecording: Boolean = false

    inner class LocalBinder : Binder() {
        fun getService(): BlueBoxService = this@BlueBoxService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        
        captorListener = CaptorListener(this, processor) {
            // Callback on each sensor update
            checkAndNotify()
            SessionManager.updateRecording(processor)
        }

        // Observe recording state changes to update notification and timeout logic
        snapshotFlow { SessionManager.activeRecording != null }
            .distinctUntilChanged()
            .onEach { isRecording ->
                checkAndNotify() // Force update notification when state changes
                updateTimeoutLogic()
            }
            .launchIn(serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NotificationHelper.NOTIFICATION_ID, NotificationHelper.buildNotification(this))
        
        captorListener.start()
        startNotificationUpdates()
        
        return START_STICKY
    }

    private fun startNotificationUpdates() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                checkAndNotify()
                delay(2000) // Base check every 2 seconds
            }
        }
    }

    private fun updateTimeoutLogic() {
        val isRecording = SessionManager.activeRecording != null
        
        if (isAppInBackground && !isRecording) {
            // Only start/restart timeout if in background and NOT recording
            startBackgroundTimeout()
        } else {
            // Cancel timeout if app is in foreground OR if recording is active
            cancelBackgroundTimeout()
        }
    }

    private fun startBackgroundTimeout() {
        backgroundTimeoutJob?.cancel()
        backgroundTimeoutJob = serviceScope.launch {
            delay(5 * 60 * 1000L) // 5 minutes
            // Final check before stopping
            if (isAppInBackground && SessionManager.activeRecording == null) {
                stopSelf()
                exitProcess(0)
            }
        }
    }

    private fun cancelBackgroundTimeout() {
        backgroundTimeoutJob?.cancel()
        backgroundTimeoutJob = null
    }

    private fun checkAndNotify() {
        val isRecording = SessionManager.activeRecording != null
        
        // Update notification if app is in background OR if recording is active
        if (!isAppInBackground && !isRecording) return

        val currentTime = System.currentTimeMillis()
        val isTimeForUpdate = (currentTime - lastNotificationTime >= Option.UI.notificationIntervalMs)
        val hasRecordingStateChanged = (isRecording != lastIsRecording)

        if (!isTimeForUpdate && !hasRecordingStateChanged) return

        val result = processor.getResult(unitSystem)
        val speedStr = "%.1f %s".format(result.getSog(), result.getSpeedUnit())
        val headingStr = "%.0f°".format(result.getCog())
        
        val windStr = lastWeatherData?.currentWeather?.let {
            val convertedWind = Converter.convertSpeed(it.windSpeed.toFloat() / 3.6f, unitSystem)
            "%.1f %s".format(convertedWind, result.getSpeedUnit())
        } ?: "--"

        val currentContent = "$speedStr|$headingStr|$windStr"
        
        if (currentContent != lastNotificationContent || hasRecordingStateChanged) {
            NotificationHelper.updateNotification(this, speedStr, headingStr, windStr, isRecording)
            lastNotificationContent = currentContent
            lastNotificationTime = currentTime
            lastIsRecording = isRecording
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Called when the app is swiped away from recents
        if (SessionManager.activeRecording != null) {
            SessionManager.stopRecording(this)
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        // Save recording if active when service is destroyed
        if (SessionManager.activeRecording != null) {
            SessionManager.stopRecording(this)
        }
        
        super.onDestroy()
        updateJob?.cancel()
        backgroundTimeoutJob?.cancel()
        captorListener.stop()
        serviceScope.cancel()
    }
}
