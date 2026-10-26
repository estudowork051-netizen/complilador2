package com.heroes.towerdefense.model.hero

/**
 * 5 Rarity tiers for Heroes Tower Defense (Part 5).
 * COMMON, RARE, EPIC, LEGENDARY, MYTHIC.
 * Each tier has visual identity (display name, color hex/int) and stat multipliers.
 * Uses pure integer colors (0xFF...toInt()) to remain decoupled from Android graphics mocks.
 */
enum class HeroRarity(
    val displayName: String,
    val colorHex: String,
    val colorInt: Int,
    val statMultiplier: Float
) {
    COMMON("Comum", "#94A3B8", 0xFF94A3B8.toInt(), 1.00f),
    RARE("Raro", "#3B82F6", 0xFF3B82F6.toInt(), 1.25f),
    EPIC("Épico", "#A855F7", 0xFFA855F7.toInt(), 1.50f),
    LEGENDARY("Lendário", "#F59E0B", 0xFFF59E0B.toInt(), 1.85f),
    MYTHIC("Mítico", "#EF4444", 0xFFEF4444.toInt(), 2.25f);

    val badgeLabel: String
        get() = displayName.uppercase()
}
