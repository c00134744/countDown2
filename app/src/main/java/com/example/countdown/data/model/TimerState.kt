package com.example.countdown.data.model

/**
 * 计时器状态枚举
 */
enum class TimerStatus {
    IDLE,       // 空闲状态，等待设置时间
    RUNNING,    // 正在计时
    PAUSED,     // 暂停状态
    FINISHED    // 计时完成
}

/**
 * 计时器状态数据类
 * 包含计时器的所有状态信息
 */
data class TimerState(
    val status: TimerStatus = TimerStatus.IDLE,
    val totalTimeMs: Long = 0L,                    // 总计时时间（毫秒）
    val remainingTimeMs: Long = 0L,                // 剩余时间（毫秒）
    val progress: Float = 0f,                      // 进度百分比 (0.0 - 1.0)
    val angle: Float = 225f,                       // 当前拖拽点角度 (225° - 315°)
    val isScreenOn: Boolean = false,               // 屏幕是否保持常亮
    val lastUpdateTime: Long = 0L                  // 最后更新时间戳
) {
    /**
     * 获取已经过的时间（毫秒）
     */
    val elapsedTimeMs: Long
        get() = totalTimeMs - remainingTimeMs
    
    /**
     * 获取总时间（分钟）
     */
    val totalTimeMinutes: Float
        get() = totalTimeMs / 60000f
    
    /**
     * 获取剩余时间（分钟）
     */
    val remainingTimeMinutes: Float
        get() = remainingTimeMs / 60000f
    
    /**
     * 检查是否正在计时
     */
    val isActive: Boolean
        get() = status == TimerStatus.RUNNING
    
    /**
     * 检查是否可以开始计时
     */
    val canStart: Boolean
        get() = status == TimerStatus.IDLE || status == TimerStatus.PAUSED
    
    /**
     * 检查是否可以暂停
     */
    val canPause: Boolean
        get() = status == TimerStatus.RUNNING
    
    /**
     * 检查是否可以停止
     */
    val canStop: Boolean
        get() = status == TimerStatus.RUNNING || status == TimerStatus.PAUSED
}

/**
 * 计时器操作枚举
 */
enum class TimerAction {
    START,      // 开始计时
    PAUSE,      // 暂停计时
    STOP,       // 停止计时
    RESET,      // 重置计时器
    SET_TIME,   // 设置时间
    TICK        // 计时器滴答（每秒更新）
}