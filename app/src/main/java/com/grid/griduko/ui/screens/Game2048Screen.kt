package com.grid.griduko.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.griduko.audio.SoundManager
import com.grid.griduko.data.Game2048SavedState
import com.grid.griduko.data.GameMechanics
import com.grid.griduko.data.GameStorage
import com.grid.griduko.data.SnakeDirection
import com.grid.griduko.ui.components.GridukoTopBar
import com.grid.griduko.ui.components.ScoreStatCard
import com.grid.griduko.ui.theme.AccentYellow
import com.grid.griduko.ui.theme.CoralPink
import com.grid.griduko.ui.theme.LightBlue
import com.grid.griduko.ui.theme.LightWhiteTile
import com.grid.griduko.ui.theme.NavyBackground
import com.grid.griduko.ui.theme.NavyBoardBackground
import com.grid.griduko.ui.theme.NavyBorder
import com.grid.griduko.ui.theme.NavySurface
import com.grid.griduko.ui.theme.SnakeGreen
import com.grid.griduko.ui.theme.SoftBlueTile
import com.grid.griduko.ui.theme.TextDarkNavy
import com.grid.griduko.ui.theme.TextMuted
import com.grid.griduko.ui.theme.TextWhite
import com.grid.griduko.ui.theme.TileOrange
import com.grid.griduko.ui.theme.TilePurple
import com.grid.griduko.ui.theme.TileRed
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@Composable
fun Game2048Screen(
    initialState: Game2048SavedState,
    storage: GameStorage,
    soundManager: SoundManager,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val numberFormatter = remember { NumberFormat.getIntegerInstance(Locale.US) }

    var board by remember { mutableStateOf(initialState.board) }
    var previousBoard by remember { mutableStateOf(initialState.previousBoard) }
    var score by remember { mutableIntStateOf(initialState.score) }
    var previousScore by remember { mutableIntStateOf(initialState.previousScore) }
    var highScore by remember { mutableIntStateOf(initialState.highScore) }
    var isGameOver by remember { mutableStateOf(initialState.isGameOver) }

    fun persist(
        newBoard: List<Int> = board,
        newPrevBoard: List<Int>? = previousBoard,
        newScore: Int = score,
        newPrevScore: Int = previousScore,
        newHighScore: Int = highScore,
        newGameOver: Boolean = isGameOver
    ) {
        storage.save2048State(
            Game2048SavedState(
                board = newBoard,
                previousBoard = newPrevBoard,
                score = newScore,
                previousScore = newPrevScore,
                highScore = newHighScore,
                isGameOver = newGameOver
            )
        )
    }

    fun startNewGame() {
        var fresh = List(16) { 0 }
        fresh = GameMechanics.spawnRandomTile(fresh)
        fresh = GameMechanics.spawnRandomTile(fresh)
        board = fresh
        previousBoard = null
        score = 0
        previousScore = 0
        isGameOver = false
        soundManager.playClick()
        persist(
            newBoard = fresh,
            newPrevBoard = null,
            newScore = 0,
            newPrevScore = 0,
            newHighScore = highScore,
            newGameOver = false
        )
    }

    fun undoLastMove() {
        val prev = previousBoard ?: return
        board = prev
        score = previousScore
        previousBoard = null
        isGameOver = false
        soundManager.playClick()
        persist(
            newBoard = prev,
            newPrevBoard = null,
            newScore = previousScore,
            newPrevScore = previousScore,
            newHighScore = highScore,
            newGameOver = false
        )
    }

    fun handleSwipe(direction: SnakeDirection) {
        if (isGameOver) return
        val result = GameMechanics.move2048(board, direction)
        if (!result.moved) return

        val savedPrevBoard = board
        val savedPrevScore = score

        val withNewTile = GameMechanics.spawnRandomTile(result.newBoard)
        val updatedScore = score + result.pointsEarned
        val updatedHigh = maxOf(highScore, updatedScore)
        val gameOverNow = !GameMechanics.canMove2048(withNewTile)

        board = withNewTile
        previousBoard = savedPrevBoard
        score = updatedScore
        previousScore = savedPrevScore
        highScore = updatedHigh
        isGameOver = gameOverNow

        if (result.maxMergedTile >= 2048) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            soundManager.playWin()
        } else if (result.maxMergedTile > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            soundManager.playMerge(result.maxMergedTile)
        } else {
            soundManager.playMove()
        }

        if (gameOverNow) {
            soundManager.playGameOver()
        }

        persist(
            newBoard = withNewTile,
            newPrevBoard = savedPrevBoard,
            newScore = updatedScore,
            newPrevScore = savedPrevScore,
            newHighScore = updatedHigh,
            newGameOver = gameOverNow
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GridukoTopBar(
                title = "2048",
                onBackClick = {
                    soundManager.playClick()
                    persist()
                    onBack()
                },
                rightActionSymbol = "↻",
                onRightActionClick = { startNewGame() }
            )

            // Tarjetas PUNTOS y RÉCORD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScoreStatCard(
                    label = "PUNTOS",
                    value = numberFormatter.format(score),
                    valueColor = AccentYellow,
                    modifier = Modifier.weight(1f)
                )
                ScoreStatCard(
                    label = "RÉCORD",
                    value = numberFormatter.format(highScore),
                    valueColor = TextWhite,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tablero 4x4 de 2048
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(NavyBoardBackground)
                    .border(1.5.dp, NavyBorder, RoundedCornerShape(22.dp))
                    .pointerInput(board, isGameOver) {
                        var dragX = 0f
                        var dragY = 0f
                        detectDragGestures(
                            onDragStart = {
                                dragX = 0f
                                dragY = 0f
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                dragX += amount.x
                                dragY += amount.y
                            },
                            onDragEnd = {
                                val minSwipe = 28f
                                if (abs(dragX) > abs(dragY)) {
                                    if (abs(dragX) > minSwipe) {
                                        handleSwipe(if (dragX > 0) SnakeDirection.RIGHT else SnakeDirection.LEFT)
                                    }
                                } else {
                                    if (abs(dragY) > minSwipe) {
                                        handleSwipe(if (dragY > 0) SnakeDirection.DOWN else SnakeDirection.UP)
                                    }
                                }
                            }
                        )
                    }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (row in 0..3) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (col in 0..3) {
                                val value = board[row * 4 + col]
                                Tile2048Cell(
                                    value = value,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                )
                            }
                        }
                    }
                }

                if (isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(NavyBackground.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "¡Sin movimientos!",
                                color = TextWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Puntaje final: ${numberFormatter.format(score)}",
                                color = AccentYellow,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { startNewGame() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentYellow,
                                    contentColor = TextDarkNavy
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Intentar de nuevo", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Botones Nuevo Juego y Deshacer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { startNewGame() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentYellow,
                        contentColor = TextDarkNavy
                    )
                ) {
                    Text(
                        text = "Nuevo juego",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                OutlinedButton(
                    onClick = { undoLastMove() },
                    enabled = previousBoard != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.5.dp,
                        if (previousBoard != null) LightBlue else NavyBorder
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = NavySurface,
                        contentColor = LightBlue,
                        disabledContainerColor = NavySurface.copy(alpha = 0.5f),
                        disabledContentColor = TextMuted.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "Deshacer",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            text = "Desliza en cualquier dirección para mover las fichas",
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun Tile2048Cell(
    value: Int,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (value > 0) 1f else 0.96f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tile_scale"
    )

    val (bgColor, textColor) = when (value) {
        0 -> NavySurface to Color.Transparent
        2 -> LightWhiteTile to TextDarkNavy
        4 -> SoftBlueTile to TextDarkNavy
        8 -> AccentYellow to TextDarkNavy
        16 -> TileOrange to TextDarkNavy
        32 -> CoralPink to TextDarkNavy
        64 -> TileRed to TextWhite
        128 -> SnakeGreen to TextDarkNavy
        256 -> TilePurple to TextWhite
        512 -> LightBlue to TextDarkNavy
        1024 -> AccentYellow to TextDarkNavy
        else -> SnakeGreen to TextWhite
    }

    val fontSize = when {
        value < 100 -> 24.sp
        value < 1000 -> 20.sp
        else -> 16.sp
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (value > 0) {
            Text(
                text = value.toString(),
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
