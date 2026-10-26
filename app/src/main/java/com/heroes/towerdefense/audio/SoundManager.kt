package com.heroes.towerdefense.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import com.heroes.towerdefense.core.GameConstants
import com.heroes.towerdefense.data.GamePreferences
import com.heroes.towerdefense.model.tower.TowerType
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural Audio & Sound Effects Manager for Heroes Tower Defense (Part 8).
 * Synthesizes 16-bit PCM audio waves on-demand using Android AudioTrack without requiring
 * external audio files or dependencies.
 *
 * Fully integrated with GamePreferences (Master Volume, SFX Volume, Music Toggle).
 * Thread-safe, non-blocking asynchronous audio playback.
 */
object SoundManager {

    private const val SAMPLE_RATE = 22050
    private val audioExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "HTD-SoundManager-Thread").apply {
            isDaemon = true
            priority = Thread.NORM_PRIORITY - 1
        }
    }

    @Volatile
    var isMusicRunning: Boolean = false
        private set

    @Volatile
    private var musicLoopThread: Thread? = null

    // Volume state cached from preferences (0 to 100)
    @Volatile
    var masterVolume: Int = GameConstants.DEFAULT_MASTER_VOLUME
        private set

    @Volatile
    var sfxVolume: Int = GameConstants.DEFAULT_SFX_VOLUME
        private set

    @Volatile
    var isMusicEnabled: Boolean = GameConstants.DEFAULT_MUSIC_ENABLED
        private set

    // Sound effect wave types
    enum class WaveType {
        SINE, SQUARE, TRIANGLE, NOISE
    }

    /**
     * Initializes or synchronizes the SoundManager with player preferences.
     */
    fun init(preferences: GamePreferences) {
        masterVolume = preferences.masterVolume
        sfxVolume = preferences.sfxVolume
        isMusicEnabled = preferences.isMusicEnabled
    }

    fun updateVolume(master: Int, sfx: Int, musicEnabled: Boolean) {
        masterVolume = master.coerceIn(GameConstants.MIN_VOLUME, GameConstants.MAX_VOLUME)
        sfxVolume = sfx.coerceIn(GameConstants.MIN_VOLUME, GameConstants.MAX_VOLUME)
        isMusicEnabled = musicEnabled

        if (!isMusicEnabled && isMusicRunning) {
            pauseMusic()
        }
    }

    val effectiveSfxVolumeMultiplier: Float
        get() = (masterVolume / 100f) * (sfxVolume / 100f)

    val effectiveMusicVolumeMultiplier: Float
        get() = (masterVolume / 100f) * if (isMusicEnabled) 0.65f else 0.0f

    // =========================================================================
    // SFX Sound Triggers
    // =========================================================================

    fun playClick() {
        if (effectiveSfxVolumeMultiplier <= 0.01f) return
        val pcm = generateChirp(startFreq = 880f, endFreq = 1320f, durationMs = 35, volume = effectiveSfxVolumeMultiplier)
        playPcmAsync(pcm)
    }

    fun playTowerShoot(type: TowerType) {
        if (effectiveSfxVolumeMultiplier <= 0.01f) return
        val pcm = when (type) {
            TowerType.ARCHER_TOWER -> {
                // Swift twang / whoosh
                generateTone(freq = 620f, durationMs = 60, volume = effectiveSfxVolumeMultiplier * 0.85f, waveType = WaveType.TRIANGLE)
            }
            TowerType.MAGE_TOWER, TowerType.FROST_TOWER -> {
                // Arcane resonance chime
                generateChirp(startFreq = 420f, endFreq = 840f, durationMs = 90, volume = effectiveSfxVolumeMultiplier * 0.90f)
            }
            TowerType.CANNON_TOWER -> {
                // Low explosive rumble
                generateExplosion(durationMs = 140, volume = effectiveSfxVolumeMultiplier)
            }
        }
        playPcmAsync(pcm)
    }

    fun playHeroSkill(heroId: String) {
        if (effectiveSfxVolumeMultiplier <= 0.01f) return
        val pcm = when (heroId) {
            "hero_kael" -> generateChirp(startFreq = 300f, endFreq = 950f, durationMs = 150, volume = effectiveSfxVolumeMultiplier)
            "hero_lyra" -> generateChirp(startFreq = 700f, endFreq = 1400f, durationMs = 120, volume = effectiveSfxVolumeMultiplier)
            "hero_brakk" -> generateExplosion(durationMs = 180, volume = effectiveSfxVolumeMultiplier)
            "hero_nyra" -> generateChirp(startFreq = 650f, endFreq = 320f, durationMs = 140, volume = effectiveSfxVolumeMultiplier)
            "hero_tiko" -> generateExplosion(durationMs = 160, volume = effectiveSfxVolumeMultiplier)
            "hero_vesper" -> generateChord(listOf(440f, 660f, 880f), durationMs = 200, volume = effectiveSfxVolumeMultiplier)
            else -> generateChord(listOf(523.25f, 659.25f), durationMs = 150, volume = effectiveSfxVolumeMultiplier)
        }
        playPcmAsync(pcm)
    }

    fun playEnemyHit() {
        if (effectiveSfxVolumeMultiplier <= 0.01f) return
        val pcm = generateTone(freq = 280f, durationMs = 40, volume = effectiveSfxVolumeMultiplier * 0.6f, waveType = WaveType.SINE)
        playPcmAsync(pcm)
    }

    fun playEnemyDefeat() {
        if (effectiveSfxVolumeMultiplier <= 0.01f) return
        val pcm = generateChirp(startFreq = 400f, endFreq = 700f, durationMs = 70, volume = effectiveSfxVolumeMultiplier * 0.8f)
        playPcmAsync(pcm)
    }

    fun playBossIntro() {
        if (effectiveSfxVolumeMultiplier <= 0.01f) return
        val pcm = generateChord(listOf(110f, 165f, 220f), durationMs = 350, volume = effectiveSfxVolumeMultiplier)
        playPcmAsync(pcm)
    }

    fun playBossDefeat() {
        if (effectiveSfxVolumeMultiplier <= 0.01f) return
        val pcm = generateChord(listOf(220f, 330f, 440f, 550f), durationMs = 500, volume = effectiveSfxVolumeMultiplier)
        playPcmAsync(pcm)
    }

    fun playVictory() {
        if (effectiveSfxVolumeMultiplier <= 0.01f) return
        // Triumphant C Major arpeggio fanfare (C5, E5, G5, C6)
        val pcm = generateArpeggio(
            notes = listOf(523.25f, 659.25f, 783.99f, 1046.50f),
            noteDurationMs = 130,
            volume = effectiveSfxVolumeMultiplier
        )
        playPcmAsync(pcm)
    }

    fun playDefeat() {
        if (effectiveSfxVolumeMultiplier <= 0.01f) return
        // Somber minor cadence (Eb4, D4, C4)
        val pcm = generateArpeggio(
            notes = listOf(311.13f, 293.66f, 261.63f),
            noteDurationMs = 200,
            volume = effectiveSfxVolumeMultiplier
        )
        playPcmAsync(pcm)
    }

    // =========================================================================
    // Background Ambient Music Loop
    // =========================================================================

    /**
     * Starts peaceful, ambient fantasy arpeggio chord progression in a dedicated thread.
     * Checks isMusicEnabled and effectiveMusicVolumeMultiplier.
     */
    fun startMusic() {
        if (!isMusicEnabled || effectiveMusicVolumeMultiplier <= 0.01f) {
            isMusicRunning = false
            return
        }
        if (isMusicRunning) return

        isMusicRunning = true
        musicLoopThread = Thread({
            // Peaceful Dorian fantasy progression notes (D4, F4, G4, A4, C5)
            val fantasyMelody = listOf(
                293.66f, 349.23f, 440.00f, 523.25f,
                440.00f, 392.00f, 349.23f, 293.66f
            )
            while (isMusicRunning) {
                for (note in fantasyMelody) {
                    if (!isMusicRunning) break
                    val vol = effectiveMusicVolumeMultiplier
                    if (vol > 0.01f) {
                        val pcm = generateTone(
                            freq = note,
                            durationMs = 280,
                            volume = vol * 0.35f,
                            waveType = WaveType.SINE
                        )
                        playPcmSync(pcm)
                    }
                    try {
                        Thread.sleep(60L)
                    } catch (e: InterruptedException) {
                        isMusicRunning = false
                        break
                    }
                }
            }
        }, "HTD-AmbientMusic-Thread").apply {
            isDaemon = true
            priority = Thread.MIN_PRIORITY
            start()
        }
    }

    fun pauseMusic() {
        isMusicRunning = false
        musicLoopThread?.interrupt()
        musicLoopThread = null
    }

    fun stopMusic() {
        pauseMusic()
    }

    fun release() {
        stopMusic()
    }

    // =========================================================================
    // Waveform Synthesizer Algorithms (16-bit Mono PCM)
    // =========================================================================

    fun generateTone(
        freq: Float,
        durationMs: Int,
        volume: Float = 1.0f,
        waveType: WaveType = WaveType.SINE
    ): ByteArray {
        val totalSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt().coerceAtLeast(1)
        val buffer = ByteArray(totalSamples * 2) // 16-bit mono = 2 bytes per sample

        val attackSamples = (totalSamples * 0.10f).toInt().coerceAtLeast(1)
        val decaySamples = (totalSamples * 0.90f).toInt().coerceAtLeast(1)

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val rawSample = when (waveType) {
                WaveType.SINE -> sin(2.0 * PI * freq * t).toFloat()
                WaveType.TRIANGLE -> {
                    val period = 1f / freq
                    val phase = (t % period) / period
                    if (phase < 0.5f) (phase * 4f - 1f) else (3f - phase * 4f)
                }
                WaveType.SQUARE -> if (sin(2.0 * PI * freq * t) >= 0.0) 0.8f else -0.8f
                WaveType.NOISE -> ((Math.random() * 2.0 - 1.0).toFloat())
            }

            // Envelope: linear attack, exponential release
            val envelope = when {
                i < attackSamples -> i.toFloat() / attackSamples
                else -> (totalSamples - i).toFloat() / decaySamples
            }.coerceIn(0f, 1f)

            val scaled = (rawSample * envelope * volume * 32767f).toInt().coerceIn(-32768, 32767)
            buffer[i * 2] = (scaled and 0xFF).toByte()
            buffer[i * 2 + 1] = ((scaled shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    fun generateChirp(
        startFreq: Float,
        endFreq: Float,
        durationMs: Int,
        volume: Float = 1.0f
    ): ByteArray {
        val totalSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt().coerceAtLeast(1)
        val buffer = ByteArray(totalSamples * 2)

        var phase = 0.0
        for (i in 0 until totalSamples) {
            val progress = i.toFloat() / totalSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            phase += 2.0 * PI * currentFreq / SAMPLE_RATE

            val rawSample = sin(phase).toFloat()
            val envelope = (1f - progress).coerceIn(0f, 1f)
            val scaled = (rawSample * envelope * volume * 32767f).toInt().coerceIn(-32768, 32767)

            buffer[i * 2] = (scaled and 0xFF).toByte()
            buffer[i * 2 + 1] = ((scaled shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    fun generateChord(
        frequencies: List<Float>,
        durationMs: Int,
        volume: Float = 1.0f
    ): ByteArray {
        val totalSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt().coerceAtLeast(1)
        val buffer = ByteArray(totalSamples * 2)
        val numFreqs = frequencies.size.coerceAtLeast(1)

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            var sum = 0f
            for (f in frequencies) {
                sum += sin(2.0 * PI * f * t).toFloat()
            }
            sum /= numFreqs

            val envelope = ((totalSamples - i).toFloat() / totalSamples).coerceIn(0f, 1f)
            val scaled = (sum * envelope * volume * 32767f).toInt().coerceIn(-32768, 32767)

            buffer[i * 2] = (scaled and 0xFF).toByte()
            buffer[i * 2 + 1] = ((scaled shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    fun generateArpeggio(
        notes: List<Float>,
        noteDurationMs: Int,
        volume: Float = 1.0f
    ): ByteArray {
        val chunks = notes.map { freq ->
            generateTone(freq, noteDurationMs, volume, WaveType.TRIANGLE)
        }
        val totalBytes = chunks.sumOf { it.size }
        val combined = ByteArray(totalBytes)
        var offset = 0
        for (chunk in chunks) {
            System.arraycopy(chunk, 0, combined, offset, chunk.size)
            offset += chunk.size
        }
        return combined
    }

    fun generateExplosion(
        durationMs: Int,
        volume: Float = 1.0f
    ): ByteArray {
        val totalSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt().coerceAtLeast(1)
        val buffer = ByteArray(totalSamples * 2)

        var lastSample = 0f
        for (i in 0 until totalSamples) {
            val progress = i.toFloat() / totalSamples
            val whiteNoise = (Math.random() * 2.0 - 1.0).toFloat()
            // Low-pass filter noise for heavy boom rumble
            lastSample = lastSample * 0.85f + whiteNoise * 0.15f

            val envelope = ((1f - progress) * (1f - progress)).coerceIn(0f, 1f)
            val scaled = (lastSample * envelope * volume * 32767f).toInt().coerceIn(-32768, 32767)

            buffer[i * 2] = (scaled and 0xFF).toByte()
            buffer[i * 2 + 1] = ((scaled shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    // =========================================================================
    // Low-Level AudioTrack Player (Safe in Unit Tests and Android OS)
    // =========================================================================

    private fun playPcmAsync(pcmData: ByteArray) {
        if (pcmData.isEmpty()) return
        audioExecutor.execute {
            playPcmSync(pcmData)
        }
    }

    private fun playPcmSync(pcmData: ByteArray) {
        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = pcmData.size.coerceAtLeast(minBufSize)

            val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioTrack(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                    bufferSize,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
                )
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM
                )
            }

            track.play()
            track.write(pcmData, 0, pcmData.size)
            track.stop()
            track.release()
        } catch (_: Throwable) {
            // Graceful silent fallback in unit test JVM or unsupported audio device
        }
    }
}
