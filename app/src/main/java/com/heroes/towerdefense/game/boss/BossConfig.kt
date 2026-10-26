package com.heroes.towerdefense.game.boss

import com.heroes.towerdefense.core.GameConstants

/**
 * Reusable configuration template for boss encounters.
 * Specifies stats, resistances, vulnerability multipliers, intro cinematic parameters,
 * and telegraphed combat abilities.
 */
data class BossConfig(
    val id: String,
    val type: BossType,
    val name: String,
    val title: String,
    val description: String,
    val maxHealth: Int,
    val moveSpeed: Float,
    val baseDamageOnEscape: Int = 5,
    val coinReward: Int = 100,
    val introDuration: Float = 2.5f,
    val attacks: List<BossAttack> = emptyList(),
    val physicalResistance: Float = 0.0f,
    val vulnerabilityDamageMultiplier: Float = 1.5f,
    val vulnerabilityDuration: Float = 3.0f,
    val protectionDamageMultiplier: Float = 0.25f,
    val protectionDuration: Float = 2.5f,
    val introCinematicMessage: String = "",
    val primaryColor: Int = 0xFFEF4444.toInt(),
    val secondaryColor: Int = 0xFF1E293B.toInt()
) {
    init {
        require(id.isNotBlank()) { "Boss id cannot be blank" }
        require(name.isNotBlank()) { "Boss name cannot be blank" }
        require(maxHealth > 0) { "Boss max health must be greater than zero" }
        require(moveSpeed > 0f) { "Boss move speed must be greater than zero" }
        require(baseDamageOnEscape > 0) { "Base damage on escape must be greater than zero" }
        require(coinReward >= 0) { "Coin reward cannot be negative" }
        require(introDuration >= 0f) { "Intro duration cannot be negative" }
    }

    companion object {
        fun getObsidianGolem(): BossConfig = BossConfig(
            id = "boss_obsidian_golem",
            type = BossType.OBSIDIAN_GOLEM,
            name = "Golem de Obsidiana",
            title = "Colosso da Terra e Fogo",
            description = "Gigante forjado em rocha vulcânica e magma primordial. Couraça de pedra com 20% de resistência física. Após desferir o Golpe Sísmico, entra em Janela de Vulnerabilidade (+50% de dano).",
            maxHealth = 900,
            moveSpeed = 0.08f,
            baseDamageOnEscape = 5,
            coinReward = GameConstants.BOSS_GOLEM_COIN_REWARD,
            introDuration = 2.5f,
            physicalResistance = 0.20f,
            vulnerabilityDamageMultiplier = 1.50f,
            vulnerabilityDuration = 3.0f,
            introCinematicMessage = "Um tremor vulcânico ecoa! O Golem de Obsidiana desperta das profundezas!",
            primaryColor = 0xFFEA580C.toInt(), // Magma orange
            secondaryColor = 0xFF334155.toInt(), // Volcanic slate
            attacks = listOf(
                BossAttack(
                    type = BossAttackType.EARTH_SLAM,
                    name = "Golpe Sísmico",
                    telegraphDuration = 1.5f,
                    cooldown = 7.0f,
                    damage = 2,
                    range = 0.25f,
                    isAoE = true,
                    vfxColor = 0xFFEA580C.toInt(),
                    telegraphWarningMessage = "⚠️ ATENÇÃO: Terremoto Iminente! Golem preparando Golpe Sísmico!"
                )
            )
        )

        fun getShadowQueen(): BossConfig = BossConfig(
            id = "boss_shadow_queen",
            type = BossType.SHADOW_QUEEN,
            name = "Rainha das Sombras",
            title = "Soberana do Eclipse",
            description = "Feiticeira espectral com domínio abissal. Alterna entre evocar o Manto das Sombras (75% de redução de dano) e invocar criaturas das trevas. Fica vulnerável após a queda do manto.",
            maxHealth = 750,
            moveSpeed = 0.13f,
            baseDamageOnEscape = 4,
            coinReward = GameConstants.BOSS_SHADOW_QUEEN_COIN_REWARD,
            introDuration = 2.5f,
            protectionDamageMultiplier = 0.25f,
            protectionDuration = 2.5f,
            vulnerabilityDamageMultiplier = 1.30f,
            vulnerabilityDuration = 2.5f,
            introCinematicMessage = "A luz se desvanece! A Rainha das Sombras surge envolta em névoa abissal!",
            primaryColor = 0xFF7C3AED.toInt(), // Shadow violet
            secondaryColor = 0xFF0F172A.toInt(), // Abyssal black
            attacks = listOf(
                BossAttack(
                    type = BossAttackType.SHADOW_VEIL,
                    name = "Manto das Sombras",
                    telegraphDuration = 1.2f,
                    cooldown = 8.0f,
                    damage = 0,
                    range = 0.20f,
                    isAoE = false,
                    vfxColor = 0xFF8B5CF6.toInt(),
                    telegraphWarningMessage = "⚠️ ATENÇÃO: Rainha invocando Manto das Sombras! Evasão ativada!"
                ),
                BossAttack(
                    type = BossAttackType.SUMMON_MINIONS,
                    name = "Invocar Sombras",
                    telegraphDuration = 1.5f,
                    cooldown = 10.0f,
                    damage = 0,
                    range = 0.25f,
                    isAoE = true,
                    vfxColor = 0xFF6D28D9.toInt(),
                    telegraphWarningMessage = "⚠️ ATENÇÃO: Rituais sombrios convocando criaturas no caminho!"
                )
            )
        )

        fun getAstralDragon(): BossConfig = BossConfig(
            id = "boss_astral_dragon",
            type = BossType.ASTRAL_DRAGON,
            name = "Dragão Astral",
            title = "Tirano Cósmico",
            description = "Draco cósmico ancestral envolto em poeira estelar. Ao atingir 50% de vida, entra na Fase 2 em Fúria Cósmica (+40% de velocidade) e desbloqueia Chuva de Estrelas.",
            maxHealth = 1200,
            moveSpeed = 0.10f,
            baseDamageOnEscape = 6,
            coinReward = GameConstants.BOSS_ASTRAL_DRAGON_COIN_REWARD,
            introDuration = 2.5f,
            vulnerabilityDamageMultiplier = 1.20f,
            introCinematicMessage = "As constelações se curvam! O Tirano Cósmico desce sobre o Pico do Dragão!",
            primaryColor = 0xFF06B6D4.toInt(), // Cosmic cyan
            secondaryColor = 0xFF8B5CF6.toInt(), // Astral purple
            attacks = listOf(
                BossAttack(
                    type = BossAttackType.ASTRAL_BREATH,
                    name = "Sopro Cósmico",
                    telegraphDuration = 1.5f,
                    cooldown = 6.0f,
                    damage = 2,
                    range = 0.30f,
                    isAoE = true,
                    vfxColor = 0xFF06B6D4.toInt(),
                    telegraphWarningMessage = "⚠️ ATENÇÃO: Sopro Cósmico concentrando energia estelar!"
                ),
                BossAttack(
                    type = BossAttackType.STARFALL,
                    name = "Chuva de Estrelas",
                    telegraphDuration = 1.8f,
                    cooldown = 9.0f,
                    damage = 3,
                    range = 0.35f,
                    isAoE = true,
                    vfxColor = 0xFFF43F5E.toInt(),
                    telegraphWarningMessage = "⚠️ PERIGO EXTREMO: Chuva de Estrelas caindo sobre as defesas!"
                )
            )
        )

        fun getBossForStage(stageNumber: Int): BossConfig? = when (stageNumber) {
            2 -> getObsidianGolem()
            3 -> getShadowQueen()
            6 -> getAstralDragon()
            else -> null
        }
    }
}
