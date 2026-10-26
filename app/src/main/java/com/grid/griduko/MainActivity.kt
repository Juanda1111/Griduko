package com.grid.griduko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.grid.griduko.audio.SoundManager
import com.grid.griduko.data.GameStorage
import com.grid.griduko.ui.screens.CreditsScreen
import com.grid.griduko.ui.screens.Game2048Screen
import com.grid.griduko.ui.screens.SnakeScreen
import com.grid.griduko.ui.screens.SplashScreen
import com.grid.griduko.ui.screens.TicTacToeScreen
import com.grid.griduko.ui.screens.WelcomeScreen
import com.grid.griduko.ui.theme.GridukoTheme
import com.grid.griduko.ui.theme.NavyBackground

enum class GridukoScreen {
    SPLASH,
    WELCOME,
    SNAKE,
    GAME_2048,
    TIC_TAC_TOE,
    CREDITS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GridukoTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NavyBackground),
                    containerColor = NavyBackground
                ) { innerPadding ->
                    GridukoApp(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GridukoApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val storage = remember { GameStorage(context.applicationContext) }
    val soundManager = remember { SoundManager(soundEnabled = true) }

    var currentScreen by rememberSaveable { mutableStateOf(GridukoScreen.SPLASH) }

    val snakeState by storage.snakeState.collectAsState()
    val game2048State by storage.game2048State.collectAsState()
    val ticTacToeState by storage.ticTacToeState.collectAsState()
    val creditsInfo by storage.creditsInfo.collectAsState()
    val easterEggCount by storage.easterEggCount.collectAsState()

    // Manejo del botón físico "Atrás" para regresar al menú desde cualquier pantalla secundaria
    BackHandler(enabled = currentScreen != GridukoScreen.WELCOME && currentScreen != GridukoScreen.SPLASH) {
        soundManager.playClick()
        currentScreen = GridukoScreen.WELCOME
    }

    AnimatedContent(
        targetState = currentScreen,
        modifier = modifier.background(NavyBackground),
        transitionSpec = {
            if (targetState == GridukoScreen.WELCOME && initialState != GridukoScreen.SPLASH) {
                (slideInHorizontally(tween(280)) { -it / 3 } + fadeIn(tween(280))) togetherWith
                        (slideOutHorizontally(tween(280)) { it / 3 } + fadeOut(tween(220)))
            } else {
                (slideInHorizontally(tween(280)) { it / 3 } + fadeIn(tween(280))) togetherWith
                        (slideOutHorizontally(tween(280)) { -it / 3 } + fadeOut(tween(220)))
            }
        },
        label = "screen_navigation"
    ) { screen ->
        when (screen) {
            GridukoScreen.SPLASH -> {
                SplashScreen(
                    onSplashFinished = {
                        currentScreen = GridukoScreen.WELCOME
                    }
                )
            }

            GridukoScreen.WELCOME -> {
                val tttTotal = ticTacToeState.scoreX + ticTacToeState.scoreO + ticTacToeState.draws
                val tttSummary = if (tttTotal > 0) {
                    "X:${ticTacToeState.scoreX} · O:${ticTacToeState.scoreO}"
                } else ""

                WelcomeScreen(
                    snakeHighScore = snakeState.highScore,
                    game2048HighScore = game2048State.highScore,
                    ticTacToeSummary = tttSummary,
                    rainbowModeUnlocked = snakeState.rainbowModeUnlocked,
                    soundManager = soundManager,
                    onSelectSnake = { currentScreen = GridukoScreen.SNAKE },
                    onSelect2048 = { currentScreen = GridukoScreen.GAME_2048 },
                    onSelectTicTacToe = { currentScreen = GridukoScreen.TIC_TAC_TOE },
                    onOpenCredits = { currentScreen = GridukoScreen.CREDITS },
                    onUnlockLogoEasterEgg = {
                        storage.registerEasterEggFound()
                        storage.saveSnakeState(snakeState.copy(rainbowModeUnlocked = true))
                    }
                )
            }

            GridukoScreen.SNAKE -> {
                SnakeScreen(
                    initialState = snakeState,
                    storage = storage,
                    soundManager = soundManager,
                    onBack = { currentScreen = GridukoScreen.WELCOME }
                )
            }

            GridukoScreen.GAME_2048 -> {
                Game2048Screen(
                    initialState = game2048State,
                    storage = storage,
                    soundManager = soundManager,
                    onBack = { currentScreen = GridukoScreen.WELCOME }
                )
            }

            GridukoScreen.TIC_TAC_TOE -> {
                TicTacToeScreen(
                    initialState = ticTacToeState,
                    storage = storage,
                    soundManager = soundManager,
                    onBack = { currentScreen = GridukoScreen.WELCOME }
                )
            }

            GridukoScreen.CREDITS -> {
                CreditsScreen(
                    creditsInfo = creditsInfo,
                    easterEggsFound = easterEggCount,
                    rainbowModeUnlocked = snakeState.rainbowModeUnlocked,
                    storage = storage,
                    soundManager = soundManager,
                    onBackToHome = { currentScreen = GridukoScreen.WELCOME }
                )
            }
        }
    }
}
