package com.heroes.towerdefense.model.stage

import com.heroes.towerdefense.core.GameConstants

/**
 * Representation of a campaign stage in Heroes Tower Defense.
 */
data class StageInfo(
    val stageNumber: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val totalWaves: Int,
    val startingGold: Int,
    val startingLives: Int,
    val isUnlocked: Boolean = false,
    var starsEarned: Int = 0
) {
    init {
        require(stageNumber >= 1) { "Stage number must be positive" }
        require(totalWaves > 0) { "Total waves must be greater than zero" }
        require(startingGold >= 0) { "Starting gold cannot be negative" }
        require(startingLives > 0) { "Starting lives must be greater than zero" }
        require(starsEarned in 0..3) { "Stars earned must be between 0 and 3" }
    }

    companion object {
        fun getStageOne(): StageInfo {
            return StageInfo(
                stageNumber = 1,
                title = "PRIMEIRA FASE",
                subtitle = "O Desfiladeiro dos Goblins",
                description = "Posto avançado guardando os desfiladeiros montanhosos contra as primeiras investidas dos clãs goblins.",
                totalWaves = GameConstants.STAGE_ONE_TOTAL_WAVES,
                startingGold = 250,
                startingLives = 20,
                isUnlocked = true,
                starsEarned = 0
            )
        }

        /**
         * Returns the full campaign stages list for the stages screen.
         * Stage 1 is available; stages 2 through 6 are locked in this stage.
         */
        fun getCampaignStages(): List<StageInfo> = listOf(
            StageInfo(
                stageNumber = 1,
                title = "Fase 1: O Desfiladeiro dos Goblins",
                subtitle = "Entrada das Terras Altas",
                description = "Posto avançado guardando os desfiladeiros montanhosos contra as primeiras investidas dos clãs goblins.",
                totalWaves = GameConstants.STAGE_ONE_TOTAL_WAVES,
                startingGold = 250,
                startingLives = 20,
                isUnlocked = true,
                starsEarned = 0
            ),
            StageInfo(
                stageNumber = 2,
                title = "Fase 2: Floresta dos Sussurros",
                subtitle = "Vegetação Densa e Emboscadas",
                description = "Trilha silvestre com rotas sinuosas e visibilidade reduzida por névoa élfica.",
                totalWaves = 12,
                startingGold = 300,
                startingLives = 20,
                isUnlocked = false,
                starsEarned = 0
            ),
            StageInfo(
                stageNumber = 3,
                title = "Fase 3: Ruínas Ancestrais",
                subtitle = "Santuário Abandonado",
                description = "Templos em ruínas infestados por xamãs e criaturas corrompidas.",
                totalWaves = 15,
                startingGold = 350,
                startingLives = 15,
                isUnlocked = false,
                starsEarned = 0
            ),
            StageInfo(
                stageNumber = 4,
                title = "Fase 4: Cavernas de Cristal",
                subtitle = "Subsolo Escuro",
                description = "Labirinto subterrâneo com rotas duplas de ataque e estalactites.",
                totalWaves = 15,
                startingGold = 400,
                startingLives = 15,
                isUnlocked = false,
                starsEarned = 0
            ),
            StageInfo(
                stageNumber = 5,
                title = "Fase 5: Fortaleza Vulcânica",
                subtitle = "Muralhas da Caldeira",
                description = "Ambiente hostil com rios de magma e hordas orcs de alta blindagem.",
                totalWaves = 18,
                startingGold = 450,
                startingLives = 10,
                isUnlocked = false,
                starsEarned = 0
            ),
            StageInfo(
                stageNumber = 6,
                title = "Fase 6: Pico do Dragão",
                subtitle = "Covil do Tirano Alado",
                description = "Batalha decisiva contra as hostes do grande dragão vermelho.",
                totalWaves = 20,
                startingGold = 500,
                startingLives = 10,
                isUnlocked = false,
                starsEarned = 0
            )
        )
    }
}
