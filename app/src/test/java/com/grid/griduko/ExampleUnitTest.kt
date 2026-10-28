package com.grid.griduko

import com.grid.griduko.data.GameMechanics
import com.grid.griduko.data.SnakeDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun snakeDirection_oppositeDetectionWorks() {
        assertTrue(SnakeDirection.UP.isOpposite(SnakeDirection.DOWN))
        assertTrue(SnakeDirection.LEFT.isOpposite(SnakeDirection.RIGHT))
        assertFalse(SnakeDirection.UP.isOpposite(SnakeDirection.LEFT))
    }

    @Test
    fun game2048_mergeLineLeftCombinesMatchingPairsOnce() {
        val (merged, points, maxTile) = GameMechanics.mergeLineLeft(listOf(2, 2, 4, 8))
        assertEquals(listOf(4, 4, 8, 0), merged)
        assertEquals(4, points)
        assertEquals(4, maxTile)
    }

    @Test
    fun game2048_moveRightUpdatesBoardAndScore() {
        val board = listOf(
            2, 2, 0, 0,
            4, 0, 4, 0,
            0, 0, 0, 0,
            2, 4, 8, 16
        )
        val result = GameMechanics.move2048(board, SnakeDirection.RIGHT)
        assertTrue(result.moved)
        assertEquals(12, result.pointsEarned) // 4 + 8
        assertEquals(4, result.newBoard[3])
        assertEquals(8, result.newBoard[7])
    }

    @Test
    fun ticTacToe_detectsRowAndDiagonalWinners() {
        val boardXWins = listOf(
            "X", "O", "X",
            "", "X", "O",
            "", "", "X"
        )
        val (winner, line) = GameMechanics.checkTicTacToeWinner(boardXWins)
        assertEquals("X", winner)
        assertEquals(listOf(0, 4, 8), line)
    }

    @Test
    fun ticTacToe_cpuBlocksHumanWinningMove() {
        val board = listOf(
            "X", "X", "",
            "", "O", "",
            "", "", ""
        )
        val cpuMove = GameMechanics.computeBestCpuMove(board, cpuSymbol = "O", humanSymbol = "X")
        assertEquals(2, cpuMove)
    }
}
