package com.example.countdown.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.countdown.data.model.TimerState
import com.example.countdown.data.model.TimerStatus
import com.example.countdown.utils.AngleCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 计时器数据仓库
 * 负责管理计时器状态的持久化和内存状态
 */
class TimerRepository(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "timer_prefs"
        private const val KEY_LAST_TOTAL_TIME = "last_total_time"
        private const val KEY_LAST_ANGLE = "last_angle"
        private const val KEY_TIMER_SETTINGS = "timer_settings"
        
        // 默认设置
        private const val DEFAULT_TIME_MS = 5 * 60 * 1000L // 5分钟
        private const val DEFAULT_ANGLE = 225f + (5f / 45f) * 270f // 对应5分钟的角度
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // 内存中的计时器状态
    private val _timerState = MutableStateFlow(createInitialState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
    /**
     * 创建初始状态
     */
    private fun createInitialState(): TimerState {
        val savedTotalTime = sharedPreferences.getLong(KEY_LAST_TOTAL_TIME, DEFAULT_TIME_MS)
        val savedAngle = sharedPreferences.getFloat(KEY_LAST_ANGLE, DEFAULT_ANGLE)
        
        return TimerState(
            status = TimerStatus.IDLE,
            totalTimeMs = savedTotalTime,
            remainingTimeMs = savedTotalTime,
            progress = 0f,
            angle = savedAngle,
            isScreenOn = false,
            lastUpdateTime = System.currentTimeMillis()
        )
    }
    
    /**
     * 更新计时器状态
     */
    fun updateTimerState(newState: TimerState) {
        _timerState.value = newState.copy(lastUpdateTime = System.currentTimeMillis())
        
        // 保存重要状态到持久化存储
        if (newState.status == TimerStatus.IDLE) {
            saveTimerSettings(newState.totalTimeMs, newState.angle)
        }
    }
    
    /**
     * 设置计时时间
     */
    fun setTimerTime(timeMs: Long) {
        val currentState = _timerState.value
        if (currentState.status == TimerStatus.IDLE) {
            val angle = AngleCalculator.timeToAngle(timeMs)
            val newState = currentState.copy(
                totalTimeMs = timeMs,
                remainingTimeMs = timeMs,
                angle = angle,
                progress = 0f
            )
            updateTimerState(newState)
        }
    }
    
    /**
     * 设置拖拽角度
     */
    fun setDragAngle(angle: Float) {
        val currentState = _timerState.value
        if (currentState.status == TimerStatus.IDLE) {
            val timeMs = AngleCalculator.angleToTime(angle)
            val newState = currentState.copy(
                totalTimeMs = timeMs,
                remainingTimeMs = timeMs,
                angle = angle,
                progress = 0f
            )
            updateTimerState(newState)
        }
    }
    
    /**
     * 开始计时
     */
    fun startTimer() {
        val currentState = _timerState.value
        if (currentState.canStart && currentState.totalTimeMs > 0) {
            val newState = currentState.copy(
                status = TimerStatus.RUNNING,
                isScreenOn = true
            )
            updateTimerState(newState)
        }
    }
    
    /**
     * 暂停计时
     */
    fun pauseTimer() {
        val currentState = _timerState.value
        if (currentState.canPause) {
            val newState = currentState.copy(
                status = TimerStatus.PAUSED,
                isScreenOn = false
            )
            updateTimerState(newState)
        }
    }
    
    /**
     * 停止计时
     */
    fun stopTimer() {
        val currentState = _timerState.value
        if (currentState.canStop) {
            val newState = currentState.copy(
                status = TimerStatus.IDLE,
                remainingTimeMs = currentState.totalTimeMs,
                progress = 0f,
                angle = AngleCalculator.timeToAngle(currentState.totalTimeMs),
                isScreenOn = false
            )
            updateTimerState(newState)
        }
    }
    
    /**
     * 重置计时器
     */
    fun resetTimer() {
        val currentState = _timerState.value
        val newState = currentState.copy(
            status = TimerStatus.IDLE,
            remainingTimeMs = currentState.totalTimeMs,
            progress = 0f,
            angle = AngleCalculator.timeToAngle(currentState.totalTimeMs),
            isScreenOn = false
        )
        updateTimerState(newState)
    }
    
    /**
     * 更新计时进度（由前台服务调用）
     */
    fun updateProgress(remainingTimeMs: Long) {
        val currentState = _timerState.value
        if (currentState.status == TimerStatus.RUNNING) {
            val progress = AngleCalculator.calculateProgress(
                currentState.totalTimeMs - remainingTimeMs,
                currentState.totalTimeMs
            )
            
            val newAngle = AngleCalculator.progressToAngle(progress)
            
            val newState = currentState.copy(
                remainingTimeMs = remainingTimeMs.coerceAtLeast(0L),
                progress = progress,
                angle = newAngle
            )
            
            // 检查是否计时完成
            if (remainingTimeMs <= 0) {
                finishTimer()
            } else {
                updateTimerState(newState)
            }
        }
    }
    
    /**
     * 完成计时
     */
    private fun finishTimer() {
        val currentState = _timerState.value
        val newState = currentState.copy(
            status = TimerStatus.FINISHED,
            remainingTimeMs = 0L,
            progress = 1f,
            angle = AngleCalculator.progressToAngle(1f),
            isScreenOn = true // 完成时保持屏幕亮起，显示完成状态
        )
        updateTimerState(newState)
    }
    
    /**
     * 获取当前状态
     */
    fun getCurrentState(): TimerState = _timerState.value
    
    /**
     * 保存计时器设置
     */
    private fun saveTimerSettings(totalTimeMs: Long, angle: Float) {
        sharedPreferences.edit()
            .putLong(KEY_LAST_TOTAL_TIME, totalTimeMs)
            .putFloat(KEY_LAST_ANGLE, angle)
            .apply()
    }
    
    /**
     * 获取保存的计时器设置
     */
    fun getSavedTimerSettings(): Pair<Long, Float> {
        val totalTime = sharedPreferences.getLong(KEY_LAST_TOTAL_TIME, DEFAULT_TIME_MS)
        val angle = sharedPreferences.getFloat(KEY_LAST_ANGLE, DEFAULT_ANGLE)
        return Pair(totalTime, angle)
    }
    
    /**
     * 清除所有保存的数据
     */
    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
        _timerState.value = createInitialState()
    }
    
    /**
     * 检查计时器是否正在运行
     */
    fun isTimerRunning(): Boolean {
        return _timerState.value.status == TimerStatus.RUNNING
    }
    
    /**
     * 获取剩余时间（毫秒）
     */
    fun getRemainingTimeMs(): Long {
        return _timerState.value.remainingTimeMs
    }
    
    /**
     * 获取总时间（毫秒）
     */
    fun getTotalTimeMs(): Long {
        return _timerState.value.totalTimeMs
    }
    
    /**
     * 设置屏幕常亮状态
     */
    fun setScreenOn(isOn: Boolean) {
        val currentState = _timerState.value
        if (currentState.isScreenOn != isOn) {
            val newState = currentState.copy(isScreenOn = isOn)
            updateTimerState(newState)
        }
    }
}