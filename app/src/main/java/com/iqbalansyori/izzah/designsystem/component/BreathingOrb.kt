package com.iqbalansyori.izzah.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors

@Composable
fun BreathingOrb(inhale: Boolean, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(if (inhale) 1f else .62f, tween(4000, easing = FastOutSlowInEasing), label = "breath")
    Box(modifier.size(280.dp)) {
        Box(
            Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale }.clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF6B8FDC).copy(alpha = .45f), IzzahColors.Bg.copy(alpha = 0f)))),
        )
        Box(
            Modifier.fillMaxSize().padding(20.dp).graphicsLayer { scaleX = scale; scaleY = scale }.clip(CircleShape)
                .background(Brush.linearGradient(listOf(IzzahColors.Lavender, IzzahColors.Blue))),
        )
    }
}
