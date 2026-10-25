package com.grid.griduko.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.griduko.ui.components.GridukoLogo
import com.grid.griduko.ui.theme.AccentYellow
import com.grid.griduko.ui.theme.NavyBackground
import com.grid.griduko.ui.theme.NavyBorder
import com.grid.griduko.ui.theme.NavySurface
import com.grid.griduko.ui.theme.TextMuted
import com.grid.griduko.ui.theme.TextWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val progress = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.75f) }

    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1900, easing = FastOutSlowInEasing)
        )
        delay(150)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 32.dp, vertical = 40.dp)
    ) {
        // Centro: Círculo grande con logo + título + subtítulo
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .scale(logoScale.value)
                    .clip(CircleShape)
                    .background(NavySurface)
                    .border(1.5.dp, NavyBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                GridukoLogo(
                    size = 136.dp,
                    animatePulse = true
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Griduko",
                color = TextWhite,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Snake · 2048 · Tres en raya",
                color = TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Parte inferior: Barra de carga amarilla + "Cargando..."
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(NavySurface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value.coerceIn(0.05f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(AccentYellow)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Cargando...",
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
