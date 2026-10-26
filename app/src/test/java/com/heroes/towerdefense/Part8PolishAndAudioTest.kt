package com.heroes.towerdefense

import com.heroes.towerdefense.audio.SoundManager
import com.heroes.towerdefense.core.GameConstants
import com.heroes.towerdefense.data.GamePreferences
import com.heroes.towerdefense.data.InMemoryStorage
import com.heroes.towerdefense.game.model.CombatParticle
import com.heroes.towerdefense.model.hero.Hero
import com.heroes.towerdefense.model.hero.HeroRarity
import com.heroes.towerdefense.model.tower.TowerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit Tests for Part 8:
 * - SoundManager procedural synthesis algorithms and waveform buffer integrity
 * - Volume scaling, clamping, and preferences synchronization
 * - Audio lifecycle states and music toggles
 * - Dynamic Combat Particle physics, drag, gravity, and expiration
 * - Stage progression dynamic access and visual badge states
 * - Roster visual identity and rarity integrity
 */
class Part8PolishAndAudioTest {

    private lateinit var storage: InMemoryStorage
    private lateinit var preferences: GamePreferences

    @Before
    fun setUp() {
        storage = InMemoryStorage()
        preferences = GamePreferences(storage)
        SoundManager.init(preferences)
    }

    // =========================================================================
    // 1. SoundManager & Volume Controls
    // =========================================================================

    @Test
    fun testSoundManagerVolumeInitializationAndClamping() {
        preferences.masterVolume = 75
        preferences.sfxVolume = 60
        preferences.isMusicEnabled = true
        SoundManager.init(preferences)

        assertEquals(75, SoundManager.masterVolume)
        assertEquals(60, SoundManager.sfxVolume)
        assertTrue(SoundManager.isMusicEnabled)

        // Effective SFX multiplier: 0.75 * 0.60 = 0.45
        assertEquals(0.45f, SoundManager.effectiveSfxVolumeMultiplier, 0.001f)

        // Clamping boundaries test: < 0 and > 100
        SoundManager.updateVolume(master = -10, sfx = 150, musicEnabled = false)
        assertEquals(GameConstants.MIN_VOLUME, SoundManager.masterVolume)
        assertEquals(GameConstants.MAX_VOLUME, SoundManager.sfxVolume)
        assertFalse(SoundManager.isMusicEnabled)
        assertEquals(0.0f, SoundManager.effectiveSfxVolumeMultiplier, 0.001f)
        assertEquals(0.0f, SoundManager.effectiveMusicVolumeMultiplier, 0.001f)
    }

    @Test
    fun testSoundManagerMuteWhenVolumeIsZero() {
        SoundManager.updateVolume(master = 0, sfx = 100, musicEnabled = true)
        assertEquals(0.0f, SoundManager.effectiveSfxVolumeMultiplier, 0.001f)
        assertEquals(0.0f, SoundManager.effectiveMusicVolumeMultiplier, 0.001f)

        SoundManager.updateVolume(master = 100, sfx = 0, musicEnabled = true)
        assertEquals(0.0f, SoundManager.effectiveSfxVolumeMultiplier, 0.001f)
    }

    @Test
    fun testSoundManagerMusicToggleControls() {
        SoundManager.updateVolume(master = 80, sfx = 80, musicEnabled = true)
        assertTrue(SoundManager.effectiveMusicVolumeMultiplier > 0f)

        SoundManager.updateVolume(master = 80, sfx = 80, musicEnabled = false)
        assertEquals(0.0f, SoundManager.effectiveMusicVolumeMultiplier, 0.001f)
    }

    // =========================================================================
    // 2. Procedural Waveform Synthesis Algorithms
    // =========================================================================

    @Test
    fun testToneSynthesisBufferLengthAndFormat() {
        val durationMs = 100
        val sampleRate = 22050
        val expectedSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val expectedBytes = expectedSamples * 2 // 16-bit mono

        val pcm = SoundManager.generateTone(freq = 440f, durationMs = durationMs)
        assertNotNull(pcm)
        assertEquals(expectedBytes, pcm.size)

        // Verify buffer is not all zeros
        val hasNonZero = pcm.any { it != 0.toByte() }
        assertTrue("Synthesized tone PCM must contain non-zero audio samples", hasNonZero)
    }

    @Test
    fun testChirpSynthesisBufferLengthAndFormat() {
        val durationMs = 80
        val expectedSamples = (22050 * (durationMs / 1000f)).toInt()
        val expectedBytes = expectedSamples * 2

        val pcm = SoundManager.generateChirp(startFreq = 400f, endFreq = 800f, durationMs = durationMs)
        assertNotNull(pcm)
        assertEquals(expectedBytes, pcm.size)
        assertTrue("Synthesized chirp must contain non-zero audio samples", pcm.any { it != 0.toByte() })
    }

    @Test
    fun testChordSynthesisBufferLength() {
        val durationMs = 150
        val expectedSamples = (22050 * (durationMs / 1000f)).toInt()
        val expectedBytes = expectedSamples * 2

        val pcm = SoundManager.generateChord(listOf(261.63f, 329.63f, 392.00f), durationMs = durationMs)
        assertEquals(expectedBytes, pcm.size)
        assertTrue(pcm.any { it != 0.toByte() })
    }

