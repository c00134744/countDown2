package com.example.countdown.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.countdown.data.model.TimerAction
import com.example.countdown.data.model.TimerState
import com.example.countdown.data.model.TimerStatus
import com.example.countdown.ui.theme.*
import com.example.countdown.ui.theme.GlassmorphismEffects.glassButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 计时器控制按钮组件
 * 支持开始/暂停/停止操作，包含长按停止功能
 */
@Composable
fun ControlButton(
    timerState: TimerState,
    onAction: (TimerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    var isLongPressing by remember { mutableStateOf(false) }
    
    // 确定按钮状态和操作
    val buttonState = when (timerState.status) {
        TimerStatus.IDLE -> if (timerState.totalTimeMs > 0) ButtonState.Start else ButtonState.Disabled
        TimerStatus.RUNNING -> ButtonState.Pause
        TimerStatus.PAUSED -> ButtonState.Resume
        TimerStatus.FINISHED -> ButtonState.Reset
    }
    
    Box(
        modifier = modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        // 主按钮
        MainControlButton(
            buttonState = buttonState,
            isPressed = isPressed,
            isLongPressing = isLongPressing,
            onPress = { pressed ->
                isPressed = pressed
                if (pressed) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            },
            onTap = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                when (buttonState) {
                    ButtonState.Start -> onAction(TimerAction.START)
                    ButtonState.Pause -> onAction(TimerAction.PAUSE)
                    ButtonState.Resume -> onAction(TimerAction.START)
                    ButtonState.Reset -> onAction(TimerAction.RESET)
                    ButtonState.Disabled -> { /* 不执行任何操作 */ }
                }
            },
            onLongPress = {
                if (timerState.canStop) {
                    isLongPressing = true
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAction(TimerAction.STOP)
                    // 延迟重置长按状态
                    GlobalScope.launch {
                        delay(200)
                        isLongPressing = false
                    }
                }
            }
        )
    }
}

/**
 * 主控制按钮
 */
@Composable
private fun MainControlButton(
    buttonState: ButtonState,
    isPressed: Boolean,
    isLongPressing: Boolean,
    onPress: (Boolean) -> Unit,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    val buttonColor = when (buttonState) {
        ButtonState.Start, ButtonState.Resume -> TimerGreen
        ButtonState.Pause -> WarningColor
        ButtonState.Reset -> SuccessColor
        ButtonState.Disabled -> TimerGrayLight
    }
    
    val contentColor = when (buttonState) {
        ButtonState.Disabled -> TextDisabled
        else -> Color.White
    }
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .glassButton(isPressed = isPressed || isLongPressing)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        buttonColor.copy(alpha = if (isPressed || isLongPressing) 0.8f else 0.6f),
                        buttonColor.copy(alpha = if (isPressed || isLongPressing) 0.4f else 0.2f)
                    )
                ),
                shape = CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        onPress(true)
                        val released = try {
                            tryAwaitRelease()
                        } catch (e: Exception) {
                            false
                        }
                        onPress(false)
                        if (released) {
                            onTap()
                        }
                    },
                    onLongPress = { onLongPress() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // 按钮图标
        Icon(
            imageVector = buttonState.icon,
            contentDescription = buttonState.description,
            tint = contentColor,
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * 按钮状态枚举
 */
private enum class ButtonState(
    val icon: ImageVector,
    val description: String
) {
    Start(Icons.Default.PlayArrow, "开始计时"),
    Pause(Icons.Default.Pause, "暂停计时"),
    Resume(Icons.Default.PlayArrow, "继续计时"),
    Reset(Icons.Default.PlayArrow, "重新开始"),
    Disabled(Icons.Default.PlayArrow, "设置时间后开始")
}

/**
 * 辅助控制按钮（停止按钮）
 */
@Composable
fun StopButton(
    timerState: TimerState,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    
    if (timerState.canStop) {
        Box(
            modifier = modifier
                .size(56.dp)
                .glassButton(isPressed = isPressed)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ErrorColor.copy(alpha = if (isPressed) 0.8f else 0.6f),
                            ErrorColor.copy(alpha = if (isPressed) 0.4f else 0.2f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onStop()
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            isPressed = true
                            val released = try {
                                tryAwaitRelease()
                            } catch (e: Exception) {
                                false
                            }
                            isPressed = false
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "停止计时",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 操作提示文本
 */
@Composable
fun ControlHint(
    timerState: TimerState,
    modifier: Modifier = Modifier
) {
    val hintText = when (timerState.status) {
        TimerStatus.IDLE -> if (timerState.totalTimeMs > 0) "点击开始计时" else "先设置时间"
        TimerStatus.RUNNING -> "点击暂停 • 长按停止"
        TimerStatus.PAUSED -> "点击继续 • 长按停止"
        TimerStatus.FINISHED -> "点击重新开始"
    }
    
    Text(
        text = hintText,
        style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        ),
        color = TextSecondary,
        modifier = modifier
    )
}

/**
 * 控制按钮组合（主按钮 + 停止按钮）
 */
@Composable
fun ControlButtonGroup(
    timerState: TimerState,
    onAction: (TimerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 停止按钮（仅在可停止时显示）
        if (timerState.canStop) {
            StopButton(
                timerState = timerState,
                onStop = { onAction(TimerAction.STOP) }
            )
        }
        
        // 主控制按钮
        ControlButton(
            timerState = timerState,
            onAction = onAction
        )
    }
}