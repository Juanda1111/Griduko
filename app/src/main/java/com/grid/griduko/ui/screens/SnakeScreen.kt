package com.grid.griduko.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.griduko.audio.SoundManager
import com.grid.griduko.data.GameStorage
import com.grid.griduko.data.GridPoint
import com.grid.griduko.data.SnakeDirection
import com.grid.griduko.data.SnakeSavedState
import com.grid.griduko.ui.components.GridukoTopBar
import com.grid.griduko.ui.components.ScoreStatCard
import com.grid.griduko.ui.theme.AccentYellow
import com.grid.griduko.ui.theme.CoralPink
import com.grid.griduko.ui.theme.LightBlue
import com.grid.griduko.ui.theme.NavyBackground
import com.grid.griduko.ui.theme.NavyBoardBackground
import com.grid.griduko.ui.theme.NavyBorder
import com.grid.griduko.ui.theme.NavySubtleGrid
import com.grid.griduko.ui.theme.NavySurface
import com.grid.griduko.ui.theme.NavySurfaceLight
import com.grid.griduko.ui.theme.SnakeGreen
import com.grid.griduko.ui.theme.SnakeGreenDark
import com.grid.griduko.ui.theme.TextDarkNavy
import com.grid.griduko.ui.theme.TextMuted
import com.grid.griduko.ui.theme.TextWhite
import com.grid.griduko.ui.theme.TileOrange
import com.grid.griduko.ui.theme.TilePurple
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

private const val GRID_SIZE = 15

private val KONAMI_SEQUENCE = listOf(
    SnakeDirection.UP,
    SnakeDirection.UP,
    SnakeDirection.DOWN,
    SnakeDirection.DOWN,
    SnakeDirection.LEFT,
    SnakeDirection.RIGHT,
    SnakeDirection.LEFT,
    SnakeDirection.RIGHT
)

