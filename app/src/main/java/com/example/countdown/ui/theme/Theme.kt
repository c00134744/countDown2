package com.example.countdown.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// 倒计时应用专用暗黑主题
private val CountDownDarkColorScheme = darkColorScheme(
    primary = TimerGreen,
    onPrimary = Color.Black,
    secondary = TimerGreenDark,
    onSecondary = Color.White,
    tertiary = TimerGreenLight,
    onTertiary = Color.Black,
    
    background = TimerGrayDark,
    onBackground = TextPrimary,
    surface = TimerGray,
    onSurface = TextPrimary,
    
    surfaceVariant = TimerGrayLight,
    onSurfaceVariant = TextSecondary,
    
    outline = GlassBorder,
    outlineVariant = GlassBackground,
    
    error = ErrorColor,
    onError = Color.White
)

// 保留浅色主题（但应用主要使用暗黑主题）
private val CountDownLightColorScheme = lightColorScheme(
    primary = TimerGreenDark,
    onPrimary = Color.White,
    secondary = TimerGreen,
    onSecondary = Color.Black,
    tertiary = TimerGreenLight,
    onTertiary = Color.Black,
    
    background = Color.White,
    onBackground = Color.Black,
    surface = Color(0xFFF5F5F5),
    onSurface = Color.Black
)

@Composable
fun CountDownTheme(
    darkTheme: Boolean = true, // 默认使用暗黑主题
    // 禁用动态颜色，使用自定义主题
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // 优先使用自定义暗黑主题
        darkTheme -> CountDownDarkColorScheme
        else -> CountDownLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}