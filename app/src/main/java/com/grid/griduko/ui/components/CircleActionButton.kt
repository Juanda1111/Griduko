package com.grid.griduko.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.griduko.ui.theme.LightBlue
import com.grid.griduko.ui.theme.NavyBorder
import com.grid.griduko.ui.theme.NavySurface

/**
 * Botón circular para la barra superior con iconos vectoriales centrados geométricamente
 * (corrige el desplazamiento vertical que tenían los caracteres Unicode de fuente).
 */
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
        when (symbol) {
            "←" -> {
                Canvas(modifier = Modifier.size(18.dp)) {
                    val w = size.width
                    val h = size.height
                    val cy = h / 2f
                    val strokeW = 2.2.dp.toPx()

                    // Línea horizontal perfectamente centrada
                    drawLine(
                        color = LightBlue,
                        start = Offset(w * 0.16f, cy),
                        end = Offset(w * 0.84f, cy),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )

                    // Punta de la flecha izquierda centrada respecto a cy
                    val arrowHead = Path().apply {
                        moveTo(w * 0.46f, h * 0.22f)
                        lineTo(w * 0.16f, cy)
                        lineTo(w * 0.46f, h * 0.78f)
                    }
                    drawPath(
                        path = arrowHead,
                        color = LightBlue,
                        style = Stroke(
                            width = strokeW,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            "||" -> {
                Canvas(modifier = Modifier.size(16.dp)) {
                    val w = size.width
                    val h = size.height
                    val barW = w * 0.20f
                    val barH = h * 0.72f
                    val topY = (h - barH) / 2f

                    drawRoundRect(
                        color = LightBlue,
                        topLeft = Offset(w * 0.22f, topY),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                    )
                    drawRoundRect(
                        color = LightBlue,
                        topLeft = Offset(w * 0.58f, topY),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                    )
                }
            }

            "▶" -> {
                Canvas(modifier = Modifier.size(16.dp)) {
                    val w = size.width
                    val h = size.height
                    val playPath = Path().apply {
                        moveTo(w * 0.26f, h * 0.18f)
                        lineTo(w * 0.82f, h * 0.50f)
                        lineTo(w * 0.26f, h * 0.82f)
                        close()
                    }
                    drawPath(path = playPath, color = LightBlue)
                }
            }

            "↻" -> {
                Canvas(modifier = Modifier.size(18.dp)) {
                    val w = size.width
                    val h = size.height
                    val strokeW = 2.1.dp.toPx()
                    val pad = w * 0.14f

                    drawArc(
                        color = LightBlue,
                        startAngle = -35f,
                        sweepAngle = 290f,
                        useCenter = false,
                        topLeft = Offset(pad, pad),
                        size = Size(w - pad * 2f, h - pad * 2f),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )

                    val tip = Path().apply {
                        moveTo(w * 0.62f, h * 0.12f)
                        lineTo(w * 0.84f, h * 0.26f)
                        lineTo(w * 0.64f, h * 0.38f)
                    }
                    drawPath(
                        path = tip,
                        color = LightBlue,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            else -> {
                Text(
                    text = symbol,
                    color = LightBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
