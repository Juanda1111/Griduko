package com.grid.griduko.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

data class GridPoint(val x: Int, val y: Int)

enum class SnakeDirection(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    fun isOpposite(other: SnakeDirection): Boolean =
        (this.dx + other.dx == 0) && (this.dy + other.dy == 0)
}

data class SnakeSavedState(
    val snake: List<GridPoint>,
    val food: GridPoint,
    val direction: SnakeDirection,
    val score: Int,
    val highScore: Int,
    val isPaused: Boolean,
    val isGameOver: Boolean,
    val rainbowModeUnlocked: Boolean
)

data class Game2048SavedState(
    val board: List<Int>, // 16 celdas (4x4)
    val previousBoard: List<Int>?,
    val score: Int,
    val previousScore: Int,
    val highScore: Int,
    val isGameOver: Boolean
)

data class TicTacToeSavedState(
    val board: List<String>, // 9 celdas ("", "X", "O")
    val currentTurn: String, // "X" o "O"
    val vsCpu: Boolean,
    val scoreX: Int,
    val draws: Int,
    val scoreO: Int,
    val winner: String?, // "X", "O", "DRAW", o null
    val winningLine: List<Int>
)

data class CreditsInfo(
    val developerName: String,
    val designerName: String,
    val acknowledgements: String
)

/**
 * Lógica pura de 2048, Snake y Tres en Raya (también usada por pruebas unitarias)
 */
object GameMechanics {

    // --- Lógica de 2048 ---
    data class Move2048Result(
        val newBoard: List<Int>,
        val pointsEarned: Int,
        val maxMergedTile: Int,
        val moved: Boolean
    )

    fun mergeLineLeft(line: List<Int>): Triple<List<Int>, Int, Int> {
        val nonZero = line.filter { it != 0 }
        val result = mutableListOf<Int>()
        var points = 0
        var maxMerged = 0
        var i = 0
        while (i < nonZero.size) {
            if (i + 1 < nonZero.size && nonZero[i] == nonZero[i + 1]) {
                val mergedVal = nonZero[i] * 2
                result.add(mergedVal)
                points += mergedVal
                if (mergedVal > maxMerged) maxMerged = mergedVal
                i += 2
            } else {
                result.add(nonZero[i])
                i += 1
            }
        }
        while (result.size < 4) {
            result.add(0)
        }
        return Triple(result, points, maxMerged)
    }

    fun move2048(board: List<Int>, direction: SnakeDirection): Move2048Result {
        val grid = Array(4) { r -> IntArray(4) { c -> board[r * 4 + c] } }
        var totalPoints = 0
        var highestMerge = 0

        when (direction) {
            SnakeDirection.LEFT -> {
                for (r in 0..3) {
                    val (merged, pts, maxM) = mergeLineLeft(grid[r].toList())
                    for (c in 0..3) grid[r][c] = merged[c]
                    totalPoints += pts
                    if (maxM > highestMerge) highestMerge = maxM
                }
            }
            SnakeDirection.RIGHT -> {
                for (r in 0..3) {
                    val reversed = grid[r].reversed()
                    val (merged, pts, maxM) = mergeLineLeft(reversed)
                    val restored = merged.reversed()
                    for (c in 0..3) grid[r][c] = restored[c]
                    totalPoints += pts
                    if (maxM > highestMerge) highestMerge = maxM
                }
            }
            SnakeDirection.UP -> {
                for (c in 0..3) {
                    val col = (0..3).map { r -> grid[r][c] }
                    val (merged, pts, maxM) = mergeLineLeft(col)
                    for (r in 0..3) grid[r][c] = merged[r]
                    totalPoints += pts
                    if (maxM > highestMerge) highestMerge = maxM
                }
            }
            SnakeDirection.DOWN -> {
                for (c in 0..3) {
                    val col = (0..3).map { r -> grid[r][c] }.reversed()
                    val (merged, pts, maxM) = mergeLineLeft(col)
                    val restored = merged.reversed()
                    for (r in 0..3) grid[r][c] = restored[r]
                    totalPoints += pts
                    if (maxM > highestMerge) highestMerge = maxM
                }
            }
        }

        val newFlat = mutableListOf<Int>()
        for (r in 0..3) {
            for (c in 0..3) {
                newFlat.add(grid[r][c])
            }
        }

        return Move2048Result(
            newBoard = newFlat,
            pointsEarned = totalPoints,
            maxMergedTile = highestMerge,
            moved = newFlat != board
        )
    }

