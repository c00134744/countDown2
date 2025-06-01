package com.example.countdown.ui.theme

import androidx.compose.ui.graphics.Color

// 暗黑主题色彩方案
// 主要颜色 - 绿色系（用于轨道和强调）
val TimerGreen = Color(0xFF00E676)          // 明亮绿色 - 未完成轨道
val TimerGreenDark = Color(0xFF00C853)      // 深绿色 - 按钮激活状态
val TimerGreenLight = Color(0xFF69F0AE)     // 浅绿色 - 高亮效果

// 灰色系（用于已完成轨道和背景）
val TimerGray = Color(0xFF424242)           // 中灰色 - 已完成轨道
val TimerGrayDark = Color(0xFF212121)       // 深灰色 - 主背景
val TimerGrayLight = Color(0xFF616161)      // 浅灰色 - 次要元素

// 玻璃态效果颜色
val GlassBackground = Color(0x1AFFFFFF)     // 半透明白色背景
val GlassBorder = Color(0x33FFFFFF)        // 半透明白色边框
val GlassHighlight = Color(0x66FFFFFF)     // 高亮效果

// 文本颜色
val TextPrimary = Color(0xFFFFFFFF)        // 主要文本 - 白色
val TextSecondary = Color(0xB3FFFFFF)      // 次要文本 - 半透明白色
val TextDisabled = Color(0x61FFFFFF)       // 禁用文本 - 更透明白色

// 状态颜色
val SuccessColor = Color(0xFF4CAF50)       // 成功状态
val WarningColor = Color(0xFFFF9800)       // 警告状态
val ErrorColor = Color(0xFFF44336)         // 错误状态

// 阴影和深度
val ShadowColor = Color(0x40000000)        // 阴影颜色
val ElevationColor = Color(0x0DFFFFFF)     // 高度效果

// 兼容性颜色（保留原有命名以避免编译错误）
val Purple80 = TimerGreenLight
val PurpleGrey80 = TimerGrayLight
val Pink80 = TimerGreenLight

val Purple40 = TimerGreen
val PurpleGrey40 = TimerGray
val Pink40 = TimerGreenDark