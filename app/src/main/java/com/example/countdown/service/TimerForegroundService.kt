package com.example.countdown.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.countdown.MainActivity
import com.example.countdown.R
import com.example.countdown.utils.TimeFormatter
import java.util.*
import kotlin.math.max

/**
 * 计时器前台服务
 * 负责后台计时、通知显示和系统集成
 */
class TimerForegroundService : Service() {
    
    companion object {
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 1
        const val EXTRA_TOTAL_TIME = "extra_total_time"
        const val EXTRA_REMAINING_TIME = "extra_remaining_time"
        
        private const val TIMER_INTERVAL = 100L // 100ms更新间隔
    }
    
    private val binder = TimerBinder()
    private var timer: Timer? = null
    private var startTime: Long = 0
    private var totalTimeMs: Long = 0
    private var remainingTimeMs: Long = 0
    private var isPaused = false
    private var pausedTime: Long = 0
    
    // 回调接口
    private var onTimerUpdateListener: ((Long) -> Unit)? = null
    private var onTimerFinishedListener: (() -> Unit)? = null
    
    inner class TimerBinder : Binder() {
        fun getService(): TimerForegroundService = this@TimerForegroundService
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            totalTimeMs = it.getLongExtra(EXTRA_TOTAL_TIME, 0L)
            remainingTimeMs = it.getLongExtra(EXTRA_REMAINING_TIME, totalTimeMs)
            
            if (totalTimeMs > 0) {
                startTimer()
            }
        }
        
        return START_STICKY // 服务被杀死后自动重启
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "计时器通知",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示计时器进度"
                setSound(null, null) // 禁用声音
                enableVibration(false) // 禁用震动
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 开始计时
     */
    private fun startTimer() {
        if (timer != null) {
            timer?.cancel()
        }
        
        startTime = System.currentTimeMillis()
        isPaused = false
        
        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification())
        
        // 创建定时器
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    updateTimer()
                }
            }, 0, TIMER_INTERVAL)
        }
    }
    
    /**
     * 暂停计时
     */
    fun pauseTimer() {
        isPaused = true
        pausedTime = System.currentTimeMillis()
        timer?.cancel()
        timer = null
        
        // 更新通知显示暂停状态
        updateNotification()
    }
    
    /**
     * 继续计时
     */
    fun resumeTimer() {
        if (isPaused) {
            val pauseDuration = System.currentTimeMillis() - pausedTime
            startTime += pauseDuration
            isPaused = false
            
            timer = Timer().apply {
                scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        updateTimer()
                    }
                }, 0, TIMER_INTERVAL)
            }
        }
    }
    
    /**
     * 停止计时
     */
    fun stopTimer() {
        timer?.cancel()
        timer = null
        stopForeground(true)
        stopSelf()
    }
    
    /**
     * 更新计时器
     */
    private fun updateTimer() {
        if (isPaused) return
        
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - startTime
        remainingTimeMs = max(0L, totalTimeMs - elapsedTime)
        
        // 通知UI更新
        onTimerUpdateListener?.invoke(remainingTimeMs)
        
        // 更新通知
        updateNotification()
        
        // 检查是否完成
        if (remainingTimeMs <= 0) {
            onTimerFinished()
        }
    }
    
    /**
     * 计时完成处理
     */
    private fun onTimerFinished() {
        timer?.cancel()
        timer = null
        
        // 触发完成回调
        onTimerFinishedListener?.invoke()
        
        // 显示完成通知
        showFinishedNotification()
        
        // 触发震动和声音
        triggerAlarm()
        
        // 延迟停止服务
        Handler(Looper.getMainLooper()).postDelayed({
            stopForeground(true)
            stopSelf()
        }, 5000) // 5秒后停止
    }
    
    /**
     * 创建通知
     */
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val timeText = TimeFormatter.formatTime(remainingTimeMs)
        val statusText = if (isPaused) "已暂停" else "计时中"
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("倒计时 - $statusText")
            .setContentText("剩余时间: $timeText")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setProgress(
                totalTimeMs.toInt(),
                (totalTimeMs - remainingTimeMs).toInt(),
                false
            )
            .build()
    }
    
    /**
     * 更新通知
     */
    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }
    
    /**
     * 显示完成通知
     */
    private fun showFinishedNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("计时完成！")
            .setContentText("倒计时已结束")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
    
    /**
     * 触发提醒（震动和声音）
     */
    private fun triggerAlarm() {
        // 震动
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
        
        // 这里可以添加声音播放逻辑
        // 由于需要处理音频权限和资源，暂时只实现震动
    }
    
    /**
     * 设置计时更新监听器
     */
    fun setOnTimerUpdateListener(listener: (Long) -> Unit) {
        onTimerUpdateListener = listener
    }
    
    /**
     * 设置计时完成监听器
     */
    fun setOnTimerFinishedListener(listener: () -> Unit) {
        onTimerFinishedListener = listener
    }
    
    /**
     * 获取剩余时间
     */
    fun getRemainingTime(): Long = remainingTimeMs
    
    /**
     * 获取总时间
     */
    fun getTotalTime(): Long = totalTimeMs
    
    /**
     * 检查是否暂停
     */
    fun isPaused(): Boolean = isPaused
    
    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        timer = null
    }
}