    @Test
    fun testExplosionSynthesisBufferLength() {
        val durationMs = 120
        val expectedSamples = (22050 * (durationMs / 1000f)).toInt()
        val expectedBytes = expectedSamples * 2

        val pcm = SoundManager.generateExplosion(durationMs = durationMs)
        assertEquals(expectedBytes, pcm.size)
        assertTrue(pcm.any { it != 0.toByte() })
    }

    @Test
    fun testArpeggioSynthesisCombinesNotes() {
        val notes = listOf(440f, 550f, 660f)
        val noteDurationMs = 50
        val pcm = SoundManager.generateArpeggio(notes, noteDurationMs = noteDurationMs)

        val singleToneSize = SoundManager.generateTone(440f, noteDurationMs).size
        val expectedSize = singleToneSize * notes.size
        assertEquals(expectedSize, pcm.size)
    }

    // =========================================================================
    // 3. Audio Triggers Safe Execution (JVM Safe)
    // =========================================================================

    @Test
    fun testSoundEffectTriggersExecuteWithoutExceptions() {
        // All SFX methods should execute safely without unhandled exceptions
        SoundManager.playClick()
        SoundManager.playTowerShoot(TowerType.ARCHER_TOWER)
        SoundManager.playTowerShoot(TowerType.MAGE_TOWER)
        SoundManager.playTowerShoot(TowerType.CANNON_TOWER)
        SoundManager.playHeroSkill("hero_kael")
        SoundManager.playHeroSkill("hero_lyra")
        SoundManager.playHeroSkill("hero_brakk")
        SoundManager.playHeroSkill("hero_nyra")
        SoundManager.playHeroSkill("hero_tiko")
        SoundManager.playHeroSkill("hero_vesper")
        SoundManager.playEnemyHit()
        SoundManager.playEnemyDefeat()
        SoundManager.playBossIntro()
        SoundManager.playBossDefeat()
        SoundManager.playVictory()
        SoundManager.playDefeat()

        // Music lifecycle calls
        SoundManager.pauseMusic()
        SoundManager.stopMusic()
        SoundManager.release()
        assertFalse(SoundManager.isMusicRunning)
    }

    // =========================================================================
    // 4. Combat Particle Physics & Simulation
    // =========================================================================

    @Test
    fun testCombatParticlePhysicsAndLifetimeDecay() {
        val particle = CombatParticle(
            x = 100f,
            y = 100f,
            vx = 50f,
            vy = -30f,
            color = 0xFFF59E0B.toInt(),
            radius = 6f,
            alpha = 1.0f,
            maxLifetime = 0.5f,
            lifetime = 0.0f
        )

        assertFalse(particle.isExpired)
        assertEquals(1.0f, particle.alpha, 0.001f)

        // Advance 0.25s (halfway)
        particle.update(0.25f)
        assertFalse(particle.isExpired)
        assertEquals(0.5f, particle.progress, 0.01f)
        assertEquals(0.5f, particle.alpha, 0.01f)
        assertTrue("X position must advance with vx", particle.x > 100f)

        // Advance past lifetime (0.30s more -> total 0.55s > 0.5s)
        particle.update(0.30f)
        assertTrue("Particle must expire when lifetime exceeds maxLifetime", particle.isExpired)
        assertEquals(0.0f, particle.alpha, 0.001f)
    }

    // =========================================================================
    // 5. Stage Select Dynamic Progression Logic
    // =========================================================================

    @Test
    fun testStageProgressionAccessAndCompletion() {
        preferences.highestUnlockedStage = 3
        preferences.completedStages = setOf(1, 2)

        for (stageNum in 1..6) {
            val isUnlocked = stageNum <= preferences.highestUnlockedStage
            val isCompleted = preferences.completedStages.contains(stageNum)

            when (stageNum) {
                1, 2 -> {
                    assertTrue("Stage $stageNum must be unlocked", isUnlocked)
                    assertTrue("Stage $stageNum must be marked completed", isCompleted)
                }
                3 -> {
                    assertTrue("Stage 3 must be unlocked and active", isUnlocked)
                    assertFalse("Stage 3 is not yet completed", isCompleted)
                }
                else -> {
                    assertFalse("Stage $stageNum must be locked", isUnlocked)
                    assertFalse("Stage $stageNum cannot be completed", isCompleted)
                }
            }
        }
    }

    // =========================================================================
    // 6. Hero Visual Identity & Planned Roster Completeness
    // =========================================================================

    @Test
    fun testPlannedHeroRosterVisualIdentity() {
        val heroes = Hero.getPlannedHeroes()
        assertEquals(6, heroes.size)

        val rarities = heroes.map { it.rarity }.toSet()
        // 5 rarities represented across the 6 heroes
        assertTrue(rarities.contains(HeroRarity.COMMON))
        assertTrue(rarities.contains(HeroRarity.RARE))
        assertTrue(rarities.contains(HeroRarity.EPIC))
        assertTrue(rarities.contains(HeroRarity.LEGENDARY))
        assertTrue(rarities.contains(HeroRarity.MYTHIC))

        for (hero in heroes) {
            assertTrue("Hero ${hero.name} must have a non-blank title", hero.title.isNotBlank())
            assertEquals("Hero ${hero.name} must possess exactly 2 active skills", 2, hero.skills.size)
            assertTrue("Hero ${hero.name} rarity multiplier must be positive", hero.rarity.statMultiplier > 0f)
            assertTrue("Hero ${hero.name} rarity badge label must not be empty", hero.rarity.badgeLabel.isNotBlank())
        }
    }
}
