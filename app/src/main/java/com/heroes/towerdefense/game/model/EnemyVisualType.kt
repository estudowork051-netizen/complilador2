package com.heroes.towerdefense.game.model

private fun parseHex(hex: String): Int {
    val clean = hex.removePrefix("#")
    val fullHex = if (clean.length == 6) "FF" + clean else clean
    return fullHex.toLong(16).toInt()
}

/**
 * Visual identities for the 4 canonical common enemy archetypes and 3 epic bosses
 * in Heroes Tower Defense (Visual Upgrade Part 2).
 */
enum class EnemyVisualType(
    val displayName: String,
    val title: String,
    val isBoss: Boolean,
    val primaryColorInt: Int,
    val secondaryColorInt: Int,
    val accentColorInt: Int
) {
    GOBLIN(
        displayName = "Goblin",
        title = "Saqueador Selvagem",
        isBoss = false,
        primaryColorInt = parseHex("#16A34A"), // Goblin green
        secondaryColorInt = parseHex("#78350F"), // Worn leather
        accentColorInt = parseHex("#FACC15") // Fiery yellow eyes
    ),
    SKELETON(
        displayName = "Esqueleto",
        title = "Guerreiro Amaldiçoado",
        isBoss = false,
        primaryColorInt = parseHex("#E2E8F0"), // Bone ivory
        secondaryColorInt = parseHex("#475569"), // Rusted iron
        accentColorInt = parseHex("#06B6D4") // Ethereal cyan soul glow
    ),
    ORC(
        displayName = "Orc",
        title = "Brutamontes de Guerra",
        isBoss = false,
        primaryColorInt = parseHex("#15803D"), // Deep moss green
        secondaryColorInt = parseHex("#1E293B"), // Heavy dark iron
        accentColorInt = parseHex("#DC2626") // Crimson war paint
    ),
    DARK_MAGE(
        displayName = "Mago Sombrio",
        title = "Conjurador das Trevas",
        isBoss = false,
        primaryColorInt = parseHex("#6B21A8"), // Royal dark violet
        secondaryColorInt = parseHex("#1E1B4B"), // Midnight cowl
        accentColorInt = parseHex("#C084FC") // Glowing arcane violet
    ),
    OBSIDIAN_GOLEM(
        displayName = "Golem de Obsidiana",
        title = "Colosso Vulcânico",
        isBoss = true,
        primaryColorInt = parseHex("#EA580C"), // Magma core
        secondaryColorInt = parseHex("#1E293B"), // Basalt rock
        accentColorInt = parseHex("#FDE047") // Blazing fissures
    ),
    SHADOW_QUEEN(
        displayName = "Rainha das Sombras",
        title = "Soberana das Sombras",
        isBoss = true,
        primaryColorInt = parseHex("#8B5CF6"), // Shadow violet
        secondaryColorInt = parseHex("#0F172A"), // Void armor
        accentColorInt = parseHex("#F59E0B") // Dark gold crown
    ),
    ASTRAL_DRAGON(
        displayName = "Dragão Astral",
        title = "Dragão Ancestral Cósmico",
        isBoss = true,
        primaryColorInt = parseHex("#0284C7"), // Astral starlight
        secondaryColorInt = parseHex("#0B132B"), // Midnight scales
        accentColorInt = parseHex("#E0F2FE") // Nova constellation
    );
}
