package com.example.countdown

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.example.countdown.ui.screens.TimerScreen
import com.example.countdown.ui.theme.CountDownTheme
import com.example.countdown.utils.PermissionHelper
import com.example.countdown.viewmodel.TimerViewModel

/**
 * 主Activity
 * 负责权限管理、屏幕控制和UI集成
 */
class MainActivity : ComponentActivity() {
    
    private val viewModel: TimerViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 启用边到边显示
        enableEdgeToEdge()
        
        // 设置状态栏和导航栏透明
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // 请求必要权限
        requestPermissions()
        
        setContent {
            CountDownTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimerApp(viewModel = viewModel)
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.onAppForeground()
        viewModel.syncWithService()
    }
    
    override fun onPause() {
        super.onPause()
        viewModel.onAppBackground()
    }
    
    /**
     * 请求必要权限
     */
    private fun requestPermissions() {
        if (!PermissionHelper.hasAllRequiredPermissions(this)) {
            PermissionHelper.requestAllRequiredPermissions(this)
        }
    }
    
    // 注意：onRequestPermissionsResult 在新版本Android中已过时
    // 现在使用 Activity Result API 或 Compose 权限处理
    // 权限处理逻辑已移至 PermissionHelper 中
}

/**
 * 计时器应用主组件
 */
@Composable
fun TimerApp(
    viewModel: TimerViewModel
) {
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    
    // 屏幕常亮控制
    ScreenKeepOnEffect(shouldKeepOn = timerState.isScreenOn)
    
    // 主界面
    TimerScreen(
        viewModel = viewModel,
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * 屏幕常亮效果
 */
@Composable
fun ScreenKeepOnEffect(shouldKeepOn: Boolean) {
    val activity = androidx.compose.ui.platform.LocalContext.current as? ComponentActivity
    
    DisposableEffect(shouldKeepOn) {
        if (shouldKeepOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        
        onDispose {
            // 清理时移除屏幕常亮标志
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}