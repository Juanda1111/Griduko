package com.grid.griduko.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.griduko.ui.theme.AccentYellow
import com.grid.griduko.ui.theme.CoralPink
import com.grid.griduko.ui.theme.LightBlue
import com.grid.griduko.ui.theme.NavyBorder
import com.grid.griduko.ui.theme.NavySurface
import com.grid.griduko.ui.theme.SnakeGreen
import com.grid.griduko.ui.theme.TextDarkNavy
import com.grid.griduko.ui.theme.TextMuted
import com.grid.griduko.ui.theme.TextWhite

/**
 * Logotipo oficial de Griduko fiel al wireframe:
 * - Insignia superior "2048" amarilla
 * - Serpiente blanca envolviendo el tablero con cabeza y ojos
 * - Tablero de Tres en Raya interior con O - X - O
 */
@Composable
fun GridukoLogo(
    modifier: Modifier = Modifier,
    size: Dp = 116.dp,
    rainbowMode: Boolean = false,
    animatePulse: Boolean = false,
    onLogoClick: (() -> Unit)? = null
) {
    val textMeasurer = rememberTextMeasurer()
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

    Box(
        modifier = modifier
            .size(size)
            .scale(pulseScale)
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val strokeWidth = w * 0.068f
            val gridStrokeWidth = w * 0.032f

            val snakeColor = if (rainbowMode) SnakeGreen else TextWhite
            val gridColor = if (rainbowMode) LightBlue else TextWhite

            // 1. Cuerpo de la serpiente envolviendo la cuadrícula
            val snakePath = Path().apply {
                // Comienza cerca del borde izquierdo del badge 2048
                moveTo(w * 0.27f, h * 0.14f)
                lineTo(w * 0.22f, h * 0.14f)
                arcTo(
                    rect = Rect(w * 0.10f, h * 0.14f, w * 0.30f, h * 0.34f),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo = false
                )
                // Baja por la izquierda hasta abajo
                lineTo(w * 0.10f, h * 0.78f)
                arcTo(
                    rect = Rect(w * 0.10f, h * 0.68f, w * 0.30f, h * 0.88f),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo = false
                )
                // Base inferior hacia la derecha (con ligera cola que sobresale)
                lineTo(w * 0.84f, h * 0.88f)
            }

            drawPath(
                path = snakePath,
                color = snakeColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Parte superior derecha de la serpiente que baja y forma el cuello + cabeza
            val headLoopPath = Path().apply {
                moveTo(w * 0.73f, h * 0.14f)
                lineTo(w * 0.78f, h * 0.14f)
                arcTo(
                    rect = Rect(w * 0.70f, h * 0.14f, w * 0.90f, h * 0.32f),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                arcTo(
                    rect = Rect(w * 0.70f, h * 0.18f, w * 0.90f, h * 0.36f),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                // Cuello que cruza hacia la izquierda y da vuelta a la cabeza
                lineTo(w * 0.24f, h * 0.36f)
            }

            drawPath(
                path = headLoopPath,
                color = snakeColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Cabeza de la serpiente mirando hacia la derecha (arriba a la derecha del tablero)
            val headCenter = Offset(w * 0.83f, h * 0.36f)
            drawRoundRect(
                color = snakeColor,
                topLeft = Offset(w * 0.74f, h * 0.305f),
                size = Size(w * 0.17f, h * 0.11f),
                cornerRadius = CornerRadius(w * 0.055f, w * 0.055f)
            )
            // Ojo de la serpiente
            drawCircle(
                color = TextDarkNavy,
                radius = w * 0.018f,
                center = Offset(w * 0.85f, h * 0.36f)
            )
            // Lengua pequeña
            drawLine(
                color = if (rainbowMode) CoralPink else TextWhite,
                start = Offset(w * 0.91f, h * 0.36f),
                end = Offset(w * 0.95f, h * 0.34f),
                strokeWidth = w * 0.016f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = if (rainbowMode) CoralPink else TextWhite,
                start = Offset(w * 0.91f, h * 0.36f),
                end = Offset(w * 0.95f, h * 0.38f),
                strokeWidth = w * 0.016f,
                cap = StrokeCap.Round
            )

            // 2. Cuadrícula 3x3 de Tres en Raya en el centro-inferior
            val gridLeft = w * 0.20f
            val gridRight = w * 0.80f
            val gridTop = h * 0.42f
            val gridBottom = h * 0.82f
            val cellW = (gridRight - gridLeft) / 3f
            val cellH = (gridBottom - gridTop) / 3f

            // Líneas verticales
            drawLine(
                color = gridColor,
                start = Offset(gridLeft + cellW, gridTop),
                end = Offset(gridLeft + cellW, gridBottom),
                strokeWidth = gridStrokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = gridColor,
                start = Offset(gridLeft + cellW * 2f, gridTop),
                end = Offset(gridLeft + cellW * 2f, gridBottom),
                strokeWidth = gridStrokeWidth,
                cap = StrokeCap.Round
            )
            // Líneas horizontales
            drawLine(
                color = gridColor,
                start = Offset(gridLeft, gridTop + cellH),
                end = Offset(gridRight, gridTop + cellH),
                strokeWidth = gridStrokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = gridColor,
                start = Offset(gridLeft, gridTop + cellH * 2f),
                end = Offset(gridRight, gridTop + cellH * 2f),
                strokeWidth = gridStrokeWidth,
                cap = StrokeCap.Round
            )

            // Símbolo O en celda (0, 0)
            drawCircle(
                color = gridColor,
                radius = cellW * 0.26f,
                center = Offset(gridLeft + cellW * 0.5f, gridTop + cellH * 0.5f),
                style = Stroke(width = gridStrokeWidth)
            )

            // Símbolo X en celda (1, 1)
            val cx = gridLeft + cellW * 1.5f
            val cy = gridTop + cellH * 1.5f
            val xr = cellW * 0.22f
            drawLine(
                color = if (rainbowMode) AccentYellow else gridColor,
                start = Offset(cx - xr, cy - xr),
                end = Offset(cx + xr, cy + xr),
                strokeWidth = gridStrokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = if (rainbowMode) AccentYellow else gridColor,
                start = Offset(cx + xr, cy - xr),
                end = Offset(cx - xr, cy + xr),
                strokeWidth = gridStrokeWidth,
                cap = StrokeCap.Round
            )

            // Símbolo O en celda (2, 2)
            drawCircle(
                color = gridColor,
                radius = cellW * 0.26f,
                center = Offset(gridLeft + cellW * 2.5f, gridTop + cellH * 2.5f),
                style = Stroke(width = gridStrokeWidth)
            )

            // 3. Insignia superior "2048" amarilla
            val badgeWidth = w * 0.46f
            val badgeHeight = h * 0.22f
            val badgeLeft = (w - badgeWidth) / 2f
            val badgeTop = h * 0.04f

            drawRoundRect(
                color = AccentYellow,
                topLeft = Offset(badgeLeft, badgeTop),
                size = Size(badgeWidth, badgeHeight),
                cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
            )

            val textLayout = textMeasurer.measure(
                text = "2048",
                style = TextStyle(
                    color = TextDarkNavy,
                    fontSize = (w * 0.115f).sp,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(
                    x = badgeLeft + (badgeWidth - textLayout.size.width) / 2f,
                    y = badgeTop + (badgeHeight - textLayout.size.height) / 2f
                )
            )
        }
    }
}

/**
 * Barra superior común para las pantallas de juego y créditos (Botón atrás, Título, Acción derecha opcional)
 */
@Composable
fun GridukoTopBar(
    title: String,
    onBackClick: () -> Unit,
    rightActionSymbol: String? = null,
    onRightActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CircleActionButton(
            symbol = "←",
            onClick = onBackClick
        )

        Text(
            text = title,
            color = TextWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        if (rightActionSymbol != null && onRightActionClick != null) {
            CircleActionButton(
                symbol = rightActionSymbol,
                onClick = onRightActionClick
            )
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
fun CircleActionButton(
    symbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(NavySurface)
            .border(1.dp, NavyBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = LightBlue,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
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
