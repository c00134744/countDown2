# 技术上下文

## 技术栈

### 核心技术
- **开发语言**: Kotlin 1.9.0
- **UI框架**: Jetpack Compose (BOM 2024.04.01)
- **架构组件**: Android Architecture Components
- **构建工具**: Gradle 8.6.0 + Kotlin DSL
- **最低API**: Android 7.0 (API 24)
- **目标API**: Android 14 (API 34)

### 主要依赖库
```kotlin
// Compose核心
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.ui:ui-graphics")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-core")
implementation("androidx.compose.material:material-icons-extended")

// 架构组件
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
implementation("androidx.activity:activity-compose:1.8.2")

// 后台任务
implementation("androidx.work:work-runtime-ktx:2.9.0")
```

### 开发环境
- **IDE**: Android Studio Hedgehog | 2023.1.1+
- **JDK**: Java 8 (1.8)
- **Gradle**: 8.7
- **Kotlin编译器**: 1.9.0

## 项目配置

### 模块结构
```
app/
├── src/main/java/com/example/countdown/
│   ├── data/
│   │   ├── model/          # 数据模型
│   │   └── repository/     # 数据仓库
│   ├── service/            # 后台服务
│   ├── ui/
│   │   ├── components/     # UI组件
│   │   ├── screens/        # 界面
│   │   └── theme/          # 主题样式
│   ├── utils/              # 工具类
│   ├── viewmodel/          # 视图模型
│   └── MainActivity.kt     # 主Activity
└── src/main/res/
    ├── values/             # 资源文件
    └── AndroidManifest.xml # 清单文件
```

### 构建配置
```kotlin
android {
    namespace = "com.example.countdown"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.example.countdown"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}
```

## 权限配置

### 必需权限
```xml
<!-- 震动权限 -->
<uses-permission android:name="android.permission.VIBRATE" />

<!-- 前台服务权限 -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

<!-- 屏幕常亮权限 -->
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- 通知权限 (Android 13+) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### 服务声明
```xml
<service
    android:name=".service.TimerForegroundService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="shortService" />
```

**注意**: 
- 使用`shortService`类型适合短时间任务（最长3小时）
- 无需额外权限，符合Android 15规范
- 之前使用`specialUse`类型在Android 15上会导致权限错误

## 开发设置

### 代码规范
- **命名规范**: 遵循Kotlin官方命名规范
- **包结构**: 按功能模块组织，避免按类型组织
- **注释规范**: 使用KDoc格式，重要方法必须有注释
- **代码格式**: 使用Android Studio默认格式化规则

### Git配置
```gitignore
# Android
*.iml
.gradle
/local.properties
/.idea/
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties

# Compose
/app/build/
```

## 技术限制

### Android版本兼容性
- **最低支持**: Android 7.0 (API 24)
- **目标版本**: Android 14 (API 34)
- **测试覆盖**: Android 7.0 - Android 14

### 性能约束
- **内存限制**: 运行时内存 < 50MB
- **电池优化**: 45分钟计时电池消耗 < 5%
- **启动时间**: 冷启动 < 2秒
- **响应时间**: UI交互响应 < 100ms

### 功能限制
- **计时精度**: 系统时间戳精度限制
- **后台执行**: Android后台执行限制
- **通知权限**: Android 13+需要运行时权限
- **电池优化**: 厂商电池优化可能影响后台运行

## 依赖管理

### 版本管理策略
- 使用`libs.versions.toml`统一管理版本
- 定期更新依赖库到稳定版本
- 避免使用alpha/beta版本的依赖

### 关键依赖版本
```toml
[versions]
agp = "8.6.0"
kotlin = "1.9.0"
composeBom = "2024.04.01"
lifecycleRuntimeKtx = "2.8.6"
activityCompose = "1.9.2"
```

## 构建和部署

### 构建类型
```kotlin
buildTypes {
    debug {
        isDebuggable = true
        applicationIdSuffix = ".debug"
    }
    
    release {
        isMinifyEnabled = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### 签名配置
- 开发环境使用debug签名
- 生产环境需要配置release签名
- 签名文件不提交到版本控制

### 混淆配置
```proguard
# Compose相关
-keep class androidx.compose.** { *; }
-keep class kotlin.** { *; }

# 自定义类保持
-keep class com.example.countdown.data.model.** { *; }
```

## 测试策略

### 单元测试
- 使用JUnit 4进行单元测试
- 测试覆盖率目标 > 80%
- 重点测试业务逻辑和工具类

### UI测试
- 使用Compose Testing进行UI测试
- 测试关键用户交互流程
- 验证UI状态变化

### 集成测试
- 测试ViewModel与Repository交互
- 测试Service与UI层通信
- 验证权限处理流程

## 性能监控

### 关键指标
- **启动时间**: 使用Android Studio Profiler监控
- **内存使用**: 监控内存泄漏和GC频率
- **电池消耗**: 使用Battery Historian分析
- **网络请求**: 无网络请求，无需监控

### 调试工具
- **Layout Inspector**: 检查Compose UI层次
- **Memory Profiler**: 分析内存使用
- **CPU Profiler**: 分析性能瓶颈
- **Logcat**: 运行时日志分析 