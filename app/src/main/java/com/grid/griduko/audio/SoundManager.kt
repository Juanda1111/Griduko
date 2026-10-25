package com.grid.griduko.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Motor de síntesis de sonido retro arcade mediante AudioTrack PCM.
 * No requiere archivos externos en res/raw y funciona en cualquier emulador o dispositivo.
 */
class SoundManager(var soundEnabled: Boolean = true) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun playClick() {
        playNotes(listOf(Note(660.0, 35)))
    }

    fun playMove() {
        playNotes(listOf(Note(440.0, 28)))
    }

    fun playEat() {
        playNotes(
            listOf(
                Note(587.33, 55), // D5
                Note(880.00, 90)  // A5
            )
        )
    }

    fun playMerge(tileValue: Int) {
        val baseFreq = (380.0 + (tileValue.coerceAtMost(2048) * 0.25)).coerceAtMost(1100.0)
        playNotes(
            listOf(
                Note(baseFreq, 45),
                Note(baseFreq * 1.25, 75)
            )
        )
    }

    fun playPlaceX() {
        playNotes(listOf(Note(523.25, 65))) // C5
    }

    fun playPlaceO() {
        playNotes(listOf(Note(659.25, 65))) // E5
    }

    fun playWin() {
        playNotes(
            listOf(
                Note(523.25, 80),  // C5
                Note(659.25, 80),  // E5
                Note(783.99, 80),  // G5
                Note(1046.50, 180) // C6
            )
        )
    }

    fun playGameOver() {
        playNotes(
            listOf(
                Note(440.0, 110),  // A4
                Note(392.0, 110),  // G4
                Note(349.23, 110), // F4
                Note(293.66, 220)  // D4
            )
        )
    }

    fun playEasterEgg() {
        // Fanfarria secreta estilo power-up arcade
        playNotes(
            listOf(
                Note(587.33, 60),
                Note(739.99, 60),
                Note(880.00, 60),
                Note(1174.66, 110),
                Note(880.00, 60),
                Note(1174.66, 200)
            )
        )
    }

    private data class Note(val frequencyHz: Double, val durationMs: Int)

    private fun playNotes(notes: List<Note>) {
        if (!soundEnabled) return
        scope.launch {
            runCatching {
                val sampleRate = 22050
                val totalDurationMs = notes.sumOf { it.durationMs }
                val totalSamples = (sampleRate * (totalDurationMs / 1000.0)).toInt().coerceAtLeast(1)
                val buffer = ShortArray(totalSamples)

                var offset = 0
                for (note in notes) {
                    val noteSamples = (sampleRate * (note.durationMs / 1000.0)).toInt()
                    val attackSamples = (noteSamples * 0.1f).toInt().coerceAtLeast(1)
                    val releaseSamples = (noteSamples * 0.25f).toInt().coerceAtLeast(1)

                    for (i in 0 until noteSamples) {
                        if (offset + i >= totalSamples) break
                        val env = when {
                            i < attackSamples -> i.toFloat() / attackSamples
                            i > noteSamples - releaseSamples -> (noteSamples - i).toFloat() / releaseSamples
                            else -> 1f
                        }
                        val angle = 2.0 * PI * i * note.frequencyHz / sampleRate
                        // Onda suave con armónico ligero tipo chiptune
                        val sampleValue = (sin(angle) * 0.75 + sin(angle * 2.0) * 0.25)
                        buffer[offset + i] = (sampleValue * env * Short.MAX_VALUE * 0.28).toInt().toShort()
                    }
                    offset += noteSamples
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                kotlinx.coroutines.delay(totalDurationMs.toLong() + 50L)
                audioTrack.stop()
                audioTrack.release()
            }
        }
    }
}
