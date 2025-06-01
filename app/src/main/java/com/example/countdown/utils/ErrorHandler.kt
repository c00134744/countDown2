package com.example.countdown.utils

import android.util.Log

/**
 * 错误处理工具类
 * 统一处理应用中的错误和边界情况
 */
object ErrorHandler {
    
    internal const val TAG = "CountDownApp"
    
    /**
     * 处理时间设置错误
     */
    fun handleTimeSetError(timeMs: Long): Long {
        return when {
            timeMs < 0 -> {
                Log.w(TAG, "时间不能为负数，已重置为0")
                0L
            }
            timeMs > AngleCalculator.MAX_TIME_MS -> {
                Log.w(TAG, "时间超过最大限制，已重置为45分钟")
                AngleCalculator.MAX_TIME_MS
            }
            else -> timeMs
        }
    }
    
    /**
     * 处理角度设置错误
     */
    fun handleAngleSetError(angle: Float): Float {
        return when {
            angle.isNaN() || angle.isInfinite() -> {
                Log.w(TAG, "角度值无效，已重置为起始角度")
                AngleCalculator.START_ANGLE
            }
            !AngleCalculator.isValidAngle(angle) -> {
                Log.w(TAG, "角度超出有效范围，已约束到有效范围")
                AngleCalculator.constrainAngle(angle)
            }
            else -> angle
        }
    }
    
    /**
     * 处理服务连接错误
     */
    fun handleServiceConnectionError(error: Exception) {
        Log.e(TAG, "服务连接失败", error)
        // 可以在这里添加重试逻辑或用户提示
    }
    
    /**
     * 处理权限错误
     */
    fun handlePermissionError(permission: String, error: Exception) {
        Log.e(TAG, "权限处理失败: $permission", error)
        // 可以在这里添加权限说明或降级处理
    }
    
    /**
     * 处理通知错误
     */
    fun handleNotificationError(error: Exception) {
        Log.e(TAG, "通知创建失败", error)
        // 可以在这里添加备用提醒方式
    }
    
    /**
     * 处理震动错误
     */
    fun handleVibrationError(error: Exception) {
        Log.e(TAG, "震动功能失败", error)
        // 可以在这里添加其他提醒方式
    }
    
    /**
     * 处理数据持久化错误
     */
    fun handleDataPersistenceError(error: Exception) {
        Log.e(TAG, "数据保存失败", error)
        // 可以在这里添加数据恢复逻辑
    }
    
    /**
     * 安全执行代码块
     */
    inline fun <T> safeExecute(
        operation: () -> T,
        onError: (Exception) -> T
    ): T {
        return try {
            operation()
        } catch (e: Exception) {
            Log.e("CountDownApp", "操作执行失败", e)
            onError(e)
        }
    }
    
    /**
     * 验证计时器状态
     */
    fun validateTimerState(state: com.example.countdown.data.model.TimerState): com.example.countdown.data.model.TimerState {
        return state.copy(
            totalTimeMs = handleTimeSetError(state.totalTimeMs),
            remainingTimeMs = handleTimeSetError(state.remainingTimeMs),
            angle = handleAngleSetError(state.angle),
            progress = state.progress.coerceIn(0f, 1f)
        )
    }
}