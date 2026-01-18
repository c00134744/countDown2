package com.example.countdown.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.countdown.data.model.TimerAction
import com.example.countdown.data.model.TimerState
import com.example.countdown.data.model.TimerStatus
import com.example.countdown.data.repository.TimerRepository
import com.example.countdown.service.TimerForegroundService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 计时器ViewModel
 * 管理UI状态和与前台服务的通信
 */
class TimerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = TimerRepository(application)
    private var timerService: TimerForegroundService? = null
    private var isServiceBound = false
    private var isBindingInProgress = false  // 防止重复绑定
    
    // 暴露计时器状态
    val timerState: StateFlow<TimerState> = repository.timerState
    
    // 服务连接
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerForegroundService.TimerBinder
            timerService = binder.getService()
            isServiceBound = true
            isBindingInProgress = false
            
            // 设置服务的状态更新回调
            timerService?.setOnTimerUpdateListener { remainingTimeMs ->
                repository.updateProgress(remainingTimeMs)
            }
            
            // 设置计时完成回调
            timerService?.setOnTimerFinishedListener {
                handleTimerFinished()
            }
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            isServiceBound = false
            isBindingInProgress = false
        }
    }
    
    /**
     * 处理计时器操作
     */
    fun handleAction(action: TimerAction) {
        viewModelScope.launch {
            when (action) {
                TimerAction.START -> startTimer()
                TimerAction.PAUSE -> pauseTimer()
                TimerAction.STOP -> stopTimer()
                TimerAction.RESET -> resetTimer()
                TimerAction.SET_TIME -> { /* 由setTime方法处理 */ }
                TimerAction.TICK -> { /* 由服务处理 */ }
            }
        }
    }
    
    /**
     * 设置计时时间
     */
    fun setTime(timeMs: Long) {
        repository.setTimerTime(timeMs)
    }
    
    /**
     * 设置拖拽角度
     */
    fun setDragAngle(angle: Float) {
        repository.setDragAngle(angle)
    }
    
    /**
     * 开始计时
     */
    private fun startTimer() {
        val currentState = repository.getCurrentState()
        if (currentState.canStart && currentState.totalTimeMs > 0) {
            repository.startTimer()
            startTimerService()
        }
    }
    
    /**
     * 暂停计时
     */
    private fun pauseTimer() {
        repository.pauseTimer()
        pauseTimerService()
    }
    
    /**
     * 停止计时
     */
    private fun stopTimer() {
        repository.stopTimer()
        timerService?.userInitiatedStop()
        stopTimerServiceItself()
    }
    
    /**
     * 重置计时器
     */
    private fun resetTimer() {
        repository.resetTimer()
        timerService?.userInitiatedStop()
        stopTimerServiceItself()
    }
    
    /**
     * 启动前台服务
     */
    private fun startTimerService() {
        val context = getApplication<Application>()
        val currentState = repository.getCurrentState()
        
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            putExtra(TimerForegroundService.EXTRA_TOTAL_TIME, currentState.totalTimeMs)
            putExtra(TimerForegroundService.EXTRA_REMAINING_TIME, currentState.remainingTimeMs)
        }
        
        // 无论是否已绑定，都启动服务以触发 onStartCommand (处理开始/恢复逻辑)
        // startForegroundService 是幂等的，如果服务已运行，只会调用 onStartCommand
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start service: ${e.message}")
        }
        
        // 仅在未绑定且未在绑定过程中时进行绑定
        if (!isServiceBound && !isBindingInProgress) {
            isBindingInProgress = true
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }
    
    /**
     * 暂停计时服务
     */
    private fun pauseTimerService() {
        timerService?.pauseTimer()
    }
    
    /**
     * 停止计时服务 (核心逻辑，停止服务本身和解绑)
     */
    private fun stopTimerServiceItself() {
        val context = getApplication<Application>()
        
        // 解绑服务
        if (isServiceBound) {
            try {
                context.unbindService(serviceConnection)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Service not registered or already unbound: ${e.message}")
            }
            isServiceBound = false
        }
        
        // 停止前台服务
        val intent = Intent(context, TimerForegroundService::class.java)
        context.stopService(intent)
        
        timerService = null
    }
    
    /**
     * 处理计时完成
     */
    private fun handleTimerFinished() {
        viewModelScope.launch {
            // 计时完成后5秒自动关闭屏幕常亮
            kotlinx.coroutines.delay(5000)
            repository.setScreenOn(false)
        }
    }
    
    /**
     * 获取当前状态
     */
    fun getCurrentState(): TimerState = repository.getCurrentState()
    
    /**
     * 检查是否正在计时
     */
    fun isTimerRunning(): Boolean = repository.isTimerRunning()
    
    /**
     * 获取保存的设置
     */
    fun getSavedSettings(): Pair<Long, Float> = repository.getSavedTimerSettings()
    
    /**
     * 清除所有数据
     */
    fun clearAllData() {
        stopTimerServiceItself()
        repository.clearAllData()
    }
    
    /**
     * 处理应用进入后台
     */
    fun onAppBackground() {
        // 如果正在计时，确保服务继续运行
        val currentState = repository.getCurrentState()
        if (currentState.status == TimerStatus.RUNNING && !isServiceBound && !isBindingInProgress) {
            startTimerService()
        }
    }
    
    /**
     * 处理应用回到前台
     */
    fun onAppForeground() {
        // 如果服务正在运行，重新绑定以获取最新状态
        val currentState = repository.getCurrentState()
        
        // 尝试绑定服务以同步状态，无论本地状态如何（因为服务可能在后台运行中）
        if (!isServiceBound && !isBindingInProgress) {
            isBindingInProgress = true
            val context = getApplication<Application>()
            val intent = Intent(context, TimerForegroundService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        } else {
             // 如果已经绑定，主动同步一次
             syncWithService()
        }
    }
    
    /**
     * 同步服务状态
     */
    fun syncWithService() {
        timerService?.let { service ->
            val serviceRemainingTime = service.getRemainingTime()
            val currentState = repository.getCurrentState()
            
            // 如果服务和本地状态不一致，以服务状态为准
            if (Math.abs(serviceRemainingTime - currentState.remainingTimeMs) > 1000) {
                repository.updateProgress(serviceRemainingTime)
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // 清理资源
        if (isServiceBound) {
            val context = getApplication<Application>()
            try {
                context.unbindService(serviceConnection)
            } catch (e: IllegalArgumentException) {
                 Log.w(TAG, "Service not registered or already unbound in onCleared: ${e.message}")
            }
            isServiceBound = false
        }
        
        // 如果不是在计时状态，确保服务也停止
        val currentState = repository.getCurrentState()
        if (currentState.status != TimerStatus.RUNNING) {
            timerService?.userInitiatedStop()
            stopTimerServiceItself()
        }
        timerService = null
    }
    
    companion object {
        private const val TAG = "TimerViewModel"
    }
}