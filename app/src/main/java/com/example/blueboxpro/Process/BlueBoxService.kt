package com.example.blueboxpro.Process

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.blueboxpro.Option
import com.example.blueboxpro.Save.SessionManager
import kotlinx.coroutines.*

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

    // For notification updates
    var lastWeatherData: WeatherData? = null
    var unitSystem: String = Option.UI.unitSystem
    
    var isAppInBackground: Boolean = false
    private var lastNotificationTime: Long = 0
    private var lastNotificationContent: String = ""

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

    private fun checkAndNotify() {
        if (!isAppInBackground) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNotificationTime < Option.UI.notificationIntervalMs) return

        val result = processor.getResult(unitSystem)
        val speedStr = "%.1f %s".format(result.getSog(), result.getSpeedUnit())
        val headingStr = "%.0f°".format(result.getCog())
        
        val windStr = lastWeatherData?.currentWeather?.let {
            val convertedWind = Converter.convertSpeed(it.windSpeed.toFloat() / 3.6f, unitSystem)
            "%.1f %s".format(convertedWind, result.getSpeedUnit())
        } ?: "--"

        val currentContent = "$speedStr|$headingStr|$windStr"
        
        if (currentContent != lastNotificationContent) {
            NotificationHelper.updateNotification(this, speedStr, headingStr, windStr)
            lastNotificationContent = currentContent
            lastNotificationTime = currentTime
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
        captorListener.stop()
        serviceScope.cancel()
    }
}
