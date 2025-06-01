package com.example.countdown.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Glassmorphism效果工具类
 * 提供玻璃态UI效果的Modifier扩展
 */
object GlassmorphismEffects {
    
    /**
     * 创建玻璃态背景效果
     * @param shape 形状
     * @param borderWidth 边框宽度
     * @param blurRadius 模糊半径
     * @return Modifier
     */
    @Composable
    fun Modifier.glassmorphism(
        shape: Shape = RoundedCornerShape(16.dp),
        borderWidth: Dp = 1.dp,
        blurRadius: Dp = 10.dp
    ): Modifier {
        return this
            .clip(shape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GlassBackground,
                        GlassBackground.copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GlassBorder,
                        Color.Transparent,
                        GlassBorder
                    )
                ),
                shape = shape
            )
    }
    
    /**
     * 创建圆形玻璃态效果
     * @param borderWidth 边框宽度
     * @return Modifier
     */
    @Composable
    fun Modifier.glassCircle(
        borderWidth: Dp = 2.dp
    ): Modifier {
        return this.glassmorphism(
            shape = CircleShape,
            borderWidth = borderWidth
        )
    }
    
    /**
     * 创建按钮玻璃态效果
     * @param isPressed 是否按下状态
     * @return Modifier
     */
    @Composable
    fun Modifier.glassButton(
        isPressed: Boolean = false
    ): Modifier {
        val backgroundColor = if (isPressed) {
            GlassBackground.copy(alpha = 0.3f)
        } else {
            GlassBackground
        }
        
        return this
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = GlassBorder,
                shape = CircleShape
            )
    }
    
    /**
     * 创建高亮边框效果
     * @param color 高亮颜色
     * @param width 边框宽度
     * @return Modifier
     */
    @Composable
    fun Modifier.glowBorder(
        color: Color = GlassHighlight,
        width: Dp = 2.dp
    ): Modifier {
        return this.border(
            width = width,
            brush = Brush.sweepGradient(
                colors = listOf(
                    color,
                    Color.Transparent,
                    color,
                    Color.Transparent,
                    color
                )
            ),
            shape = CircleShape
        )
    }
    
    /**
     * 创建渐变背景
     * @param colors 渐变颜色列表
     * @param shape 形状
     * @return Modifier
     */
    @Composable
    fun Modifier.gradientBackground(
        colors: List<Color> = listOf(
            TimerGrayDark,
            TimerGrayDark.copy(alpha = 0.8f)
        ),
        shape: Shape = RoundedCornerShape(0.dp)
    ): Modifier {
        return this
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(colors = colors)
            )
    }
    
    /**
     * 创建阴影效果
     * @param elevation 阴影高度
     * @param color 阴影颜色
     * @return Modifier
     */
    @Composable
    fun Modifier.customShadow(
        elevation: Dp = 8.dp,
        color: Color = ShadowColor
    ): Modifier {
        // 注意：这里使用简化的阴影效果
        // 在实际项目中可能需要使用第三方库或自定义绘制
        return this.background(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    color
                ),
                radius = elevation.value * 2
            )
        )
    }
}

/**
 * 预定义的玻璃态样式
 */
object GlassStyles {
    
    /**
     * 主容器样式
     */
    @Composable
    fun Modifier.mainContainer(): Modifier {
        return with(GlassmorphismEffects) {
            this@mainContainer.gradientBackground(
                colors = listOf(
                    TimerGrayDark,
                    Color.Black
                )
            )
        }
    }
    
    /**
     * 计时器圆环样式
     */
    @Composable
    fun Modifier.timerRing(): Modifier {
        return with(GlassmorphismEffects) {
            this@timerRing.glassmorphism(
                shape = CircleShape,
                borderWidth = 2.dp
            )
        }
    }
    
    /**
     * 控制按钮样式
     */
    @Composable
    fun Modifier.controlButton(isActive: Boolean = false): Modifier {
        return with(GlassmorphismEffects) {
            this@controlButton.glassButton(isPressed = isActive)
                .glowBorder(
                    color = if (isActive) TimerGreen else GlassBorder,
                    width = if (isActive) 3.dp else 1.dp
                )
        }
    }
    
    /**
     * 时间显示样式
     */
    @Composable
    fun Modifier.timeDisplay(): Modifier {
        return with(GlassmorphismEffects) {
            this@timeDisplay.glassmorphism(
                shape = RoundedCornerShape(12.dp),
                borderWidth = 1.dp
            )
        }
    }
}