# 系统设计模式

## 整体架构

### 架构模式: MVVM + Repository + Service
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   UI Layer      │    │  Business Layer │    │  Service Layer  │
│                 │    │                 │    │                 │
│ • TimerScreen   │◄──►│ • TimerViewModel│◄──►│ • TimerService  │
│ • CircularTimer │    │ • TimerRepo     │    │ • NotificationMgr│
│ • ControlButton │    │ • StateFlow     │    │ • VibrationMgr  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 核心设计原则
1. **单一职责**: 每个组件只负责一个明确的功能
2. **依赖注入**: 通过构造函数注入依赖，便于测试
3. **响应式编程**: 使用StateFlow进行状态管理
4. **分层架构**: UI、业务逻辑、数据访问分离

## 关键技术选择

### UI框架: Jetpack Compose
**选择原因**:
- 声明式UI，代码简洁
- 原生支持状态管理
- 优秀的动画和自定义绘制能力
- Google官方推荐的现代UI框架

**实现模式**:
- 自定义Canvas组件绘制圆形轨道
- Modifier链式调用实现样式组合
- remember和mutableStateOf管理局部状态
- collectAsStateWithLifecycle连接ViewModel

### 状态管理: StateFlow + ViewModel
**选择原因**:
- 生命周期感知，避免内存泄漏
- 线程安全的状态更新
- 支持背压处理
- 与Compose完美集成

**实现模式**:
```kotlin
class TimerViewModel : ViewModel() {
    private val _timerState = MutableStateFlow(TimerState.initial())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
    fun updateState(newState: TimerState) {
        _timerState.value = newState
    }
}
```

### 后台计时: 前台服务
**选择原因**:
- 系统不会随意杀死前台服务
- 可以在后台精确计时
- 用户可见的通知提供透明度
- 符合Android后台执行限制

**实现模式**:
- 使用系统时间戳确保精确性
- 定时器每100ms更新一次
- 通过Binder与UI层通信
- 计时结束自动停止服务

## 设计模式应用

### 1. 观察者模式 (Observer Pattern)
**应用场景**: UI组件监听计时状态变化
```kotlin
@Composable
fun TimerScreen(viewModel: TimerViewModel) {
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    // UI根据状态变化自动更新
}
```

### 2. 策略模式 (Strategy Pattern)
**应用场景**: 不同计时状态下的行为策略
```kotlin
sealed class TimerAction {
    object Start : TimerAction()
    object Pause : TimerAction()
    object Stop : TimerAction()
    object Reset : TimerAction()
}
```

### 3. 工厂模式 (Factory Pattern)
**应用场景**: 创建不同类型的通知
```kotlin
object NotificationFactory {
    fun createTimerNotification(state: TimerState): Notification
    fun createCompletionNotification(): Notification
}
```

### 4. 单例模式 (Singleton Pattern)
**应用场景**: 全局配置和工具类
```kotlin
object AngleCalculator {
    const val START_ANGLE = 225f
    const val TOTAL_ANGLE = 270f
    const val MAX_TIME_MS = 45 * 60 * 1000L
}
```

## 数据流设计

### 单向数据流
```
User Action → ViewModel → Repository → Service
     ↑                                    ↓
UI Update ← StateFlow ← State Update ← Timer Event
```

### 状态管理模式
```kotlin
data class TimerState(
    val status: TimerStatus,
    val totalTimeMs: Long,
    val remainingTimeMs: Long,
    val progress: Float,
    val angle: Float,
    val isScreenOn: Boolean
) {
    val canStart: Boolean get() = status == TimerStatus.IDLE && totalTimeMs > 0
    val canPause: Boolean get() = status == TimerStatus.RUNNING
    val canStop: Boolean get() = status in listOf(TimerStatus.RUNNING, TimerStatus.PAUSED)
}
```

## 组件连接模式

### UI组件组合
```kotlin
@Composable
fun TimerScreen(viewModel: TimerViewModel) {
    Column {
        CircularTimerView(
            timerState = timerState,
            onTimeSet = viewModel::setTime
        )
        TimerDisplay(timerState = timerState)
        ControlButton(
            timerState = timerState,
            onAction = viewModel::handleAction
        )
    }
}
```

### 服务通信模式
```kotlin
class TimerViewModel : ViewModel() {
    private var serviceBinder: TimerService.TimerBinder? = null
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serviceBinder = service as TimerService.TimerBinder
            syncWithService()
        }
    }
}
```

## 错误处理模式

### 分层错误处理
1. **UI层**: 显示用户友好的错误信息
2. **业务层**: 处理业务逻辑错误，提供降级方案
3. **服务层**: 记录系统错误，确保服务稳定性

### 错误恢复策略
```kotlin
object ErrorHandler {
    fun handleTimeSetError(timeMs: Long): Long {
        return when {
            timeMs < 0 -> 0L
            timeMs > MAX_TIME_MS -> MAX_TIME_MS
            else -> timeMs
        }
    }
    
    inline fun <T> safeExecute(
        operation: () -> T,
        onError: (Exception) -> T
    ): T {
        return try {
            operation()
        } catch (e: Exception) {
            Log.e(TAG, "操作执行失败", e)
            onError(e)
        }
    }
}
```

## 性能优化模式

### 1. 懒加载模式
- 服务只在需要时启动
- 重量级资源延迟初始化

### 2. 对象池模式
- 复用Notification对象
- 减少GC压力

### 3. 缓存模式
- 缓存计算结果（角度转换等）
- 避免重复计算

### 4. 批量更新模式
- UI更新频率控制（60fps）
- 状态变更批量处理 

### UI布局模式
```kotlin
@Composable
fun TimerScreen(viewModel: TimerViewModel) {
    Column {
        CircularTimerView(
            timerState = timerState,
            onTimeSet = viewModel::setTime
        )
        TimerDisplay(timerState = timerState)
        ControlButton(
            timerState = timerState,
            onAction = viewModel::handleAction
        )
    }
}
```

#### 新增：分层Box布局模式 (2025-06-02)
**应用场景**: 精确控制UI元素位置，实现复杂的重叠布局

**设计模式**:
```kotlin
@Composable
fun MainTimerSection(
    timerState: TimerState,
    onTimeSet: (Long) -> Unit,
    onAction: (TimerAction) -> Unit,
    onDragAngle: (Float) -> Unit
) {
    Box(
        modifier = Modifier.size(350.dp),
        contentAlignment = Alignment.Center
    ) {
        // Layer 1: 背景圆形轨道
        CircularTimerView(
            timerState = timerState,
            onTimeSet = onTimeSet,
            size = 350.dp
        )
        
        // Layer 2: 中心时间显示
        TimerDisplay(
            timerState = timerState,
            modifier = Modifier.align(Alignment.Center)
        )
        
        // Layer 3: 精确定位的控制按钮
        ControlButton(
            timerState = timerState,
            onAction = onAction,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.55f)
                .height(56.dp)
                .offset(y = (-250).dp) // 精确偏移定位
        )
    }
}
```

**优势**:
- 精确控制元素位置
- 支持重叠布局
- 灵活的对齐方式
- 易于维护和调整

**使用场景**:
- 需要精确控制位置的UI元素
- 圆形布局中的元素定位
- 复杂的视觉层次设计 