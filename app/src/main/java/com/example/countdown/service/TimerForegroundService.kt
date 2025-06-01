package com.example.countdown.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.countdown.MainActivity
import com.example.countdown.R // 确保 R 文件导入正确
import com.example.countdown.utils.TimeFormatter

class TimerForegroundService : Service() {

    private val binder = TimerBinder()
    private var countDownTimer: CountDownTimer? = null
    private var serviceTotalTimeMs: Long = 0L
    private var serviceRemainingTimeMs: Long = 0L
    private var isTimerCurrentlyRunning = false // Renamed for clarity

    private var onTimerUpdateListener: ((Long) -> Unit)? = null
    private var onTimerFinishedListener: (() -> Unit)? = null

    inner class TimerBinder : Binder() {
        fun getService(): TimerForegroundService = this@TimerForegroundService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "Service onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand received")

        val totalTimeFromIntent = intent?.getLongExtra(EXTRA_TOTAL_TIME, 0L) ?: 0L
        val remainingTimeFromIntent = intent?.getLongExtra(EXTRA_REMAINING_TIME, totalTimeFromIntent) ?: totalTimeFromIntent

        Log.d(TAG, "Intent values - Total: $totalTimeFromIntent, Remaining: $remainingTimeFromIntent")

        serviceTotalTimeMs = totalTimeFromIntent
        serviceRemainingTimeMs = if (remainingTimeFromIntent > 0 && remainingTimeFromIntent <= serviceTotalTimeMs) {
            remainingTimeFromIntent
        } else if (serviceTotalTimeMs > 0) {
            serviceTotalTimeMs
        } else {
            0L
        }
        Log.d(TAG, "Effective times - Total: $serviceTotalTimeMs, Remaining for timer: $serviceRemainingTimeMs")

        countDownTimer?.cancel() // Stop any existing timer
        isTimerCurrentlyRunning = false

        if (serviceRemainingTimeMs > 0) {
            // Notification will be created and foregrounded in startActualTimer
            startActualTimer(serviceRemainingTimeMs)
        } else {
            Log.d(TAG, "No time to countdown ($serviceRemainingTimeMs ms), processing finish.")
            onTimerFinishedListener?.invoke()
            updateNotificationOnStateChange(0L, false) // Update to "Finished"
            stopServiceAndForeground() 
        }
        
        return START_STICKY
    }

    private fun startActualTimer(timeToCountDownMs: Long) {
        Log.d(TAG, "startActualTimer for: $timeToCountDownMs ms")
        serviceRemainingTimeMs = timeToCountDownMs 
        isTimerCurrentlyRunning = true // Set running before starting timer and notification
        
        // Start foreground with an initial/running notification
        startForeground(NOTIFICATION_ID, getCurrentNotificationBuilder(serviceRemainingTimeMs, true).build())

        countDownTimer = object : CountDownTimer(timeToCountDownMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isTimerCurrentlyRunning) { // Handle cases where timer might be cancelled externally but tick still fires
                    this.cancel()
                    return
                }
                serviceRemainingTimeMs = millisUntilFinished
                onTimerUpdateListener?.invoke(millisUntilFinished)
                updateNotificationOnStateChange(millisUntilFinished, true)
            }

            override fun onFinish() {
                Log.d(TAG, "CountDownTimer onFinish")
                serviceRemainingTimeMs = 0L
                isTimerCurrentlyRunning = false
                onTimerUpdateListener?.invoke(0L)
                onTimerFinishedListener?.invoke()
                updateNotificationOnStateChange(0L, false) // Update to "Finished"
                triggerAlarm() // Trigger alarm on finish
                stopServiceAndForeground()
            }
        }.start()
    }

    fun pauseTimer() {
        Log.d(TAG, "pauseTimer called. Current remaining: $serviceRemainingTimeMs")
        countDownTimer?.cancel()
        isTimerCurrentlyRunning = false
        updateNotificationOnStateChange(serviceRemainingTimeMs, false) 
    }

    fun userInitiatedStop() { 
        Log.d(TAG, "userInitiatedStop called")
        countDownTimer?.cancel()
        isTimerCurrentlyRunning = false
        serviceRemainingTimeMs = 0 
        // serviceTotalTimeMs = 0; // Let ViewModel/Repository decide if totalTime resets
        onTimerFinishedListener?.invoke() 
        updateNotificationOnStateChange(0L, false)
        stopServiceAndForeground()
    }
    
    private fun stopServiceAndForeground() {
        Log.d(TAG, "stopServiceAndForeground called")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun getRemainingTime(): Long = serviceRemainingTimeMs
    fun isCurrentlyRunning(): Boolean = isTimerCurrentlyRunning

    fun setOnTimerUpdateListener(listener: (Long) -> Unit) {
        onTimerUpdateListener = listener
    }

    fun setOnTimerFinishedListener(listener: () -> Unit) {
        onTimerFinishedListener = listener
    }
    
    private fun createNotificationChannel() {
        val name = "Timer Service Channel"
        val descriptionText = "Channel for Timer Foreground Service"
        val importance = NotificationManager.IMPORTANCE_LOW 
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun getCurrentNotificationBuilder(timeRemainingMsParam: Long, isRunningState: Boolean): NotificationCompat.Builder {
        val effectiveRemainingTime = if (timeRemainingMsParam < 0) 0 else timeRemainingMsParam

        val contentText = when {
            isRunningState && effectiveRemainingTime > 0 -> "剩余时间: ${TimeFormatter.formatTime(effectiveRemainingTime)}"
            !isRunningState && effectiveRemainingTime > 0 -> "已暂停: ${TimeFormatter.formatTime(effectiveRemainingTime)}"
            else -> "计时完成"
        }
        
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingIntentFlags)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("倒计时器")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Standard icon
            .setOngoing(isRunningState) 
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
    }
    
    private fun updateNotificationOnStateChange(timeRemainingMsParam: Long, running: Boolean) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, getCurrentNotificationBuilder(timeRemainingMsParam, running).build())
    }

    private fun triggerAlarm() {
        Log.d(TAG, "Triggering alarm")
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        countDownTimer?.cancel()
        isTimerCurrentlyRunning = false
        super.onDestroy()
    }

    companion object {
        private const val TAG = "TimerForegroundSrv" // Shorter TAG for logs
        const val EXTRA_TOTAL_TIME = "extra_total_time"
        const val EXTRA_REMAINING_TIME = "extra_remaining_time"
        private const val CHANNEL_ID = "TimerServiceChannel_01" 
        private const val NOTIFICATION_ID = 1
    }
}