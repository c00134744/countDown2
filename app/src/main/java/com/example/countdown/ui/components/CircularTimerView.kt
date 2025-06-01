package com.example.countdown.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.countdown.data.model.TimerState
import com.example.countdown.ui.theme.*
import com.example.countdown.utils.AngleCalculator
import kotlin.math.*

/**
 * 圆形计时器视图组件
 * 实现圆形轨道绘制、拖拽交互和进度显示
 */
@Composable
fun CircularTimerView(
    timerState: TimerState,
    onTimeSet: (Long) -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 300.dp
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragAngle by remember { mutableStateOf(timerState.angle) }
    
    // 同步外部状态变化
    LaunchedEffect(timerState.angle) {
        if (!isDragging) {
            dragAngle = timerState.angle
        }
    }
    
    // 拖拽手势现在总是启用，因为按钮已分离
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .pointerInput(Unit) { // 直接启用手势检测
                    detectDragGestures(
                        onDragStart = { offset ->
                            val center = Offset(this.size.width / 2f, this.size.height / 2f)
                            // 移除了按钮区域检测，因为按钮已分离
                            isDragging = true
                            val angle = AngleCalculator.coordinateToAngle(
                                offset.x, offset.y, center.x, center.y
                            )
                            dragAngle = AngleCalculator.constrainAngle(angle)
                        },
                        onDragEnd = {
                            if (isDragging) {
                                isDragging = false
                                val timeMs = AngleCalculator.angleToTime(dragAngle)
                                onTimeSet(timeMs)
                            }
                        },
                        onDrag = { change, _ ->
                            if (!isDragging) return@detectDragGestures
                            
                            val angle = AngleCalculator.coordinateToAngle(
                                change.position.x, change.position.y, 
                                this.size.width / 2f, this.size.height / 2f
                            )
                            dragAngle = AngleCalculator.constrainAngle(angle)
                            
                            // 实时更新时间
                            val timeMs = AngleCalculator.angleToTime(dragAngle)
                            onTimeSet(timeMs)
                        }
                    )
                }
        ) {
            drawCircularTimer(
                timerState = timerState,
                currentAngle = if (isDragging) dragAngle else timerState.angle,
                isDragging = isDragging
            )
        }
    }
}

/**
 * 绘制圆形计时器
 */
private fun DrawScope.drawCircularTimer(
    timerState: TimerState,
    currentAngle: Float,
    isDragging: Boolean
) {
    val center = size.center
    val radius = size.minDimension / 2f - 40.dp.toPx()
    val strokeWidth = 12.dp.toPx()
    
    // 绘制背景轨道
    drawBackgroundTrack(center, radius, strokeWidth)
    
    // 绘制进度轨道
    drawProgressTrack(center, radius, strokeWidth, timerState, currentAngle)
    
    // 绘制拖拽点
    drawDragHandle(center, radius, currentAngle, isDragging)
    
    // 绘制中心装饰
    drawCenterDecoration(center)
}

/**
 * 绘制背景轨道
 */
private fun DrawScope.drawBackgroundTrack(
    center: Offset,
    radius: Float,
    strokeWidth: Float
) {
    // 绘制完整的背景圆环
    drawCircle(
        color = TimerGrayLight.copy(alpha = 0.3f),
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    
    // 绘制有效轨道范围的背景
    drawArc(
        color = TimerGrayLight.copy(alpha = 0.6f),
        startAngle = AngleCalculator.START_ANGLE,
        sweepAngle = AngleCalculator.TOTAL_ANGLE,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

/**
 * 绘制进度轨道
 */
private fun DrawScope.drawProgressTrack(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    timerState: TimerState,
    currentAngle: Float
) {
    when (timerState.status) {
        com.example.countdown.data.model.TimerStatus.IDLE -> {
            // 空闲状态：显示设置的时间范围
            if (timerState.totalTimeMs > 0) {
                val sweepAngle = (timerState.totalTimeMs.toFloat() / AngleCalculator.MAX_TIME_MS) * AngleCalculator.TOTAL_ANGLE
                drawArc(
                    color = TimerGreen,
                    startAngle = AngleCalculator.START_ANGLE,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        else -> {
            // 计时状态：显示已完成（灰色）和剩余（绿色）
            val totalSweepAngle = (timerState.totalTimeMs.toFloat() / AngleCalculator.MAX_TIME_MS) * AngleCalculator.TOTAL_ANGLE
            val progressSweepAngle = timerState.progress * totalSweepAngle
            
            // 已完成部分（灰色）
            if (progressSweepAngle > 0) {
                drawArc(
                    color = TimerGray,
                    startAngle = AngleCalculator.START_ANGLE,
                    sweepAngle = progressSweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            
            // 剩余部分（绿色）
            val remainingSweepAngle = totalSweepAngle - progressSweepAngle
            if (remainingSweepAngle > 0) {
                drawArc(
                    color = TimerGreen,
                    startAngle = AngleCalculator.START_ANGLE + progressSweepAngle,
                    sweepAngle = remainingSweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
    }
}

/**
 * 绘制拖拽点
 */
private fun DrawScope.drawDragHandle(
    center: Offset,
    radius: Float,
    angle: Float,
    isDragging: Boolean
) {
    // 计算拖拽点位置
    val angleRad = Math.toRadians(angle.toDouble())
    val handleX = center.x + radius * cos(angleRad).toFloat()
    val handleY = center.y + radius * sin(angleRad).toFloat()
    val handlePosition = Offset(handleX, handleY)
    
    // 拖拽点大小
    val handleRadius = if (isDragging) 16.dp.toPx() else 12.dp.toPx()
    val glowRadius = if (isDragging) 24.dp.toPx() else 18.dp.toPx()
    
    // 绘制光晕效果
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                TimerGreen.copy(alpha = 0.6f),
                TimerGreen.copy(alpha = 0.2f),
                Color.Transparent
            ),
            radius = glowRadius
        ),
        radius = glowRadius,
        center = handlePosition
    )
    
    // 绘制拖拽点主体
    drawCircle(
        color = TimerGreen,
        radius = handleRadius,
        center = handlePosition
    )
    
    // 绘制拖拽点内部高亮
    drawCircle(
        color = TimerGreenLight.copy(alpha = 0.8f),
        radius = handleRadius * 0.6f,
        center = handlePosition
    )
    
    // 绘制拖拽点边框
    drawCircle(
        color = Color.White.copy(alpha = 0.8f),
        radius = handleRadius,
        center = handlePosition,
        style = Stroke(width = 2.dp.toPx())
    )
}

/**
 * 绘制中心装饰
 */
private fun DrawScope.drawCenterDecoration(center: Offset) {
    val decorationRadius = 8.dp.toPx()
    
    // 绘制中心点
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                GlassHighlight,
                GlassBorder,
                Color.Transparent
            )
        ),
        radius = decorationRadius,
        center = center
    )
    
    // 绘制中心高亮点
    drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        radius = decorationRadius * 0.5f,
        center = center
    )
}