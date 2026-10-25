package com.grid.griduko.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.griduko.audio.SoundManager
import com.grid.griduko.ui.components.GridukoLogo
import com.grid.griduko.ui.theme.AccentYellow
import com.grid.griduko.ui.theme.LightBlue
import com.grid.griduko.ui.theme.NavyBackground
import com.grid.griduko.ui.theme.NavyBorder
import com.grid.griduko.ui.theme.NavySurface
import com.grid.griduko.ui.theme.NavySurfaceLight
import com.grid.griduko.ui.theme.SnakeGreen
import com.grid.griduko.ui.theme.TextDarkNavy
import com.grid.griduko.ui.theme.TextMuted
import com.grid.griduko.ui.theme.TextWhite

@Composable
fun WelcomeScreen(
    snakeHighScore: Int,
    game2048HighScore: Int,
    ticTacToeSummary: String,
    rainbowModeUnlocked: Boolean,
    soundManager: SoundManager,
    onSelectSnake: () -> Unit,
    onSelect2048: () -> Unit,
    onSelectTicTacToe: () -> Unit,
    onOpenCredits: () -> Unit,
    onUnlockLogoEasterEgg: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var logoTapCount by remember { mutableIntStateOf(0) }
    var secretBannerVisible by remember { mutableStateOf(rainbowModeUnlocked) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 22.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Encabezado con Logo y Títulos
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            GridukoLogo(
                size = 132.dp,
                rainbowMode = secretBannerVisible || rainbowModeUnlocked,
                animatePulse = true,
                onLogoClick = {
                    logoTapCount++
                    soundManager.playClick()
                    if (logoTapCount >= 7) {
                        logoTapCount = 0
                        secretBannerVisible = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        soundManager.playEasterEgg()
                        onUnlockLogoEasterEgg()
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Griduko",
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Elige un juego para empezar",
                color = TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            AnimatedVisibility(
                visible = secretBannerVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(NavySurfaceLight)
                        .border(1.dp, AccentYellow, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "★ Modo Arcade Secreto Desbloqueado ★",
                        color = AccentYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Lista de tarjetas de juegos
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "ELIGE TU JUEGO",
                color = AccentYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )

            GameMenuCard(
                badgeText = "S",
                badgeColor = SnakeGreen,
                badgeFontSize = 26,
                title = "Snake",
                subtitle = if (snakeHighScore > 0) {
                    "Come, crece y no choques · Récord: $snakeHighScore"
                } else {
                    "Come, crece y no choques"
                },
                onClick = {
                    soundManager.playClick()
                    onSelectSnake()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            GameMenuCard(
                badgeText = "2048",
                badgeColor = AccentYellow,
                badgeFontSize = 14,
                title = "2048",
                subtitle = if (game2048HighScore > 0) {
                    "Une números hasta el 2048 · Récord: $game2048HighScore"
                } else {
                    "Une números hasta el 2048"
                },
                onClick = {
                    soundManager.playClick()
                    onSelect2048()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            GameMenuCard(
                badgeText = "#",
                badgeColor = LightBlue,
                badgeFontSize = 24,
                title = "Tres en raya",
                subtitle = if (ticTacToeSummary.isNotEmpty()) {
                    "Gana en línea: X contra O · $ticTacToeSummary"
                } else {
                    "Gana en línea: X contra O"
                },
                onClick = {
                    soundManager.playClick()
                    onSelectTicTacToe()
                }
            )
        }

        // Pie de página: Créditos · v1.0
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    soundManager.playClick()
                    onOpenCredits()
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Créditos · v1.0",
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun GameMenuCard(
    badgeText: String,
    badgeColor: Color,
    badgeFontSize: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        label = "card_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(NavySurface)
            .border(1.dp, NavyBorder, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono cuadrado izquierdo
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badgeText,
                color = TextDarkNavy,
                fontSize = badgeFontSize.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Botón redondo con chevron '>'
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(NavySurfaceLight)
                .border(1.dp, NavyBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "›",
                color = LightBlue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
