package com.example.countdown.utils

/**
 * 时间格式化工具类
 * 处理时间的显示格式化
 */
object TimeFormatter {
    
    /**
     * 将毫秒转换为分:秒格式
     * @param timeMs 时间（毫秒）
     * @return 格式化的时间字符串，如 "25:30"
     */
    fun formatTime(timeMs: Long): String {
        val totalSeconds = (timeMs / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    /**
     * 将毫秒转换为详细的时间格式
     * @param timeMs 时间（毫秒）
     * @return 格式化的时间字符串，如 "25分30秒"
     */
    fun formatTimeDetailed(timeMs: Long): String {
        val totalSeconds = (timeMs / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        
        return when {
            minutes > 0 && seconds > 0 -> "${minutes}分${seconds}秒"
            minutes > 0 -> "${minutes}分钟"
            seconds > 0 -> "${seconds}秒"
            else -> "0秒"
        }
    }
    
    /**
     * 将毫秒转换为简短格式
     * @param timeMs 时间（毫秒）
     * @return 格式化的时间字符串，如 "25m" 或 "30s"
     */
    fun formatTimeShort(timeMs: Long): String {
        val totalSeconds = (timeMs / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        
        return when {
            minutes > 0 -> "${minutes}m"
            seconds > 0 -> "${seconds}s"
            else -> "0s"
        }
    }
    
    /**
     * 将分钟转换为毫秒
     * @param minutes 分钟数
     * @return 毫秒数
     */
    fun minutesToMs(minutes: Float): Long {
        return (minutes * 60 * 1000).toLong()
    }
    
    /**
     * 将毫秒转换为分钟
     * @param timeMs 毫秒数
     * @return 分钟数
     */
    fun msToMinutes(timeMs: Long): Float {
        return timeMs / (60 * 1000f)
    }
    
    /**
     * 将秒转换为毫秒
     * @param seconds 秒数
     * @return 毫秒数
     */
    fun secondsToMs(seconds: Int): Long {
        return seconds * 1000L
    }
    
    /**
     * 将毫秒转换为秒
     * @param timeMs 毫秒数
     * @return 秒数
     */
    fun msToSeconds(timeMs: Long): Int {
        return (timeMs / 1000).toInt()
    }
    
    /**
     * 格式化进度百分比
     * @param progress 进度值（0.0 - 1.0）
     * @return 格式化的百分比字符串，如 "75%"
     */
    fun formatProgress(progress: Float): String {
        val percentage = (progress * 100).toInt()
        return "${percentage}%"
    }
    
    /**
     * 获取时间设置的建议值（分钟）
     * @return 建议的时间设置列表
     */
    fun getTimeSuggestions(): List<Int> {
        return listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 45)
    }
    
    /**
     * 将时间四舍五入到最近的分钟
     * @param timeMs 时间（毫秒）
     * @return 四舍五入后的时间（毫秒）
     */
    fun roundToNearestMinute(timeMs: Long): Long {
        val minutes = Math.round(timeMs / (60 * 1000f))
        return minutes * 60 * 1000L
    }
    
    /**
     * 检查时间是否有效（在0-45分钟范围内）
     * @param timeMs 时间（毫秒）
     * @return 是否有效
     */
    fun isValidTime(timeMs: Long): Boolean {
        return timeMs in 0..AngleCalculator.MAX_TIME_MS
    }
}