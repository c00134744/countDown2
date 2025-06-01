package com.example.countdown.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.countdown.data.model.TimerState
import com.example.countdown.data.model.TimerStatus
import com.example.countdown.ui.theme.*
import com.example.countdown.utils.TimeFormatter

/**
 * 计时器时间显示组件
 * 显示在圆形计时器中央的时间信息
 */
@Composable
fun TimerDisplay(
    timerState: TimerState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 主要时间显示
        MainTimeDisplay(timerState = timerState)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 状态指示器
        StatusIndicator(timerState = timerState)
        
        // 进度信息（仅在计时时显示）
        if (timerState.status != TimerStatus.IDLE) {
            Spacer(modifier = Modifier.height(4.dp))
            ProgressInfo(timerState = timerState)
        }
    }
}

/**
 * 主要时间显示
 */
@Composable
private fun MainTimeDisplay(timerState: TimerState) {
    val displayTime = when (timerState.status) {
        TimerStatus.IDLE -> timerState.totalTimeMs
        else -> timerState.remainingTimeMs
    }
    
    val timeText = TimeFormatter.formatTime(displayTime)
    val textColor = when (timerState.status) {
        TimerStatus.IDLE -> TextSecondary
        TimerStatus.RUNNING -> TextPrimary
        TimerStatus.PAUSED -> WarningColor
        TimerStatus.FINISHED -> SuccessColor
    }
    
    Text(
        text = timeText,
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        ),
        color = textColor,
        textAlign = TextAlign.Center
    )
}

/**
 * 状态指示器
 */
@Composable
private fun StatusIndicator(timerState: TimerState) {
    val statusText = when (timerState.status) {
        TimerStatus.IDLE -> "设置时间"
        TimerStatus.RUNNING -> "计时中"
        TimerStatus.PAUSED -> "已暂停"
        TimerStatus.FINISHED -> "时间到！"
    }
    
    val statusColor = when (timerState.status) {
        TimerStatus.IDLE -> TextDisabled
        TimerStatus.RUNNING -> TimerGreen
        TimerStatus.PAUSED -> WarningColor
        TimerStatus.FINISHED -> SuccessColor
    }
    
    Text(
        text = statusText,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        ),
        color = statusColor,
        textAlign = TextAlign.Center
    )
}

/**
 * 进度信息显示
 */
@Composable
private fun ProgressInfo(timerState: TimerState) {
    val progressText = TimeFormatter.formatProgress(timerState.progress)
    val elapsedText = TimeFormatter.formatTimeShort(timerState.elapsedTimeMs)
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = progressText,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        
        Text(
            text = "•",
            style = MaterialTheme.typography.bodySmall,
            color = TextDisabled
        )
        
        Text(
            text = elapsedText,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

/**
 * 简化版时间显示（用于小尺寸场景）
 */
@Composable
fun CompactTimerDisplay(
    timerState: TimerState,
    modifier: Modifier = Modifier
) {
    val displayTime = when (timerState.status) {
        TimerStatus.IDLE -> timerState.totalTimeMs
        else -> timerState.remainingTimeMs
    }
    
    val timeText = TimeFormatter.formatTime(displayTime)
    val textColor = when (timerState.status) {
        TimerStatus.IDLE -> TextSecondary
        TimerStatus.RUNNING -> TextPrimary
        TimerStatus.PAUSED -> WarningColor
        TimerStatus.FINISHED -> SuccessColor
    }
    
    Text(
        text = timeText,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold
        ),
        color = textColor,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

/**
 * 时间设置提示显示
 */
@Composable
fun TimeSetupHint(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "拖拽设置时间",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "最大45分钟",
            style = MaterialTheme.typography.bodySmall,
            color = TextDisabled,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 计时完成提示
 */
@Composable
fun TimerFinishedDisplay(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⏰",
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "时间到！",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = SuccessColor,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "计时完成",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}