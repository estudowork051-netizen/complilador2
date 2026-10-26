package com.heroes.towerdefense.game.map

import com.heroes.towerdefense.game.model.AmbientParticleType

private fun parseHex(hex: String): Int {
    val clean = hex.removePrefix("#")
    val fullHex = if (clean.length == 6) "FF" + clean else clean
    return fullHex.toLong(16).toInt()
}

/**
 * Visual environment theme configuration for each of the 6 campaign stages in Heroes Tower Defense.
 * Provides custom color palettes for terrain, stone paths, cobblestones,
 * vegetation, props, atmospheric lighting, and ambient particle systems.
 */
enum class EnvironmentTheme(
    val stageNumber: Int,
    val themeName: String,
    val subtitle: String,
    // Terrain Base & Accents
    val groundBaseColor: Int,
    val groundAccentColor: Int,
    val groundGridColor: Int,
    // Road & Path
    val pathSurfaceColor: Int,
    val pathBorderColor: Int,
    val cobblestoneColor: Int,
    val cobblestoneHighlightColor: Int,
    // Water / Lava / Hazard
    val hazardOrWaterColor: Int,
    val hazardGlowColor: Int,
    val hasHazardLiquid: Boolean,
    // Foliage & Props
    val foliagePrimaryColor: Int,
    val foliageSecondaryColor: Int,
    val trunkWoodColor: Int,
    val rockColor: Int,
    val rockHighlightColor: Int,
    // Lighting & Atmosphere
    val torchLightColor: Int,
    val ambientVignetteColor: Int,
    val ambientParticleColor: Int,
    val ambientParticleSecondaryColor: Int,
    val ambientParticleType: AmbientParticleType,
    val castleWallColor: Int,
    val castleTrimColor: Int,
    // 3D Environmental Lighting & Atmosphere
    val skyTopColor: Int = parseHex("#4073AF"),
    val skyHorizonColor: Int = parseHex("#A6C0D9"),
    val sunLightColor: Int = parseHex("#FFF5E6"),
    val sunEnergy: Float = 1.35f,
    val fogColor: Int = parseHex("#A6C0D9"),
    val fogDensity: Float = 0.005f,
    val ambientEnergy: Float = 0.45f
) {
    HIGHLAND_FOREST(
        stageNumber = 1,
        themeName = "O Desfiladeiro dos Goblins",
        subtitle = "Entrada das Terras Altas",
        groundBaseColor = parseHex("#1B3320"), // Lush forest green
        groundAccentColor = parseHex("#23442B"),
        groundGridColor = parseHex("#16281A"),
        pathSurfaceColor = parseHex("#8C6E4A"), // Warm dirt road
        pathBorderColor = parseHex("#5A442E"),
        cobblestoneColor = parseHex("#9A7B56"),
        cobblestoneHighlightColor = parseHex("#B4936B"),
        hazardOrWaterColor = parseHex("#2563EB"), // Crystal river
        hazardGlowColor = parseHex("#60A5FA"),
        hasHazardLiquid = true,
        foliagePrimaryColor = parseHex("#15803D"),
        foliageSecondaryColor = parseHex("#166534"),
        trunkWoodColor = parseHex("#78350F"),
        rockColor = parseHex("#475569"),
        rockHighlightColor = parseHex("#64748B"),
        torchLightColor = parseHex("#FBBF24"), // Warm golden torch
        ambientVignetteColor = parseHex("#220A1A0D"),
        ambientParticleColor = parseHex("#FDE68A"), // Golden pollen
        ambientParticleSecondaryColor = parseHex("#86EFAC"),
        ambientParticleType = AmbientParticleType.POLLEN,
        castleWallColor = parseHex("#1E293B"),
        castleTrimColor = parseHex("#F59E0B"),
        skyTopColor = parseHex("#4073AF"),
        skyHorizonColor = parseHex("#A6C0D9"),
        sunLightColor = parseHex("#FFF5E6"),
        sunEnergy = 1.35f,
        fogColor = parseHex("#A6C0D9"),
        fogDensity = 0.005f,
        ambientEnergy = 0.45f
    ),

    WHISPERING_WOODS(
        stageNumber = 2,
        themeName = "Floresta dos Sussurros",
        subtitle = "Vegetação Densa e Emboscadas",
        groundBaseColor = parseHex("#0F291E"), // Deep emerald mist
        groundAccentColor = parseHex("#16382B"),
        groundGridColor = parseHex("#0A1E16"),
        pathSurfaceColor = parseHex("#6B5B43"),
        pathBorderColor = parseHex("#4A3F2E"),
        cobblestoneColor = parseHex("#786950"),
        cobblestoneHighlightColor = parseHex("#8D7C61"),
        hazardOrWaterColor = parseHex("#0D9488"), // Misty stream
        hazardGlowColor = parseHex("#2DD4BF"),
        hasHazardLiquid = true,
        foliagePrimaryColor = parseHex("#047857"),
        foliageSecondaryColor = parseHex("#065F46"),
        trunkWoodColor = parseHex("#5C3D2E"),
        rockColor = parseHex("#334155"),
        rockHighlightColor = parseHex("#475569"),
        torchLightColor = parseHex("#67E8F9"), // Ethereal cyan lanterns
        ambientVignetteColor = parseHex("#33041F16"),
        ambientParticleColor = parseHex("#67E8F9"), // Cyan fireflies
        ambientParticleSecondaryColor = parseHex("#A7F3D0"),
        ambientParticleType = AmbientParticleType.FIREFLIES,
        castleWallColor = parseHex("#0A231C"), // Mossy granite wall
        castleTrimColor = parseHex("#10B981"),
        skyTopColor = parseHex("#143833"),
        skyHorizonColor = parseHex("#2E7366"),
        sunLightColor = parseHex("#D9FAEB"),
        sunEnergy = 1.10f,
        fogColor = parseHex("#266659"),
        fogDensity = 0.012f,
        ambientEnergy = 0.38f
    ),

    ANCIENT_RUINS(
        stageNumber = 3,
        themeName = "Ruínas Ancestrais",
        subtitle = "Santuário Abandonado",
        groundBaseColor = parseHex("#1E242B"), // Ancient granite slabs
        groundAccentColor = parseHex("#28303A"),
        groundGridColor = parseHex("#15191E"),
        pathSurfaceColor = parseHex("#475569"), // Paved stone road
        pathBorderColor = parseHex("#334155"),
        cobblestoneColor = parseHex("#64748B"),
        cobblestoneHighlightColor = parseHex("#94A3B8"),
        hazardOrWaterColor = parseHex("#4C1D95"), // Void energy pool
        hazardGlowColor = parseHex("#8B5CF6"),
        hasHazardLiquid = true,
        foliagePrimaryColor = parseHex("#334155"),
        foliageSecondaryColor = parseHex("#1E293B"),
        trunkWoodColor = parseHex("#475569"),
        rockColor = parseHex("#64748B"),
        rockHighlightColor = parseHex("#94A3B8"),
        torchLightColor = parseHex("#C084FC"), // Violet witchfire
        ambientVignetteColor = parseHex("#331A122E"),
        ambientParticleColor = parseHex("#C084FC"), // Spectral mist
        ambientParticleSecondaryColor = parseHex("#E9D5FF"),
        ambientParticleType = AmbientParticleType.SPECTRAL_MIST,
        castleWallColor = parseHex("#1E1B4B"),
        castleTrimColor = parseHex("#8B5CF6"),
        skyTopColor = parseHex("#261F47"),
        skyHorizonColor = parseHex("#614085"),
        sunLightColor = parseHex("#EBD9FF"),
        sunEnergy = 1.15f,
        fogColor = parseHex("#472E6B"),
        fogDensity = 0.009f,
        ambientEnergy = 0.40f
    ),

    CRYSTAL_CAVERNS(
        stageNumber = 4,
        themeName = "Cavernas de Cristal",
        subtitle = "Subsolo Escuro",
        groundBaseColor = parseHex("#0F172A"), // Deep subterranean cavern floor
        groundAccentColor = parseHex("#1E293B"),
        groundGridColor = parseHex("#080D1A"),
        pathSurfaceColor = parseHex("#334155"),
        pathBorderColor = parseHex("#1E293B"),
        cobblestoneColor = parseHex("#475569"),
        cobblestoneHighlightColor = parseHex("#64748B"),
        hazardOrWaterColor = parseHex("#0284C7"), // Luminous subterranean pool
        hazardGlowColor = parseHex("#38BDF8"),
        hasHazardLiquid = true,
        foliagePrimaryColor = parseHex("#0284C7"), // Crystal clusters instead of foliage
        foliageSecondaryColor = parseHex("#0369A1"),
        trunkWoodColor = parseHex("#1E293B"),
        rockColor = parseHex("#1E293B"), // Subterranean dark basalt
        rockHighlightColor = parseHex("#38BDF8"),
        torchLightColor = parseHex("#38BDF8"), // Arcane crystal glow
        ambientVignetteColor = parseHex("#44040A1A"),
        ambientParticleColor = parseHex("#38BDF8"), // Glistening crystal dust
        ambientParticleSecondaryColor = parseHex("#E0E7FF"),
        ambientParticleType = AmbientParticleType.CRYSTAL_DUST,
        castleWallColor = parseHex("#0C4A6E"), // Subterranean sapphire stronghold
        castleTrimColor = parseHex("#38BDF8"),
        skyTopColor = parseHex("#080D1F"),
        skyHorizonColor = parseHex("#0F2447"),
        sunLightColor = parseHex("#A6E0FF"),
        sunEnergy = 0.95f,
        fogColor = parseHex("#0D1F40"),
        fogDensity = 0.015f,
        ambientEnergy = 0.35f
    ),

    VOLCANIC_FORTRESS(
        stageNumber = 5,
        themeName = "Fortaleza Vulcânica",
        subtitle = "Muralhas da Caldeira",
        groundBaseColor = parseHex("#1C100F"), // Scorched obsidian basalt
        groundAccentColor = parseHex("#2A1614"),
        groundGridColor = parseHex("#140A09"),
        pathSurfaceColor = parseHex("#3F2623"),
        pathBorderColor = parseHex("#2A1614"),
        cobblestoneColor = parseHex("#5A3732"),
        cobblestoneHighlightColor = parseHex("#774943"),
        hazardOrWaterColor = parseHex("#DC2626"), // Molten lava river
        hazardGlowColor = parseHex("#F97316"),
        hasHazardLiquid = true,
        foliagePrimaryColor = parseHex("#451A03"), // Charred deadwood
        foliageSecondaryColor = parseHex("#270F04"),
        trunkWoodColor = parseHex("#1C1917"),
        rockColor = parseHex("#292524"),
        rockHighlightColor = parseHex("#EA580C"), // Magma veins in rocks
        torchLightColor = parseHex("#F97316"), // Blazing iron braziers
        ambientVignetteColor = parseHex("#441F0A06"),
        ambientParticleColor = parseHex("#F97316"), // Rising volcanic embers
        ambientParticleSecondaryColor = parseHex("#FDE047"),
        ambientParticleType = AmbientParticleType.VOLCANIC_EMBERS,
        castleWallColor = parseHex("#1C1917"),
        castleTrimColor = parseHex("#EA580C"),
        skyTopColor = parseHex("#2E0D08"),
        skyHorizonColor = parseHex("#6B260F"),
        sunLightColor = parseHex("#FFBF8C"),
        sunEnergy = 1.25f,
        fogColor = parseHex("#591F0D"),
        fogDensity = 0.011f,
        ambientEnergy = 0.42f
    ),

    ASTRAL_PEAK(
        stageNumber = 6,
        themeName = "Pico do Dragão",
        subtitle = "Dimensão Astral & Covil do Tirano",
        groundBaseColor = parseHex("#0B0F26"), // Cosmic midnight stardust
        groundAccentColor = parseHex("#131A40"),
        groundGridColor = parseHex("#070A1A"),
        pathSurfaceColor = parseHex("#1E1B4B"), // Celestial constellation lane
        pathBorderColor = parseHex("#17133B"),
        cobblestoneColor = parseHex("#312E81"),
        cobblestoneHighlightColor = parseHex("#4338CA"),
        hazardOrWaterColor = parseHex("#7C3AED"), // Cosmic nebula abyss
        hazardGlowColor = parseHex("#C084FC"),
        hasHazardLiquid = true,
        foliagePrimaryColor = parseHex("#38BDF8"), // Floating celestial crystals
        foliageSecondaryColor = parseHex("#1E40AF"),
        trunkWoodColor = parseHex("#1E1B4B"),
        rockColor = parseHex("#1B2236"), // Cosmic meteorite rock
        rockHighlightColor = parseHex("#818CF8"),
        torchLightColor = parseHex("#F43F5E"), // Cosmic solar flare beacons
        ambientVignetteColor = parseHex("#44090526"),
        ambientParticleColor = parseHex("#818CF8"), // Astral stardust
        ambientParticleSecondaryColor = parseHex("#F43F5E"),
        ambientParticleType = AmbientParticleType.ASTRAL_STARDUST,
        castleWallColor = parseHex("#13103A"), // Celestial voidstone citadel
        castleTrimColor = parseHex("#EC4899"), // Radiant celestial rose-gold trim
        skyTopColor = parseHex("#0A0826"),
        skyHorizonColor = parseHex("#381F61"),
        sunLightColor = parseHex("#F2BFFF"),
        sunEnergy = 1.20f,
        fogColor = parseHex("#2E1A52"),
        fogDensity = 0.008f,
        ambientEnergy = 0.44f
    );

    companion object {
        fun fromStageNumber(stageNumber: Int): EnvironmentTheme = when (stageNumber) {
            1 -> HIGHLAND_FOREST
            2 -> WHISPERING_WOODS
            3 -> ANCIENT_RUINS
            4 -> CRYSTAL_CAVERNS
            5 -> VOLCANIC_FORTRESS
            6 -> ASTRAL_PEAK
            else -> HIGHLAND_FOREST
        }
    }
}