@Composable
fun SnakeScreen(
    initialState: SnakeSavedState,
    storage: GameStorage,
    soundManager: SoundManager,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Si la partida guardada terminó con 0 puntos (por el arranque automático anterior),
    // la preparamos limpia para que el jugador pueda iniciar cómodamente.
    val startClean = initialState.isGameOver && initialState.score == 0

    var snake by remember {
        mutableStateOf(if (startClean) storage.defaultSnakePoints() else initialState.snake)
    }
    var food by remember {
        mutableStateOf(if (startClean) GridPoint(11, 4) else initialState.food)
    }
    var direction by remember {
        mutableStateOf(if (startClean) SnakeDirection.RIGHT else initialState.direction)
    }
    var nextDirection by remember {
        mutableStateOf(if (startClean) SnakeDirection.RIGHT else initialState.direction)
    }
    var score by remember { mutableIntStateOf(if (startClean) 0 else initialState.score) }
    var highScore by remember { mutableIntStateOf(initialState.highScore) }

    // Al entrar a la pantalla, la serpiente espera el primer movimiento del jugador
    var waitingToStart by remember { mutableStateOf(true) }
    var isPaused by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(if (startClean) false else initialState.isGameOver) }
    var rainbowMode by remember { mutableStateOf(initialState.rainbowModeUnlocked) }
    var konamiBannerVisible by remember { mutableStateOf(false) }

    val inputHistory = remember { mutableStateListOf<SnakeDirection>() }

    fun persistCurrentState(pausedOverride: Boolean = isPaused) {
        storage.saveSnakeState(
            SnakeSavedState(
                snake = snake,
                food = food,
                direction = direction,
                score = score,
                highScore = maxOf(highScore, score),
                isPaused = pausedOverride,
                isGameOver = isGameOver,
                rainbowModeUnlocked = rainbowMode
            )
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            persistCurrentState(pausedOverride = true)
        }
    }

    fun spawnFood(currentSnake: List<GridPoint>): GridPoint {
        val available = mutableListOf<GridPoint>()
        for (x in 0 until GRID_SIZE) {
            for (y in 0 until GRID_SIZE) {
                val pt = GridPoint(x, y)
                if (pt !in currentSnake) available.add(pt)
            }
        }
        return if (available.isNotEmpty()) available[Random.nextInt(available.size)] else GridPoint(0, 0)
    }

    fun restartGame() {
        val defaultSnake = storage.defaultSnakePoints()
        snake = defaultSnake
        direction = SnakeDirection.RIGHT
        nextDirection = SnakeDirection.RIGHT
        food = GridPoint(11, 4)
        score = 0
        isGameOver = false
        isPaused = false
        waitingToStart = true
        persistCurrentState(pausedOverride = true)
    }

    fun handleDirectionInput(newDir: SnakeDirection) {
        // Registrar en historial para detectar el Easter Egg del Código Konami
        inputHistory.add(newDir)
        if (inputHistory.size > KONAMI_SEQUENCE.size) {
            inputHistory.removeAt(0)
        }
        if (inputHistory.toList() == KONAMI_SEQUENCE) {
            rainbowMode = true
            konamiBannerVisible = true
            score += 100
            if (score > highScore) highScore = score
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            soundManager.playEasterEgg()
            storage.registerEasterEggFound()
            inputHistory.clear()
        }

        if (isGameOver) return

        if (waitingToStart) {
            waitingToStart = false
            isPaused = false
            if (!newDir.isOpposite(direction)) {
                direction = newDir
                nextDirection = newDir
            }
            soundManager.playMove()
            return
        }

        if (isPaused) {
            isPaused = false
        }
        if (!newDir.isOpposite(direction)) {
            nextDirection = newDir
            soundManager.playMove()
        }
    }

    // Game Loop de Snake: solo avanza cuando el jugador ya inició (waitingToStart == false)
    LaunchedEffect(waitingToStart, isPaused, isGameOver, rainbowMode) {
        while (!waitingToStart && !isPaused && !isGameOver) {
            val speedMs = (195L - (score / 40) * 8L).coerceAtLeast(90L)
            delay(speedMs)

            direction = nextDirection
            val head = snake.first()
            val newHead = GridPoint(head.x + direction.dx, head.y + direction.dy)

            val hitWall = newHead.x !in 0 until GRID_SIZE || newHead.y !in 0 until GRID_SIZE
            val hitSelf = newHead in snake.dropLast(1)

            if (hitWall || hitSelf) {
                isGameOver = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                soundManager.playGameOver()
                persistCurrentState(pausedOverride = true)
            } else if (newHead == food) {
                val grown = listOf(newHead) + snake
                snake = grown
                score += 10
                if (score > highScore) highScore = score
                food = spawnFood(grown)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                soundManager.playEat()
                persistCurrentState()
            } else {
                snake = listOf(newHead) + snake.dropLast(1)
                persistCurrentState()
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "snake_food_pulse")
    val foodScale by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(550),
            repeatMode = RepeatMode.Reverse
        ),
        label = "food_pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GridukoTopBar(
                title = "Snake",
                onBackClick = {
                    soundManager.playClick()
                    persistCurrentState(pausedOverride = true)
                    onBack()
                },
                rightActionSymbol = if (isPaused || waitingToStart) "▶" else "||",
                onRightActionClick = {
                    soundManager.playClick()
                    when {
                        isGameOver -> restartGame()
                        waitingToStart -> {
                            waitingToStart = false
                            isPaused = false
                        }
                        else -> {
                            isPaused = !isPaused
                            persistCurrentState()
                        }
                    }
                }
            )

            // Tarjetas de PUNTOS y RÉCORD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScoreStatCard(
                    label = "PUNTOS",
                    value = score.toString(),
                    valueColor = SnakeGreen,
                    modifier = Modifier.weight(1f)
                )
                ScoreStatCard(
                    label = "RÉCORD",
                    value = highScore.toString(),
                    valueColor = AccentYellow,
                    modifier = Modifier.weight(1f)
                )
            }

            AnimatedVisibility(
                visible = konamiBannerVisible || rainbowMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "★ Easter Egg Konami Activo: Serpiente Arcoíris (+100 pts) ★",
                    color = AccentYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tablero de Snake
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(NavyBoardBackground)
                    .border(
                        width = 2.dp,
                        color = if (rainbowMode) AccentYellow else SnakeGreen,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .pointerInput(direction, waitingToStart, isPaused, isGameOver) {
                        var accumulatedX = 0f
                        var accumulatedY = 0f
                        detectDragGestures(
                            onDragStart = {
                                accumulatedX = 0f
                                accumulatedY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                accumulatedX += dragAmount.x
                                accumulatedY += dragAmount.y
                            },
                            onDragEnd = {
                                val threshold = 24f
                                if (abs(accumulatedX) > abs(accumulatedY)) {
                                    if (abs(accumulatedX) > threshold) {
                                        handleDirectionInput(
                                            if (accumulatedX > 0) SnakeDirection.RIGHT else SnakeDirection.LEFT
                                        )
                                    }
                                } else {
                                    if (abs(accumulatedY) > threshold) {
                                        handleDirectionInput(
                                            if (accumulatedY > 0) SnakeDirection.DOWN else SnakeDirection.UP
                                        )
                                    }
                                }
                            }
                        )
                    }
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                val rainbowPalette = remember {
                    listOf(SnakeGreen, AccentYellow, CoralPink, LightBlue, TileOrange, TilePurple)
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellW = size.width / GRID_SIZE
                    val cellH = size.height / GRID_SIZE

                    // 1. Líneas de la cuadrícula
                    for (i in 1 until GRID_SIZE) {
                        drawLine(
                            color = NavySubtleGrid,
                            start = Offset(i * cellW, 0f),
                            end = Offset(i * cellW, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = NavySubtleGrid,
                            start = Offset(0f, i * cellH),
                            end = Offset(size.width, i * cellH),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // 2. Comida rosa circular con pulso
                    val foodCenter = Offset(
                        x = (food.x + 0.5f) * cellW,
                        y = (food.y + 0.5f) * cellH
                    )
                    drawCircle(
                        color = CoralPink,
                        radius = (cellW * 0.36f) * foodScale,
                        center = foodCenter
                    )

                    // 3. Segmentos de la serpiente
                    snake.forEachIndexed { index, pt ->
                        val padding = cellW * 0.08f
                        val segmentColor = when {
                            rainbowMode -> rainbowPalette[index % rainbowPalette.size]
                            index == 0 -> SnakeGreen
                            else -> if (index % 2 == 0) SnakeGreen else SnakeGreenDark
                        }

                        drawRoundRect(
                            color = segmentColor,
                            topLeft = Offset(pt.x * cellW + padding, pt.y * cellH + padding),
                            size = Size(cellW - padding * 2, cellH - padding * 2),
                            cornerRadius = CornerRadius(cellW * 0.28f, cellH * 0.28f)
                        )

                        // Ojos en la cabeza de la serpiente
                        if (index == 0) {
                            val cx = (pt.x + 0.5f) * cellW
                            val cy = (pt.y + 0.5f) * cellH
                            val eyeOffsetForward = cellW * 0.16f
                            val eyeOffsetSide = cellW * 0.17f
                            val (eye1, eye2) = when (direction) {
                                SnakeDirection.RIGHT -> Offset(cx + eyeOffsetForward, cy - eyeOffsetSide) to
                                        Offset(cx + eyeOffsetForward, cy + eyeOffsetSide)
                                SnakeDirection.LEFT -> Offset(cx - eyeOffsetForward, cy - eyeOffsetSide) to
                                        Offset(cx - eyeOffsetForward, cy + eyeOffsetSide)
                                SnakeDirection.UP -> Offset(cx - eyeOffsetSide, cy - eyeOffsetForward) to
                                        Offset(cx + eyeOffsetSide, cy - eyeOffsetForward)
                                SnakeDirection.DOWN -> Offset(cx - eyeOffsetSide, cy + eyeOffsetForward) to
                                        Offset(cx + eyeOffsetSide, cy + eyeOffsetForward)
                            }
                            drawCircle(color = TextDarkNavy, radius = cellW * 0.08f, center = eye1)
                            drawCircle(color = TextDarkNavy, radius = cellW * 0.08f, center = eye2)
                        }
                    }
                }

                // Cartel flotante superior cuando espera el primer movimiento del jugador
                if (waitingToStart && !isGameOver) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(NavySurfaceLight.copy(alpha = 0.94f))
                            .border(1.dp, AccentYellow, RoundedCornerShape(50))
                            .clickable {
                                soundManager.playClick()
                                waitingToStart = false
                                isPaused = false
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (score > 0) {
                                "Partida guardada · Presiona una flecha para continuar"
                            } else {
                                "¿Listo? Presiona una flecha o desliza para empezar"
                            },
                            color = AccentYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Overlay de Pausa manual o Game Over
                if (isGameOver || (isPaused && !waitingToStart)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(NavyBackground.copy(alpha = 0.82f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = if (isGameOver) "¡Fin del juego!" else "Juego en Pausa",
                                color = TextWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isGameOver) {
                                    "Puntaje obtenido: $score"
                                } else {
                                    "Tu progreso se ha guardado automáticamente"
                                },
                                color = TextMuted,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    soundManager.playClick()
                                    if (isGameOver) restartGame() else isPaused = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentYellow,
                                    contentColor = TextDarkNavy
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = if (isGameOver) "Jugar de nuevo" else "Continuar partida",
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Cruceta direccional (D-Pad) con iconos vectoriales centrados
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            DPadButton(
                direction = SnakeDirection.UP,
                onClick = { handleDirectionInput(SnakeDirection.UP) }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DPadButton(
                    direction = SnakeDirection.LEFT,
                    onClick = { handleDirectionInput(SnakeDirection.LEFT) }
                )
                Spacer(modifier = Modifier.width(48.dp))
                DPadButton(
                    direction = SnakeDirection.RIGHT,
                    onClick = { handleDirectionInput(SnakeDirection.RIGHT) }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            DPadButton(
                direction = SnakeDirection.DOWN,
                onClick = { handleDirectionInput(SnakeDirection.DOWN) }
            )
        }

        Text(
            text = "Usa los botones o desliza para mover la serpiente",
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun DPadButton(
    direction: SnakeDirection,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NavySurface)
            .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                when (direction) {
                    SnakeDirection.UP -> {
                        moveTo(w * 0.50f, h * 0.18f)
                        lineTo(w * 0.86f, h * 0.80f)
                        lineTo(w * 0.14f, h * 0.80f)
                        close()
                    }
                    SnakeDirection.DOWN -> {
                        moveTo(w * 0.14f, h * 0.20f)
                        lineTo(w * 0.86f, h * 0.20f)
                        lineTo(w * 0.50f, h * 0.82f)
                        close()
                    }
                    SnakeDirection.LEFT -> {
                        moveTo(w * 0.18f, h * 0.50f)
                        lineTo(w * 0.80f, h * 0.14f)
                        lineTo(w * 0.80f, h * 0.86f)
                        close()
                    }
                    SnakeDirection.RIGHT -> {
                        moveTo(w * 0.82f, h * 0.50f)
                        lineTo(w * 0.20f, h * 0.14f)
                        lineTo(w * 0.20f, h * 0.86f)
                        close()
                    }
                }
            }
            drawPath(path = path, color = AccentYellow)
        }
    }
}
