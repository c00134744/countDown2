package com.example.countdown.utils

import kotlin.math.*

/**
 * 角度计算工具类
 * 处理圆形计时器的角度与时间转换
 */
object AngleCalculator {
    
    // 圆形轨道的角度范围
    const val START_ANGLE = 135f        // 起点角度（左上角）
    const val END_ANGLE = 45f           // 终点角度（右上角）
    const val TOTAL_ANGLE = 270f        // 总角度范围
    
    // 最大计时时间（45分钟）
    const val MAX_TIME_MINUTES = 45f
    const val MAX_TIME_MS = (MAX_TIME_MINUTES * 60 * 1000).toLong()
    
    /**
     * 将触摸坐标转换为角度
     * @param touchX 触摸点X坐标
     * @param touchY 触摸点Y坐标
     * @param centerX 圆心X坐标
     * @param centerY 圆心Y坐标
     * @return 角度值（0-360度）
     */
    fun coordinateToAngle(touchX: Float, touchY: Float, centerX: Float, centerY: Float): Float {
        val deltaX = touchX - centerX
        val deltaY = touchY - centerY
        
        // 使用atan2计算角度，结果为弧度
        val radians = atan2(deltaY, deltaX)
        
        // 转换为角度并调整到0-360范围
        var degrees = Math.toDegrees(radians.toDouble()).toFloat()
        if (degrees < 0) {
            degrees += 360f
        }
        
        return degrees
    }
    
    /**
     * 将角度限制在有效范围内（135° - 45°，跨越360°）
     * @param angle 输入角度
     * @return 限制后的角度
     */
    fun constrainAngle(angle: Float): Float {
        return when {
            // 有效范围：135° - 360° 和 0° - 45°
            angle >= START_ANGLE || angle <= END_ANGLE -> {
                angle
            }
            // 角度在无效范围内（45° - 135°），选择最近的有效角度
            angle > END_ANGLE && angle < START_ANGLE -> {
                val distToStart = abs(angle - START_ANGLE)
                val distToEnd = abs(angle - END_ANGLE)
                if (distToStart < distToEnd) START_ANGLE else END_ANGLE
            }
            else -> angle
        }
    }
    
    /**
     * 将角度转换为时间（毫秒）
     * @param angle 角度值（135° - 45°，跨越360°）
     * @return 对应的时间（毫秒）
     */
    fun angleToTime(angle: Float): Long {
        val constrainedAngle = constrainAngle(angle)
        
        // 计算相对于起点的角度进度
        val relativeAngle = if (constrainedAngle >= START_ANGLE) {
            constrainedAngle - START_ANGLE
        } else {
            // 处理跨越360度的情况：0° - 45° 对应 225° - 270°
            (360f - START_ANGLE) + constrainedAngle
        }
        
        // 将角度进度转换为时间比例
        val timeRatio = relativeAngle / TOTAL_ANGLE
        
        return (timeRatio * MAX_TIME_MS).toLong()
    }
    
    /**
     * 将时间转换为角度
     * @param timeMs 时间（毫秒）
     * @return 对应的角度值
     */
    fun timeToAngle(timeMs: Long): Float {
        val clampedTime = timeMs.coerceIn(0L, MAX_TIME_MS)
        val timeRatio = clampedTime.toFloat() / MAX_TIME_MS
        
        val relativeAngle = timeRatio * TOTAL_ANGLE
        val angle = START_ANGLE + relativeAngle
        
        // 处理超过360度的情况
        return if (angle > 360f) angle - 360f else angle
    }
    
    /**
     * 计算进度百分比
     * @param elapsedTimeMs 已经过时间
     * @param totalTimeMs 总时间
     * @return 进度百分比（0.0 - 1.0）
     */
    fun calculateProgress(elapsedTimeMs: Long, totalTimeMs: Long): Float {
        if (totalTimeMs <= 0) return 0f
        return (elapsedTimeMs.toFloat() / totalTimeMs).coerceIn(0f, 1f)
    }
    
    /**
     * 根据进度计算当前角度位置
     * @param progress 进度百分比（0.0 - 1.0）
     * @return 当前角度位置
     */
    fun progressToAngle(progress: Float): Float {
        val clampedProgress = progress.coerceIn(0f, 1f)
        val relativeAngle = clampedProgress * TOTAL_ANGLE
        val angle = START_ANGLE + relativeAngle
        
        return if (angle > 360f) angle - 360f else angle
    }
    
    /**
     * 检查角度是否在有效范围内
     * @param angle 角度值
     * @return 是否有效
     */
    fun isValidAngle(angle: Float): Boolean {
        return angle >= START_ANGLE || angle <= END_ANGLE
    }
}