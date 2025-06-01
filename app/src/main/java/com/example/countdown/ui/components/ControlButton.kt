package com.example.countdown.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.countdown.data.model.TimerAction
import com.example.countdown.data.model.TimerState
import com.example.countdown.data.model.TimerStatus
import com.example.countdown.ui.theme.*

/**
 * 计时器控制按钮组件
 * 支持开始/暂停/停止操作，使用 combinedClickable
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ControlButton(
    timerState: TimerState,
    onAction: (TimerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(targetValue = if (isPressed) 0.9f else 1.0f, label = "buttonScale")
    val backgroundAlpha by animateFloatAsState(targetValue = if (isPressed) 0.8f else 0.6f, label = "buttonAlpha")

    val buttonStateLogic = when (timerState.status) {
        TimerStatus.IDLE -> if (timerState.totalTimeMs > 0) ButtonDisplayState.Start else ButtonDisplayState.Disabled
        TimerStatus.RUNNING -> ButtonDisplayState.Pause
        TimerStatus.PAUSED -> ButtonDisplayState.Resume
        TimerStatus.FINISHED -> ButtonDisplayState.Reset
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(
                color = buttonStateLogic.baseColor,
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material.ripple.rememberRipple(bounded = false, radius = 40.dp),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    when (timerState.status) {
                        TimerStatus.IDLE -> if (timerState.totalTimeMs > 0) onAction(TimerAction.START)
                        TimerStatus.RUNNING -> onAction(TimerAction.PAUSE)
                        TimerStatus.PAUSED -> onAction(TimerAction.START)
                        TimerStatus.FINISHED -> onAction(TimerAction.RESET)
                        else -> {}
                    }
                },
                onLongClick = {
                    if (timerState.canStop) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAction(TimerAction.STOP)
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = buttonStateLogic.icon,
            contentDescription = buttonStateLogic.description,
            tint = buttonStateLogic.iconTintColor,
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * Represents the visual state of the button (icon, colors)
 */
private enum class ButtonDisplayState(
    val icon: ImageVector,
    val description: String,
    val baseColor: Color,
    val iconTintColor: Color
) {
    Start(Icons.Default.PlayArrow, "开始计时", TimerGreen, Color.Green),
    Pause(Icons.Default.Pause, "暂停计时", WarningColor, Color.Red),
    Resume(Icons.Default.PlayArrow, "继续计时", TimerGreen, Color.Green),
    Reset(Icons.Default.PlayArrow, "重新开始", SuccessColor, Color.Blue),
    Disabled(Icons.Default.PlayArrow, "设置时间后开始", TimerGrayLight, Color.DarkGray)
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
 * 控制按钮组合（主按钮）
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
        ControlButton(
            timerState = timerState,
            onAction = onAction
        )
    }
}