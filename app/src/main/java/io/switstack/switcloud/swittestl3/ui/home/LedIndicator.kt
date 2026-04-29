package io.switstack.switcloud.swittestl3.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.switstack.switcloud.swittestl3.data.LedState

@Composable
fun LedIndicator(
    state: LedState,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    borderColor: Color = Color.DarkGray,
    borderWidth: Dp = 1.dp,
    blinkingDurationMillis: Long = 200L,
    intervalMillis: Long = 5000L
) {
//    val greenColor = Color(0xFF0AA3AA)
    val greenColor = Color.Green
    val greyColor = Color.LightGray

    val baseColor = when (state) {
        LedState.GREY -> greyColor
        LedState.GREEN, LedState.BLINKING_GREEN -> greenColor // Base color for green states
    }

    // --- Blinking Animation Logic ---
    val infiniteTransition = rememberInfiniteTransition(label = "LedHardBlinkAnimation")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f, // These are overridden by KeyframesSpec
        targetValue = 0f, // These are overridden by KeyframesSpec
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = intervalMillis.toInt()
                0f at 0
                1f at 1
                1f at blinkingDurationMillis.toInt()
                0f at blinkingDurationMillis.toInt()
                0f at intervalMillis.toInt()
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "AlphaHardBlink"
    )

    val fillColor = if (state == LedState.BLINKING_GREEN) {
        greenColor.copy(alpha = alpha)
    } else {
        baseColor
    }

    Box(
        modifier = modifier
            .size(size)
            .background(color = greyColor, shape = CircleShape)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .border(width = borderWidth, color = borderColor, shape = CircleShape)
                // Apply the dynamic fill color
                .background(color = fillColor, shape = CircleShape)
        )
    }
}