    fun spawnRandomTile(board: List<Int>, random: Random = Random.Default): List<Int> {
        val emptyIndices = board.indices.filter { board[it] == 0 }
        if (emptyIndices.isEmpty()) return board
        val chosenIndex = emptyIndices[random.nextInt(emptyIndices.size)]
        val tileValue = if (random.nextFloat() < 0.88f) 2 else 4
        return board.toMutableList().also { it[chosenIndex] = tileValue }
    }

    fun canMove2048(board: List<Int>): Boolean {
        if (board.any { it == 0 }) return true
        for (r in 0..3) {
            for (c in 0..3) {
                val current = board[r * 4 + c]
                if (c < 3 && current == board[r * 4 + (c + 1)]) return true
                if (r < 3 && current == board[(r + 1) * 4 + c]) return true
            }
        }
        return false
    }

    // --- Lógica de Tres en Raya ---
    val WINNING_LINES = listOf(
        listOf(0, 1, 2),
        listOf(3, 4, 5),
        listOf(6, 7, 8),
        listOf(0, 3, 6),
        listOf(1, 4, 7),
        listOf(2, 5, 8),
        listOf(0, 4, 8),
        listOf(2, 4, 6)
    )

    fun checkTicTacToeWinner(board: List<String>): Pair<String?, List<Int>> {
        for (line in WINNING_LINES) {
            val a = board[line[0]]
            val b = board[line[1]]
            val c = board[line[2]]
            if (a.isNotEmpty() && a == b && b == c) {
                return a to line
            }
        }
        if (board.none { it.isEmpty() }) {
            return "DRAW" to emptyList()
        }
        return null to emptyList()
    }

    fun computeBestCpuMove(board: List<String>, cpuSymbol: String = "O", humanSymbol: String = "X"): Int {
        val emptyIndices = board.indices.filter { board[it].isEmpty() }
        if (emptyIndices.isEmpty()) return -1

        // 1. Si la CPU puede ganar en este turno, juega esa casilla
        for (idx in emptyIndices) {
            val copy = board.toMutableList().also { it[idx] = cpuSymbol }
            if (checkTicTacToeWinner(copy).first == cpuSymbol) return idx
        }

        // 2. Si el jugador humano puede ganar en el siguiente turno, bloquéalo
        for (idx in emptyIndices) {
            val copy = board.toMutableList().also { it[idx] = humanSymbol }
            if (checkTicTacToeWinner(copy).first == humanSymbol) return idx
        }

        // 3. Tomar el centro si está libre
        if (board[4].isEmpty()) return 4

        // 4. Tomar una esquina disponible
        val corners = listOf(0, 2, 6, 8).filter { board[it].isEmpty() }
        if (corners.isNotEmpty()) return corners.random()

        // 5. Cualquier casilla disponible
        return emptyIndices.random()
    }
}

/**
 * Almacenamiento persistente entre vistas y sesiones de la aplicación.
 */
class GameStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("griduko_Persistent_prefs", Context.MODE_PRIVATE)

    private val _snakeState = MutableStateFlow(loadSnakeState())
    val snakeState: StateFlow<SnakeSavedState> = _snakeState.asStateFlow()

    private val _game2048State = MutableStateFlow(load2048State())
    val game2048State: StateFlow<Game2048SavedState> = _game2048State.asStateFlow()

    private val _ticTacToeState = MutableStateFlow(loadTicTacToeState())
    val ticTacToeState: StateFlow<TicTacToeSavedState> = _ticTacToeState.asStateFlow()

    private val _creditsInfo = MutableStateFlow(loadCreditsInfo())
    val creditsInfo: StateFlow<CreditsInfo> = _creditsInfo.asStateFlow()

    private val _easterEggCount = MutableStateFlow(prefs.getInt("easter_egg_count", 0))
    val easterEggCount: StateFlow<Int> = _easterEggCount.asStateFlow()

    // --- Persistencia de Snake ---
    fun defaultSnakePoints(): List<GridPoint> = listOf(
        GridPoint(8, 7),
        GridPoint(7, 7),
        GridPoint(6, 7),
        GridPoint(6, 8),
        GridPoint(6, 9),
        GridPoint(5, 9),
        GridPoint(4, 9)
    )

    private fun loadSnakeState(): SnakeSavedState {
        val highScore = prefs.getInt("snake_high_score", 0)
        val score = prefs.getInt("snake_score", 0)
        val dirName = prefs.getString("snake_dir", SnakeDirection.RIGHT.name) ?: SnakeDirection.RIGHT.name
        val direction = runCatching { SnakeDirection.valueOf(dirName) }.getOrDefault(SnakeDirection.RIGHT)
        val foodX = prefs.getInt("snake_food_x", 11)
        val foodY = prefs.getInt("snake_food_y", 4)
        val isGameOver = prefs.getBoolean("snake_game_over", false)
        val rainbow = prefs.getBoolean("snake_rainbow_mode", false)

        val serializedBody = prefs.getString("snake_body", null)
        val body = if (!serializedBody.isNullOrBlank()) {
            serializedBody.split(";").mapNotNull { token ->
                val parts = token.split(",")
                if (parts.size == 2) {
                    val x = parts[0].toIntOrNull()
                    val y = parts[1].toIntOrNull()
                    if (x != null && y != null) GridPoint(x, y) else null
                } else null
            }.ifEmpty { defaultSnakePoints() }
        } else {
            defaultSnakePoints()
        }

        return SnakeSavedState(
            snake = body,
            food = GridPoint(foodX, foodY),
            direction = direction,
            score = score,
            highScore = highScore,
            isPaused = true, // Al volver a la vista inicia en pausa lista para continuar
            isGameOver = isGameOver,
            rainbowModeUnlocked = rainbow
        )
    }

    fun saveSnakeState(state: SnakeSavedState) {
        val updatedHigh = maxOf(state.highScore, state.score)
        val finalState = state.copy(highScore = updatedHigh)
        _snakeState.value = finalState

        val serializedBody = finalState.snake.joinToString(";") { "${it.x},${it.y}" }
        prefs.edit()
            .putString("snake_body", serializedBody)
            .putInt("snake_food_x", finalState.food.x)
            .putInt("snake_food_y", finalState.food.y)
            .putString("snake_dir", finalState.direction.name)
            .putInt("snake_score", finalState.score)
            .putInt("snake_high_score", finalState.highScore)
            .putBoolean("snake_game_over", finalState.isGameOver)
            .putBoolean("snake_rainbow_mode", finalState.rainbowModeUnlocked)
            .apply()
    }

    // --- Persistencia de 2048 ---
    private fun createInitial2048Board(): List<Int> {
        var board = List(16) { 0 }
        board = GameMechanics.spawnRandomTile(board)
        board = GameMechanics.spawnRandomTile(board)
        return board
    }

    private fun load2048State(): Game2048SavedState {
        val rawBoard = prefs.getString("2048_board", null)
        val board = if (!rawBoard.isNullOrBlank()) {
            val parsed = rawBoard.split(",").mapNotNull { it.toIntOrNull() }
            if (parsed.size == 16 && parsed.any { it > 0 }) parsed else createInitial2048Board()
        } else {
            createInitial2048Board()
        }

        val rawPrev = prefs.getString("2048_prev_board", null)
        val prevBoard = if (!rawPrev.isNullOrBlank()) {
            val parsed = rawPrev.split(",").mapNotNull { it.toIntOrNull() }
            if (parsed.size == 16) parsed else null
        } else null

        val score = prefs.getInt("2048_score", 0)
        val prevScore = prefs.getInt("2048_prev_score", 0)
        val highScore = prefs.getInt("2048_high_score", 0)
        val isGameOver = !GameMechanics.canMove2048(board)

        return Game2048SavedState(
            board = board,
            previousBoard = prevBoard,
            score = score,
            previousScore = prevScore,
            highScore = maxOf(highScore, score),
            isGameOver = isGameOver
        )
    }

    fun save2048State(state: Game2048SavedState) {
        val updatedHigh = maxOf(state.highScore, state.score)
        val finalState = state.copy(highScore = updatedHigh)
        _game2048State.value = finalState

        val editor = prefs.edit()
            .putString("2048_board", finalState.board.joinToString(","))
            .putInt("2048_score", finalState.score)
            .putInt("2048_prev_score", finalState.previousScore)
            .putInt("2048_high_score", finalState.highScore)

        if (finalState.previousBoard != null) {
            editor.putString("2048_prev_board", finalState.previousBoard.joinToString(","))
        } else {
            editor.remove("2048_prev_board")
        }
        editor.apply()
    }

    // --- Persistencia de Tres en Raya ---
    private fun loadTicTacToeState(): TicTacToeSavedState {
        val rawBoard = prefs.getString("ttt_board", null)
        val board = if (rawBoard != null) {
            val parts = rawBoard.split("|")
            if (parts.size == 9) parts else List(9) { "" }
        } else {
            List(9) { "" }
        }

        val currentTurn = prefs.getString("ttt_turn", "X") ?: "X"
        val vsCpu = prefs.getBoolean("ttt_vs_cpu", true)
        val scoreX = prefs.getInt("ttt_score_x", 0)
        val draws = prefs.getInt("ttt_draws", 0)
        val scoreO = prefs.getInt("ttt_score_o", 0)
        val (winner, winningLine) = GameMechanics.checkTicTacToeWinner(board)

        return TicTacToeSavedState(
            board = board,
            currentTurn = currentTurn,
            vsCpu = vsCpu,
            scoreX = scoreX,
            draws = draws,
            scoreO = scoreO,
            winner = winner,
            winningLine = winningLine
        )
    }

    fun saveTicTacToeState(state: TicTacToeSavedState) {
        _ticTacToeState.value = state
        prefs.edit()
            .putString("ttt_board", state.board.joinToString("|"))
            .putString("ttt_turn", state.currentTurn)
            .putBoolean("ttt_vs_cpu", state.vsCpu)
            .putInt("ttt_score_x", state.scoreX)
            .putInt("ttt_draws", state.draws)
            .putInt("ttt_score_o", state.scoreO)
            .apply()
    }

    // --- Persistencia de Créditos y Easter Eggs ---
    private fun loadCreditsInfo(): CreditsInfo {
        return CreditsInfo(
            developerName = prefs.getString("credits_dev", "Equipo Griduko") ?: "Equipo Griduko",
            designerName = prefs.getString("credits_design", "Equipo Griduko") ?: "Equipo Griduko",
            acknowledgements = prefs.getString(
                "credits_thanks",
                "Gracias por jugar y descubrir los secretos de Griduko"
            ) ?: "Gracias por jugar y descubrir los secretos de Griduko"
        )
    }

    fun saveCreditsInfo(info: CreditsInfo) {
        _creditsInfo.value = info
        prefs.edit()
            .putString("credits_dev", info.developerName)
            .putString("credits_design", info.designerName)
            .putString("credits_thanks", info.acknowledgements)
            .apply()
    }

    fun registerEasterEggFound() {
        val next = _easterEggCount.value + 1
        _easterEggCount.value = next
        prefs.edit().putInt("easter_egg_count", next).apply()
    }
}
