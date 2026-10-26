package com.grid.griduko.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.griduko.audio.SoundManager
import com.grid.griduko.data.GameMechanics
import com.grid.griduko.data.GameStorage
import com.grid.griduko.data.TicTacToeSavedState
import com.grid.griduko.ui.components.GridukoTopBar
import com.grid.griduko.ui.components.ScoreStatCard
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
import kotlinx.coroutines.delay

@Composable
fun TicTacToeScreen(
    initialState: TicTacToeSavedState,
    storage: GameStorage,
    soundManager: SoundManager,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var board by remember { mutableStateOf(initialState.board) }
    var currentTurn by remember { mutableStateOf(initialState.currentTurn) }
    var vsCpu by remember { mutableStateOf(initialState.vsCpu) }
    var scoreX by remember { mutableIntStateOf(initialState.scoreX) }
    var draws by remember { mutableIntStateOf(initialState.draws) }
    var scoreO by remember { mutableIntStateOf(initialState.scoreO) }
    var winner by remember { mutableStateOf(initialState.winner) }
    var winningLine by remember { mutableStateOf(initialState.winningLine) }

    fun persist(
        newBoard: List<String> = board,
        newTurn: String = currentTurn,
        newVsCpu: Boolean = vsCpu,
        newScoreX: Int = scoreX,
        newDraws: Int = draws,
        newScoreO: Int = scoreO,
        newWinner: String? = winner,
        newWinningLine: List<Int> = winningLine
    ) {
        storage.saveTicTacToeState(
            TicTacToeSavedState(
                board = newBoard,
                currentTurn = newTurn,
                vsCpu = newVsCpu,
                scoreX = newScoreX,
                draws = newDraws,
                scoreO = newScoreO,
                winner = newWinner,
                winningLine = newWinningLine
            )
        )
    }

    fun resetBoardOnly() {
        val clean = List(9) { "" }
        board = clean
        currentTurn = "X"
        winner = null
        winningLine = emptyList()
        soundManager.playClick()
        persist(
            newBoard = clean,
            newTurn = "X",
            newWinner = null,
            newWinningLine = emptyList()
        )
    }

    fun applyMove(index: Int, symbol: String) {
        if (board[index].isNotEmpty() || winner != null) return
        val updatedBoard = board.toMutableList().also { it[index] = symbol }
        val (winResult, winIndices) = GameMechanics.checkTicTacToeWinner(updatedBoard)

        var nextScoreX = scoreX
        var nextDraws = draws
        var nextScoreO = scoreO

        if (symbol == "X") soundManager.playPlaceX() else soundManager.playPlaceO()
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

        when (winResult) {
            "X" -> {
                nextScoreX += 1
                scoreX = nextScoreX
                soundManager.playWin()
            }
            "O" -> {
                nextScoreO += 1
                scoreO = nextScoreO
                if (vsCpu) soundManager.playGameOver() else soundManager.playWin()
            }
            "DRAW" -> {
                nextDraws += 1
                draws = nextDraws
                soundManager.playGameOver()
            }
        }

        val nextTurn = if (symbol == "X") "O" else "X"
        board = updatedBoard
        currentTurn = nextTurn
        winner = winResult
        winningLine = winIndices

        persist(
            newBoard = updatedBoard,
            newTurn = nextTurn,
            newScoreX = nextScoreX,
            newDraws = nextDraws,
            newScoreO = nextScoreO,
            newWinner = winResult,
            newWinningLine = winIndices
        )
    }

    // Turno automático de la CPU cuando está activo "vs CPU" y es el turno de O
    LaunchedEffect(currentTurn, vsCpu, winner, board) {
        if (vsCpu && currentTurn == "O" && winner == null && board.any { it.isEmpty() }) {
            delay(360)
            val cpuMoveIndex = GameMechanics.computeBestCpuMove(board, cpuSymbol = "O", humanSymbol = "X")
            if (cpuMoveIndex in 0..8) {
                applyMove(cpuMoveIndex, "O")
            }
        }
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
                title = "Tres en raya",
                onBackClick = {
                    soundManager.playClick()
                    persist()
                    onBack()
                },
                rightActionSymbol = "↻",
                onRightActionClick = { resetBoardOnly() }
            )

            // Tres tarjetas de marcador: JUGADOR X | EMPATES | JUGADOR O
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ScoreStatCard(
                    label = "JUGADOR X",
                    value = scoreX.toString(),
                    valueColor = AccentYellow,
                    modifier = Modifier.weight(1f)
                )
                ScoreStatCard(
                    label = "EMPATES",
                    value = draws.toString(),
                    valueColor = TextWhite,
                    modifier = Modifier.weight(1f)
                )
                ScoreStatCard(
                    label = if (vsCpu) "CPU (O)" else "JUGADOR O",
                    value = scoreO.toString(),
                    valueColor = LightBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Píldora indicadora de turno o resultado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(NavySurface)
                    .border(1.dp, NavyBorder, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                val statusText = buildAnnotatedString {
                    when (winner) {
                        "X" -> {
                            append("¡Ganador: ")
                            withStyle(SpanStyle(color = AccentYellow, fontWeight = FontWeight.ExtraBold)) {
                                append("Jugador X")
                            }
                            append("!")
                        }
                        "O" -> {
                            append("¡Ganador: ")
                            withStyle(SpanStyle(color = LightBlue, fontWeight = FontWeight.ExtraBold)) {
                                append(if (vsCpu) "CPU (O)" else "Jugador O")
                            }
                            append("!")
                        }
                        "DRAW" -> {
                            withStyle(SpanStyle(color = TextWhite, fontWeight = FontWeight.ExtraBold)) {
                                append("¡Partida empatada!")
                            }
                        }
                        else -> {
                            append("Turno de ")
                            val turnColor = if (currentTurn == "X") AccentYellow else LightBlue
                            withStyle(SpanStyle(color = turnColor, fontWeight = FontWeight.ExtraBold)) {
                                append(currentTurn)
                            }
                        }
                    }
                }
                Text(
                    text = statusText,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tablero 3x3 de Tres en Raya
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .aspectRatio(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (row in 0..2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (col in 0..2) {
                            val index = row * 3 + col
                            val cellValue = board[index]
                            val isWinningCell = index in winningLine
                            TicTacToeCell(
                                symbol = cellValue,
                                isWinningCell = isWinningCell,
                                onClick = {
                                    if (winner == null && cellValue.isEmpty()) {
                                        if (!vsCpu || currentTurn == "X") {
                                            applyMove(index, currentTurn)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // Controles inferiores: Selector "vs CPU | 2 jugadores" + Botón "Reiniciar partida"
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Segmented Toggle: vs CPU / 2 jugadores
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NavySurface)
                    .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (vsCpu) NavySurfaceLight else NavySurface)
                        .border(
                            width = if (vsCpu) 1.dp else 0.dp,
                            color = if (vsCpu) LightBlue else NavySurface,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            if (!vsCpu) {
                                vsCpu = true
                                resetBoardOnly()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "vs CPU",
                        color = if (vsCpu) TextWhite else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = if (vsCpu) FontWeight.Bold else FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!vsCpu) NavySurfaceLight else NavySurface)
                        .border(
                            width = if (!vsCpu) 1.dp else 0.dp,
                            color = if (!vsCpu) LightBlue else NavySurface,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            if (vsCpu) {
                                vsCpu = false
                                resetBoardOnly()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "2 jugadores",
                        color = if (!vsCpu) TextWhite else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = if (!vsCpu) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { resetBoardOnly() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentYellow,
                    contentColor = TextDarkNavy
                )
            ) {
                Text(
                    text = "Reiniciar partida",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun TicTacToeCell(
    symbol: String,
    isWinningCell: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = when {
            isWinningCell -> 1.05f
            symbol.isNotEmpty() -> 1f
            else -> 0.98f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ttt_cell_scale"
    )

    val borderColor = when {
        isWinningCell -> SnakeGreen
        symbol == "X" -> AccentYellow
        symbol == "O" -> LightBlue
        else -> NavyBorder
    }

    val symbolColor = if (symbol == "X") AccentYellow else LightBlue

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isWinningCell) NavySurfaceLight else NavySurface)
            .border(
                width = if (symbol.isNotEmpty() || isWinningCell) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (symbol.isNotEmpty()) {
            Text(
                text = symbol,
                color = symbolColor,
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
