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
import com.example.countdown.data.model.TimerStatus
import com.example.countdown.utils.TimerNotificationManager

class TimerForegroundService : Service() {

    private val binder = TimerBinder()
    private var countDownTimer: CountDownTimer? = null
    private var serviceTotalTimeMs: Long = 0L
    private var serviceRemainingTimeMs: Long = 0L
    private var isTimerCurrentlyRunning = false // Renamed for clarity
    private var lastKnownRemainingTimeMs: Long = 0L
    private var status: TimerStatus = TimerStatus.IDLE
    private var lastNotifiedSecond: Long = -1L
    private var lastUiUpdatedSecond: Long = -1L

    private var onTimerUpdateListener: ((Long) -> Unit)? = null
    private var onTimerFinishedListener: (() -> Unit)? = null

    inner class TimerBinder : Binder() {
        fun getService(): TimerForegroundService = this@TimerForegroundService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        TimerNotificationManager.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val totalTimeFromIntent = intent?.getLongExtra(EXTRA_TOTAL_TIME, 0L) ?: 0L
        val remainingTimeFromIntent = intent?.getLongExtra(EXTRA_REMAINING_TIME, totalTimeFromIntent) ?: totalTimeFromIntent

        if (serviceTotalTimeMs == 0L && totalTimeFromIntent > 0L) {
            serviceTotalTimeMs = totalTimeFromIntent
            serviceRemainingTimeMs = remainingTimeFromIntent
        } else if (totalTimeFromIntent == 0L && serviceTotalTimeMs == 0L) {
            serviceRemainingTimeMs = lastKnownRemainingTimeMs
        }

        if (serviceRemainingTimeMs > 0) {
            startActualTimer(serviceRemainingTimeMs)
        } else {
            onTimerFinishedListener?.invoke()
            stopServiceAndForeground()
        }
        
        return START_STICKY
    }

    private fun startActualTimer(timeToCountDownMs: Long) {
        val notification = TimerNotificationManager.createNotification(
            this,
            serviceTotalTimeMs,
            timeToCountDownMs,
            status // Initial status before running might be IDLE or PAUSED if resuming
        )
        startForeground(NOTIFICATION_ID, notification)
        status = TimerStatus.RUNNING
        isTimerCurrentlyRunning = true
        lastNotifiedSecond = -1L // Reset for a new timer instance
        lastUiUpdatedSecond = -1L // Reset for a new timer instance

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(timeToCountDownMs, 100) {
            override fun onTick(millisUntilFinished: Long) {
                serviceRemainingTimeMs = millisUntilFinished
                lastKnownRemainingTimeMs = millisUntilFinished
            
                val currentSecond = millisUntilFinished / 1000
                if (currentSecond != lastNotifiedSecond) { 
                    // 仅当状态改变或每秒更新时发送通知
                    // 注意：高频更新通知可能会被系统限制或导致卡顿
                    TimerNotificationManager.updateNotification(
                        this@TimerForegroundService,
                        serviceTotalTimeMs,
                        millisUntilFinished,
                        TimerStatus.RUNNING // Explicitly pass RUNNING status
                    )
                    lastNotifiedSecond = currentSecond
                }

                // Control UI update frequency
                if (currentSecond != lastUiUpdatedSecond) {
                    onTimerUpdateListener?.invoke(millisUntilFinished)
                    lastUiUpdatedSecond = currentSecond
                }
            }

            override fun onFinish() {
                serviceRemainingTimeMs = 0
                lastKnownRemainingTimeMs = 0
                status = TimerStatus.FINISHED
                isTimerCurrentlyRunning = false
                TimerNotificationManager.updateNotification( // Final update
                    this@TimerForegroundService,
                    serviceTotalTimeMs,
                    0,
                    status
                )
                onTimerUpdateListener?.invoke(0) // Ensure final UI update
                lastUiUpdatedSecond = -1L // Reset for next timer run

                triggerAlarm()
                stopServiceAndForeground() // This will set status to IDLE
                lastNotifiedSecond = -1L // Reset for next timer run
            }
        }.start()
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        status = TimerStatus.PAUSED
        isTimerCurrentlyRunning = false
        TimerNotificationManager.updateNotification(this, serviceTotalTimeMs, serviceRemainingTimeMs, status)
    }

    fun userInitiatedStop() {
        countDownTimer?.cancel()
        serviceRemainingTimeMs = 0
        status = TimerStatus.IDLE
        isTimerCurrentlyRunning = false
        lastKnownRemainingTimeMs = 0
        TimerNotificationManager.updateNotification(this, serviceTotalTimeMs, serviceRemainingTimeMs, status)
        stopServiceAndForeground()
    }
    
    private fun stopServiceAndForeground() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        serviceTotalTimeMs = 0L
        serviceRemainingTimeMs = 0L
        lastKnownRemainingTimeMs = 0L
        countDownTimer = null
        status = TimerStatus.IDLE
        isTimerCurrentlyRunning = false
    }

    fun getRemainingTime(): Long = serviceRemainingTimeMs
    fun isCurrentlyRunning(): Boolean = isTimerCurrentlyRunning

    fun setOnTimerUpdateListener(listener: (Long) -> Unit) {
        onTimerUpdateListener = listener
    }

    fun setOnTimerFinishedListener(listener: () -> Unit) {
        onTimerFinishedListener = listener
    }

    private fun triggerAlarm() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(1000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    companion object {
        private const val TAG = "TimerForegroundSrv" // Shorter TAG for logs
        const val EXTRA_TOTAL_TIME = "extra_total_time"
        const val EXTRA_REMAINING_TIME = "extra_remaining_time"
        // CHANNEL_ID and NOTIFICATION_ID are now primarily managed by TimerNotificationManager
        // Ensure these values are consistent if accessed directly here for some reason,
        // though ideally all notification logic uses TimerNotificationManager.
        private const val CHANNEL_ID = "TimerServiceChannel_01" 
        private const val NOTIFICATION_ID = 1
    }
}