package com.example.countdown.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.countdown.MainActivity
import com.example.countdown.R
import com.example.countdown.data.model.TimerStatus

object TimerNotificationManager {

    // These IDs should be consistent with any other parts of the app that might reference them,
    // though ideally, they are solely used by this manager.
    private const val CHANNEL_ID = "TimerServiceChannel_01"
    private const val NOTIFICATION_ID = 1

    fun createNotificationChannel(context: Context) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Timer Service Channel"
            val descriptionText = "Channel for Timer Foreground Service"
            // Importance changed to IMPORTANCE_DEFAULT from LOW to make sure notification is visible
            // Users can then manage notification settings if they prefer less intrusion.
            val importance = NotificationManager.IMPORTANCE_DEFAULT 
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                // Sound and vibration are typically managed by the notification itself when it's posted,
                // or by the service for specific events (like timer finish). 
                // Setting to null here means it uses system defaults or what's set on the notification.
                setSound(null, null)
                enableVibration(false) // Vibration for finish is handled by service explicitly
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(
        context: Context,
        totalTimeMs: Long,
        remainingTimeMsParam: Long,
        currentStatus: TimerStatus
    ): Notification {
        val effectiveRemainingTime = if (remainingTimeMsParam < 0) 0 else remainingTimeMsParam

        val contentText = when (currentStatus) {
            TimerStatus.RUNNING -> "剩余时间: ${TimeFormatter.formatTime(effectiveRemainingTime)}"
            TimerStatus.PAUSED -> "已暂停: ${TimeFormatter.formatTime(effectiveRemainingTime)}"
            TimerStatus.IDLE -> if (totalTimeMs > 0) "准备就绪: ${TimeFormatter.formatTime(totalTimeMs)}" else "设置时间"
            TimerStatus.FINISHED -> "计时完成"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            // Flags to bring an existing task to the foreground without clearing it
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("倒计时器")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Using a dedicated timer icon if available
            .setOngoing(currentStatus == TimerStatus.RUNNING || currentStatus == TimerStatus.PAUSED) // Notification is ongoing if running or paused
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true) // Prevents re-alerting on update
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setSilent(true) // Updates should be silent, specific alerts (like finish) handled by service
            .build()
    }

    fun updateNotification(
        context: Context,
        totalTimeMs: Long,
        remainingTimeMs: Long,
        status: TimerStatus
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Ensure the channel is created, especially for API < O where it might not be called in onCreate of service every time.
        // Though createNotificationChannel itself has the API check, this is a safe call.
        createNotificationChannel(context) 
        val notification = createNotification(context, totalTimeMs, remainingTimeMs, status)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
} 