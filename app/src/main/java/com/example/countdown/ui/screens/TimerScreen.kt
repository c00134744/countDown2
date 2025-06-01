package com.example.countdown.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.countdown.data.model.TimerAction
import com.example.countdown.data.model.TimerStatus
import com.example.countdown.ui.components.*
import com.example.countdown.ui.theme.GlassStyles.mainContainer
import com.example.countdown.viewmodel.TimerViewModel

/**
 * 计时器主界面
 * 整合所有UI组件，提供完整的用户体验
 */
@Composable
fun TimerScreen(
    viewModel: TimerViewModel,
    modifier: Modifier = Modifier
) {
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .mainContainer()
    ) {
        if (isLandscape) {
            LandscapeLayout(
                timerState = timerState,
                onTimeSet = viewModel::setTime,
                onDragAngle = viewModel::setDragAngle,
                onAction = viewModel::handleAction
            )
        } else {
            PortraitLayout(
                timerState = timerState,
                onTimeSet = viewModel::setTime,
                onDragAngle = viewModel::setDragAngle,
                onAction = viewModel::handleAction
            )
        }
        
        // 计时完成动画覆盖层
        AnimatedVisibility(
            visible = timerState.status == TimerStatus.FINISHED,
            enter = fadeIn(animationSpec = tween(500)) + scaleIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(300)) + scaleOut(animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            TimerFinishedOverlay(
                onDismiss = { viewModel.handleAction(TimerAction.RESET) }
            )
        }
    }
}

/**
 * 竖屏布局
 */
@Composable
private fun PortraitLayout(
    timerState: com.example.countdown.data.model.TimerState,
    onTimeSet: (Long) -> Unit,
    onDragAngle: (Float) -> Unit,
    onAction: (TimerAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // 顶部状态信息
        TopStatusSection(timerState = timerState)
        
        // 主要计时器区域
        MainTimerSection(
            timerState = timerState,
            onTimeSet = onTimeSet,
            onDragAngle = onDragAngle,
            onAction = onAction,
            modifier = Modifier.weight(1f)
        )
        
        // 底部提示区域
        BottomHintSection(timerState = timerState)
    }
}

/**
 * 横屏布局
 */
@Composable
private fun LandscapeLayout(
    timerState: com.example.countdown.data.model.TimerState,
    onTimeSet: (Long) -> Unit,
    onDragAngle: (Float) -> Unit,
    onAction: (TimerAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧计时器
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            CircularTimerView(
                timerState = timerState,
                onTimeSet = { timeMs ->
                    onTimeSet(timeMs)
                    onDragAngle(com.example.countdown.utils.AngleCalculator.timeToAngle(timeMs))
                },
                size = 280.dp
            )
            
            TimerDisplay(
                timerState = timerState,
                modifier = Modifier.padding(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(24.dp))
        
        // 右侧控制面板
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            TopStatusSection(timerState = timerState)
            BottomControlSection(timerState = timerState, onAction = onAction)
        }
    }
}

/**
 * 顶部状态区域
 */
@Composable
private fun TopStatusSection(
    timerState: com.example.countdown.data.model.TimerState
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 应用标题
        Text(
            text = "倒计时",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        // 状态指示器
        AnimatedVisibility(
            visible = timerState.status != TimerStatus.IDLE,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            StatusIndicatorCard(timerState = timerState)
        }
    }
}

/**
 * 主计时器区域
 */
@Composable
private fun MainTimerSection(
    timerState: com.example.countdown.data.model.TimerState,
    onTimeSet: (Long) -> Unit,
    onDragAngle: (Float) -> Unit,
    onAction: (TimerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(), // Parent Box for overall centering
        contentAlignment = Alignment.Center
    ) {
        // Layer 1: CircularTimerView (Temporarily without animation for diagnosis)
        // val scale by animateFloatAsState(...)
        // val rotation by animateFloatAsState(...)
        
        Box(
            modifier = Modifier
                // .graphicsLayer { // Temporarily removed for diagnosis
                //     scaleX = scale
                //     scaleY = scale
                //     rotationZ = rotation
                // }
                .size(320.dp), // Define the size for the Box
            contentAlignment = Alignment.Center
        ) {
            CircularTimerView(
                timerState = timerState,
                onTimeSet = { timeMs ->
                    onTimeSet(timeMs)
                    onDragAngle(com.example.countdown.utils.AngleCalculator.timeToAngle(timeMs))
                },
                size = 320.dp // CircularTimerView fills this Box
            )
        }

        // Layer 2: TimerDisplay and ControlButton, overlaid
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 时间显示
            when (timerState.status) {
                TimerStatus.IDLE -> {
                    if (timerState.totalTimeMs > 0) {
                        TimerDisplay(timerState = timerState)
                    } else {
                        TimeSetupHint()
                    }
                }
                TimerStatus.FINISHED -> {
                    TimerFinishedDisplay()
                }
                else -> {
                    TimerDisplay(timerState = timerState)
                }
            }
            
            // 控制按钮
            ControlButton(
                timerState = timerState,
                onAction = onAction,
                modifier = Modifier.size(80.dp)
            )
        }
    }
}

/**
 * 底部控制区域
 */
@Composable
private fun BottomControlSection(
    timerState: com.example.countdown.data.model.TimerState,
    onAction: (TimerAction) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 主控制按钮
        ControlButtonGroup(
            timerState = timerState,
            onAction = onAction
        )
        
        // 操作提示
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ControlHint(timerState = timerState)
        }
    }
}

/**
 * 底部提示区域
 */
@Composable
private fun BottomHintSection(
    timerState: com.example.countdown.data.model.TimerState
) {
    // 操作提示
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ControlHint(timerState = timerState)
    }
}

/**
 * 状态指示卡片
 */
@Composable
private fun StatusIndicatorCard(
    timerState: com.example.countdown.data.model.TimerState
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "进度: ${(timerState.progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "•",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            
            Text(
                text = "已用: ${com.example.countdown.utils.TimeFormatter.formatTimeShort(timerState.elapsedTimeMs)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 计时完成覆盖层
 */
@Composable
private fun TimerFinishedOverlay(
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                TimerFinishedDisplay()
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("重新开始")
                }
            }
        }
    }
}