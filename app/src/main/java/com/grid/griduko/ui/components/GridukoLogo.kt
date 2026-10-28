package com.grid.griduko.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.griduko.R
import com.grid.griduko.ui.theme.AccentYellow
import com.grid.griduko.ui.theme.NavyBorder
import com.grid.griduko.ui.theme.NavySurface
import com.grid.griduko.ui.theme.TextMuted

/**
 * Muestra el logotipo oficial de Griduko a partir del recurso drawable proporcionado.
 */
@Composable
fun GridukoLogo(
    modifier: Modifier = Modifier,
    size: Dp = 116.dp,
    rainbowMode: Boolean = false,
    animatePulse: Boolean = false,
    onLogoClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (animatePulse) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val clickModifier = if (onLogoClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onLogoClick
        )
    } else {
        Modifier
    }

    val cornerShape = RoundedCornerShape((size.value * 0.18f).dp)
    val rainbowBorderModifier = if (rainbowMode) {
        Modifier.border(2.dp, AccentYellow, cornerShape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(pulseScale)
            .clip(cornerShape)
            .background(Color(0xFF062252))
            .then(rainbowBorderModifier)
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_griduko),
            contentDescription = "Logo de Griduko",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Tarjeta de puntaje reutilizable (Puntos / Récord / Marcadores)
 */
@Composable
fun ScoreStatCard(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(NavySurface)
            .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(),
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
