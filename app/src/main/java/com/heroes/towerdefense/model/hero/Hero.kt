package com.heroes.towerdefense.model.hero

/**
 * Representation of a deployable Fantasy Hero unit.
 * Extended in Part 5 with Rarity tiers, 2 unique active skills per hero,
 * and scaled progression formulas.
 */
data class Hero(
    val id: String,
    val name: String,
    val heroClass: HeroClass,
    val baseHealth: Int,
    val attackPower: Int,
    val attackRange: Float,
    val attackSpeed: Float, // Attacks per second
    val skillName: String = "",
    var level: Int = 1,
    val title: String = "Herói do Reino",
    val rarity: HeroRarity = HeroRarity.COMMON,
    val skills: List<HeroSkill> = emptyList()
) {
    // Secondary constructor preserving Part 2 compatibility
    constructor(id: String, name: String, heroClass: HeroClass, title: String) : this(
        id = id,
        name = name,
        heroClass = heroClass,
        baseHealth = 500,
        attackPower = 35,
        attackRange = 1.5f,
        attackSpeed = 1.0f,
        skillName = "Em Desenvolvimento",
        level = 1,
        title = title,
        rarity = HeroRarity.COMMON,
        skills = emptyList()
    )

    init {
        require(baseHealth > 0) { "Base health must be greater than zero" }
        require(attackPower > 0) { "Attack power must be greater than zero" }
        require(attackRange > 0f) { "Attack range must be greater than zero" }
        require(attackSpeed > 0f) { "Attack speed must be greater than zero" }
        require(level >= 1) { "Level must be at least 1" }
    }

    /**
     * Calculates the damage per second (DPS) of the hero, scaled by level and rarity.
     */
    fun calculateDps(): Float = attackPower * attackSpeed * (1f + (level - 1) * 0.15f) * rarity.statMultiplier

    /**
     * Calculates scaled health based on level and rarity.
     */
    fun calculateHealth(): Int = (baseHealth * (1f + (level - 1) * 0.12f) * rarity.statMultiplier).toInt()

    /**
     * Calculates scaled attack power based on level and rarity.
     */
    fun calculateAttackPower(): Int = (attackPower * (1f + (level - 1) * 0.15f) * rarity.statMultiplier).toInt()

    /**
     * Upgrades the hero level.
     */
    fun levelUp() {
        level += 1
    }

    companion object {
        /**
         * Returns the 6 canonical heroes for Heroes Tower Defense,
         * each endowed with unique class, rarity tier, attributes, and 2 unique functional active skills.
         */
        fun getPlannedHeroes(): List<Hero> = listOf(
            // 1. Kael (Warrior / Fire Vanguard) - COMMON
            Hero(
                id = "hero_kael",
                name = "Kael",
                heroClass = HeroClass.WARRIOR,
                baseHealth = 550,
                attackPower = 45,
                attackRange = 0.22f,
                attackSpeed = 1.2f,
                skillName = "Golpe Flamejante",
                level = 1,
                title = "Cavaleiro do Fogo",
                rarity = HeroRarity.COMMON,
                skills = listOf(
                    HeroSkill(
                        id = "kael_skill_1",
                        name = "Golpe Flamejante",
                        description = "Golpe concentrado com lâmina em chamas que causa alto dano ígneo a um invasor.",
                        cooldownSeconds = 6.0f,
                        damage = 120,
                        range = 0.25f,
                        targetType = SkillTargetType.SINGLE_TARGET,
                        vfxColor = 0xFFF97316.toInt()
                    ),
                    HeroSkill(
                        id = "kael_skill_2",
                        name = "Onda de Fogo",
                        description = "Libera um anel ardente expansivo causando dano ígneo em área a todos os inimigos próximos.",
                        cooldownSeconds = 12.0f,
                        damage = 80,
                        splashRadius = 0.22f,
                        targetType = SkillTargetType.AREA_OF_EFFECT,
                        vfxColor = 0xFFEF4444.toInt()
                    )
                )
            ),
            // 2. Lyra (Archer / Arcane Precision) - RARE
            Hero(
                id = "hero_lyra",
                name = "Lyra",
                heroClass = HeroClass.ARCHER,
                baseHealth = 420,
                attackPower = 40,
                attackRange = 0.32f,
                attackSpeed = 1.5f,
                skillName = "Tiro de Precisão",
                level = 1,
                title = "Sentinela dos Bosques",
                rarity = HeroRarity.RARE,
                skills = listOf(
                    HeroSkill(
                        id = "lyra_skill_1",
                        name = "Tiro de Precisão",
                        description = "Dispara uma flecha encantada veloz perfurante com alto dano ao invasor mais avançado.",
                        cooldownSeconds = 7.0f,
                        damage = 140,
                        range = 0.38f,
                        targetType = SkillTargetType.SINGLE_TARGET,
                        vfxColor = 0xFFF59E0B.toInt()
                    ),
                    HeroSkill(
                        id = "lyra_skill_2",
                        name = "Chuva de Flechas",
                        description = "Faz chover uma saraivada de flechas místicas em uma zona da trilha, punindo múltiplos goblins.",
                        cooldownSeconds = 14.0f,
                        damage = 95,
                        splashRadius = 0.25f,
                        targetType = SkillTargetType.AREA_OF_EFFECT,
                        vfxColor = 0xFF10B981.toInt()
                    )
                )
            ),
            // 3. Brakk (Warrior / Mountain Defender) - EPIC
            Hero(
                id = "hero_brakk",
                name = "Brakk",
                heroClass = HeroClass.WARRIOR,
                baseHealth = 750,
                attackPower = 50,
                attackRange = 0.20f,
                attackSpeed = 0.9f,
                skillName = "Pancada Terrestre",
                level = 1,
                title = "Guardião das Montanhas",
                rarity = HeroRarity.EPIC,
                skills = listOf(
                    HeroSkill(
                        id = "brakk_skill_1",
                        name = "Pancada Terrestre",
                        description = "Bate fortemente com a maça no solo gerando uma onda de choque sísmica em área.",
                        cooldownSeconds = 8.0f,
                        damage = 110,
                        splashRadius = 0.20f,
                        targetType = SkillTargetType.AREA_OF_EFFECT,
                        vfxColor = 0xFF78716C.toInt()
                    ),
                    HeroSkill(
                        id = "brakk_skill_2",
                        name = "Fortificação Protetora",
                        description = "Ergue escudos pétreos ancestrais, restaurando integridade da fortaleza e repelindo invasores.",
                        cooldownSeconds = 18.0f,
                        damage = 50,
                        splashRadius = 0.18f,
                        targetType = SkillTargetType.SELF_BUFF,
                        vfxColor = 0xFFEAB308.toInt()
                    )
                )
            ),
            // 4. Nyra (Mage / Shadow Weaver) - EPIC
            Hero(
                id = "hero_nyra",
                name = "Nyra",
                heroClass = HeroClass.MAGE,
                baseHealth = 400,
                attackPower = 60,
                attackRange = 0.24f,
                attackSpeed = 1.4f,
                skillName = "Ataque Sombrio",
                level = 1,
                title = "Arquimaga da Tempestade",
                rarity = HeroRarity.EPIC,
                skills = listOf(
                    HeroSkill(
                        id = "nyra_skill_1",
                        name = "Ataque Sombrio",
                        description = "Desfere um golpe sombrio súbito que atinge o invasor prioritário com dano fulminante.",
                        cooldownSeconds = 6.5f,
                        damage = 170,
                        range = 0.30f,
                        targetType = SkillTargetType.SINGLE_TARGET,
                        vfxColor = 0xFF7E22CE.toInt()
                    ),
                    HeroSkill(
                        id = "nyra_skill_2",
                        name = "Dança das Lâminas",
                        description = "Invoca adagas de energia sombria rotativa que fustigam todos os inimigos no raio de ação.",
                        cooldownSeconds = 11.0f,
                        damage = 115,
                        splashRadius = 0.20f,
                        targetType = SkillTargetType.AREA_OF_EFFECT,
                        vfxColor = 0xFFA855F7.toInt()
                    )
                )
            ),
            // 5. Tiko (Alchemist / Traps & Ordnance) - LEGENDARY
            Hero(
                id = "hero_tiko",
                name = "Tiko",
                heroClass = HeroClass.ALCHEMIST,
                baseHealth = 460,
                attackPower = 42,
                attackRange = 0.26f,
                attackSpeed = 1.1f,
                skillName = "Mina de Fragmentação",
                level = 1,
                title = "Artífice Alquimista",
                rarity = HeroRarity.LEGENDARY,
                skills = listOf(
                    HeroSkill(
                        id = "tiko_skill_1",
                        name = "Mina de Fragmentação",
                        description = "Detona um artefato de estilhaços mecânicos atingindo múltiplos invasores no caminho.",
                        cooldownSeconds = 8.0f,
                        damage = 130,
                        splashRadius = 0.20f,
                        targetType = SkillTargetType.AREA_OF_EFFECT,
                        vfxColor = 0xFFEA580C.toInt()
                    ),
                    HeroSkill(
                        id = "tiko_skill_2",
                        name = "Engenho Explosivo",
                        description = "Arremessa um complexo incendiário de alta potência causando explosão devastadora em grande área.",
                        cooldownSeconds = 15.0f,
                        damage = 160,
                        splashRadius = 0.28f,
                        targetType = SkillTargetType.AREA_OF_EFFECT,
                        vfxColor = 0xFFDC2626.toInt()
                    )
                )
            ),
            // 6. Vesper (Assassin / Astral Hunter) - MYTHIC
            Hero(
                id = "hero_vesper",
                name = "Vesper",
                heroClass = HeroClass.ASSASSIN,
                baseHealth = 440,
                attackPower = 65,
                attackRange = 0.35f,
                attackSpeed = 1.0f,
                skillName = "Cometa Astral",
                level = 1,
                title = "Caçadora Espectral",
                rarity = HeroRarity.MYTHIC,
                skills = listOf(
                    HeroSkill(
                        id = "vesper_skill_1",
                        name = "Cometa Astral",
                        description = "Canaliza um projétil cósmico veloz de matéria estelar causando destruição maciça a um alvo.",
                        cooldownSeconds = 7.5f,
                        damage = 190,
                        range = 0.36f,
                        targetType = SkillTargetType.SINGLE_TARGET,
                        vfxColor = 0xFF06B6D4.toInt()
                    ),
                    HeroSkill(
                        id = "vesper_skill_2",
                        name = "Singularidade Cósmica",
                        description = "Abre um vórtice estelar supermassivo que aprisiona e consome toda a horda de goblins na área.",
                        cooldownSeconds = 16.0f,
                        damage = 150,
                        splashRadius = 0.26f,
                        targetType = SkillTargetType.AREA_OF_EFFECT,
                        vfxColor = 0xFF3B82F6.toInt()
                    )
                )
            )
        )

        /**
         * Convenience accessor for Kael (Hero 1).
         */
        fun getKael(): Hero = getPlannedHeroes().first { it.id == "hero_kael" }
    }
}

enum class HeroClass(val displayName: String = "") {
    WARRIOR("Guerreiro"),
    ARCHER("Arqueiro"),
    MAGE("Mago"),
    PALADIN("Paladino"),
    ALCHEMIST("Alquimista"),
    ASSASSIN("Assassino"),
    GUARDIAN("Guardião"),
    ENGINEER("Engenheiro")
}
