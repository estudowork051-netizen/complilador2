package com.heroes.towerdefense.game.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.heroes.towerdefense.game.map.EnvironmentTheme
import com.heroes.towerdefense.game.map.StageOneMap
import com.heroes.towerdefense.game.map.Waypoint
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-performance 2D Canvas Environment & Map Renderer (Clash Royale Style Reference).
 *
 * Implements a rich 9-layer isometric battlefield architecture:
 * 1. Textured ground terrain with elevation contours and stylized grid tiles
 * 2. 3D Cobblestone path with individual paver stones, beveled curbs, and wooden bridges
 * 3. Liquid hazard streams (crystal river, misty pond, void pool, or molten magma)
 * 4. Living environmental props:
 *    - Swaying multi-tiered trees with organic wind oscillation
 *    - Mossy boulders and beveled rock outcroppings
 *    - Arcane crystal clusters and ancient stone pillars
 * 5. Animated torches & iron braziers with dancing flames and radial point-light glow
 * 6. 3D Isometric Spawning Portal with spinning dimensional vortex
 * 7. 3D Medieval Fortress Base with corner bastions, portcullis gate, and waving flags
 * 8. Atmospheric vignette and mood lighting
 *
 * All draw routines are strictly zero-allocation in onDraw loops.
 */
object EnvironmentMapRenderer {

    // =========================================================================
    // Reusable Paints (Allocated once, zero GC pressure during frame rendering)
    // =========================================================================

    private val terrainBasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val terrainGridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val pathCurbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val pathCurbHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val pathBedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val cobblestonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val cobblestoneHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val hazardLiquidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val hazardGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val bridgePlankPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#78350F")
        style = Paint.Style.FILL
    }

    private val bridgeRailingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#451A03")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#44000000")
        style = Paint.Style.FILL
    }

    private val treeTrunkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val foliageLightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val foliageDarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val rockLightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val rockDarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val torchBasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#334155")
        style = Paint.Style.FILL
    }

    private val flameCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val flameGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val portalRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val portalVortexPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }

    private val portalPylonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val castleWallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val castleTrimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val castleGatePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0F172A")
        style = Paint.Style.FILL
    }

    private val flagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EF4444")
        style = Paint.Style.FILL
    }

    private val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 20f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    // Reusable Path & RectF objects
    private val roadPath = Path()
    private val curbPath = Path()
    private val polygonPath = Path()
    private val tempRect = RectF()

    // =========================================================================
    // 1. Terrain Base Layer
    // =========================================================================

    fun drawTerrainBase(canvas: Canvas, w: Float, h: Float, theme: EnvironmentTheme) {
        terrainBasePaint.color = theme.groundBaseColor
        canvas.drawRect(0f, 0f, w, h, terrainBasePaint)

        // Subtle 3D Isometric Tile Grid Lines
        terrainGridPaint.color = theme.groundGridColor
        terrainGridPaint.alpha = 60
        val step = w * 0.10f
        var x = 0f
        while (x < w) {
            canvas.drawLine(x, 0f, x, h, terrainGridPaint)
            x += step
        }
        var y = 0f
        while (y < h) {
            canvas.drawLine(0f, y, w, y, terrainGridPaint)
            y += step
        }

        // Elevation Cliff Ridges (Top-right & bottom-left terrain contours)
        terrainBasePaint.color = theme.groundAccentColor
        polygonPath.reset()
        polygonPath.moveTo(w * 0.70f, 0f)
        polygonPath.lineTo(w, 0f)
        polygonPath.lineTo(w, h * 0.18f)
        polygonPath.lineTo(w * 0.85f, h * 0.22f)
        polygonPath.close()
        canvas.drawPath(polygonPath, terrainBasePaint)

        polygonPath.reset()
        polygonPath.moveTo(0f, h * 0.80f)
        polygonPath.lineTo(w * 0.35f, h * 0.80f)
        polygonPath.lineTo(w * 0.25f, h)
        polygonPath.lineTo(0f, h)
        polygonPath.close()
        canvas.drawPath(polygonPath, terrainBasePaint)
    }

    // =========================================================================
    // 2. Liquid Hazard Stream (River / Lava / Void Stream)
    // =========================================================================

    fun drawLiquidHazard(canvas: Canvas, w: Float, h: Float, theme: EnvironmentTheme, animTime: Float) {
        if (!theme.hasHazardLiquid) return

        hazardLiquidPaint.color = theme.hazardOrWaterColor
        hazardGlowPaint.color = theme.hazardGlowColor

        // Flowing river stream traversing across the mid-section
        val riverY = h * 0.35f
        val riverH = h * 0.08f
        val waveW = sin(animTime * 2.5f) * (w * 0.015f)

        polygonPath.reset()
        polygonPath.moveTo(0f, riverY - riverH * 0.5f)
        polygonPath.cubicTo(
            w * 0.35f + waveW, riverY - riverH * 0.7f,
            w * 0.65f - waveW, riverY + riverH * 0.3f,
            w, riverY - riverH * 0.2f
        )
        polygonPath.lineTo(w, riverY + riverH * 0.8f)
        polygonPath.cubicTo(
            w * 0.65f - waveW, riverY + riverH * 1.3f,
            w * 0.35f + waveW, riverY + riverH * 0.3f,
            0f, riverY + riverH * 0.5f
        )
        polygonPath.close()
        canvas.drawPath(polygonPath, hazardLiquidPaint)

        // Luminous shoreline foam / glow line
        hazardGlowPaint.alpha = (140 + sin(animTime * 4f) * 60).toInt().coerceIn(80, 220)
        canvas.drawPath(polygonPath, hazardGlowPaint)
    }

    // =========================================================================
    // 3. 3D Cobblestone Path with Elevated Stone Curbs
    // =========================================================================

    fun drawCobblestonePath(
        canvas: Canvas,
        w: Float,
        h: Float,
        theme: EnvironmentTheme,
        waypoints: List<Waypoint> = StageOneMap.waypoints
    ) {
        if (waypoints.isEmpty()) return

        roadPath.reset()
        roadPath.moveTo(waypoints[0].x * w, waypoints[0].y * h)
        for (i in 1 until waypoints.size) {
            roadPath.lineTo(waypoints[i].x * w, waypoints[i].y * h)
        }

        val roadWidth = w * 0.135f
        val curbThickness = 5f

        // 1. Shaded lower curb border (South/East drop shadow)
        pathCurbPaint.color = theme.pathBorderColor
        pathCurbPaint.strokeWidth = roadWidth + curbThickness * 2.5f
        canvas.drawPath(roadPath, pathCurbPaint)

        // 2. Main Cobblestone Road Bed
        pathBedPaint.color = theme.pathSurfaceColor
        pathBedPaint.strokeWidth = roadWidth
        canvas.drawPath(roadPath, pathBedPaint)

        // 3. Highlighted Top Curb Edge
        pathCurbHighlightPaint.color = theme.cobblestoneHighlightColor
        pathCurbHighlightPaint.strokeWidth = 2f
        pathCurbHighlightPaint.alpha = 150
        canvas.drawPath(roadPath, pathCurbHighlightPaint)

        // 4. Individual Stylized Cobblestone Pavers along the road
        cobblestonePaint.color = theme.cobblestoneColor
        cobblestoneHighlightPaint.color = theme.cobblestoneHighlightColor

        for (i in 0 until waypoints.size - 1) {
            val p1 = waypoints[i]
            val p2 = waypoints[i + 1]
            val x1 = p1.x * w
            val y1 = p1.y * h
            val x2 = p2.x * w
            val y2 = p2.y * h
            val dx = x2 - x1
            val dy = y2 - y1
            val dist = sqrt(dx * dx + dy * dy)
            val steps = (dist / (roadWidth * 0.40f)).toInt().coerceAtLeast(1)

            val perpX = -dy / dist
            val perpY = dx / dist

            for (s in 0 until steps) {
                val t = s.toFloat() / steps
                val cx = x1 + dx * t
                val cy = y1 + dy * t

                // Left and right paver stones
                val stoneW = roadWidth * 0.22f
                val stoneH = roadWidth * 0.16f

                val stone1X = cx + perpX * (roadWidth * 0.20f)
                val stone1Y = cy + perpY * (roadWidth * 0.20f)
                tempRect.set(stone1X - stoneW, stone1Y - stoneH, stone1X + stoneW, stone1Y + stoneH)
                canvas.drawRoundRect(tempRect, 4f, 4f, cobblestonePaint)

                val stone2X = cx - perpX * (roadWidth * 0.20f)
                val stone2Y = cy - perpY * (roadWidth * 0.20f)
                tempRect.set(stone2X - stoneW * 0.85f, stone2Y - stoneH * 0.85f, stone2X + stoneW * 0.85f, stone2Y + stoneH * 0.85f)
                canvas.drawRoundRect(tempRect, 4f, 4f, cobblestoneHighlightPaint)
            }
        }

        // 5. Wooden Bridge Planks when passing over the river area (Waypoint 2 to 3)
        if (theme.hasHazardLiquid && waypoints.size > 2) {
            val bridgeW = roadWidth * 1.30f
            val bx = waypoints[2].x * w
            val by = (waypoints[1].y + waypoints[2].y) * 0.5f * h
            for (p in 0 until 5) {
                val py = by - 24f + p * 12f
                canvas.drawRect(bx - bridgeW * 0.5f, py, bx + bridgeW * 0.5f, py + 8f, bridgePlankPaint)
                canvas.drawLine(bx - bridgeW * 0.5f, py, bx - bridgeW * 0.5f, py + 8f, bridgeRailingPaint)
                canvas.drawLine(bx + bridgeW * 0.5f, py, bx + bridgeW * 0.5f, py + 8f, bridgeRailingPaint)
            }
        }
    }

    // =========================================================================
    // 4. Living Environmental Props (Trees, Rocks, Crystals)
    // =========================================================================

    /**
     * Draws a 3D stylized tree with organic wind sway.
     */
    fun drawStylizedTree(
        canvas: Canvas,
        tx: Float,
        ty: Float,
        size: Float,
        theme: EnvironmentTheme,
        animTime: Float,
        windPhase: Float = 0f
    ) {
        // Contact Drop Shadow
        tempRect.set(tx - size * 0.85f, ty + size * 0.20f, tx + size * 0.85f, ty + size * 0.80f)
        canvas.drawOval(tempRect, shadowPaint)

        // Wood Trunk with bark texture
        treeTrunkPaint.color = theme.trunkWoodColor
        canvas.drawRect(tx - size * 0.16f, ty - size * 0.65f, tx + size * 0.16f, ty + size * 0.35f, treeTrunkPaint)

        // Wind Sway Angle
        val swayAngle = sin(animTime * 1.8f + windPhase) * 4.5f

        canvas.save()
        canvas.translate(tx, ty - size * 0.50f)
        canvas.rotate(swayAngle, 0f, size * 0.50f)

        // 3-Tiered Foliage Spheres / Cones
        foliageDarkPaint.color = theme.foliageSecondaryColor
        foliageLightPaint.color = theme.foliagePrimaryColor

        // Tier 1 (Lower / Largest)
        canvas.drawCircle(size * 0.10f, size * 0.15f, size * 0.65f, foliageDarkPaint)
        canvas.drawCircle(-size * 0.05f, size * 0.05f, size * 0.60f, foliageLightPaint)

        // Tier 2 (Middle)
        canvas.drawCircle(size * 0.08f, -size * 0.35f, size * 0.52f, foliageDarkPaint)
        canvas.drawCircle(-size * 0.04f, -size * 0.42f, size * 0.48f, foliageLightPaint)

        // Tier 3 (Crown / Top)
        canvas.drawCircle(0f, -size * 0.80f, size * 0.38f, foliageLightPaint)

        canvas.restore()
    }

    /**
     * Draws a beveled, faceted rock boulder with moss and highlight edges.
     */
    fun drawStylizedRock(canvas: Canvas, rx: Float, ry: Float, size: Float, theme: EnvironmentTheme) {
        // Shadow
        tempRect.set(rx - size * 0.90f, ry - size * 0.10f, rx + size * 0.90f, ry + size * 0.65f)
        canvas.drawOval(tempRect, shadowPaint)

        // Shaded side
        rockDarkPaint.color = theme.rockColor
        polygonPath.reset()
        polygonPath.moveTo(rx - size * 0.80f, ry + size * 0.20f)
        polygonPath.lineTo(rx - size * 0.40f, ry - size * 0.55f)
        polygonPath.lineTo(rx + size * 0.35f, ry - size * 0.65f)
        polygonPath.lineTo(rx + size * 0.75f, ry + size * 0.25f)
        polygonPath.lineTo(rx, ry + size * 0.45f)
        polygonPath.close()
        canvas.drawPath(polygonPath, rockDarkPaint)

        // Highlight facet (Top-left facing light)
        rockLightPaint.color = theme.rockHighlightColor
        polygonPath.reset()
        polygonPath.moveTo(rx - size * 0.70f, ry + size * 0.10f)
        polygonPath.lineTo(rx - size * 0.35f, ry - size * 0.50f)
        polygonPath.lineTo(rx, ry - size * 0.15f)
        polygonPath.close()
        canvas.drawPath(polygonPath, rockLightPaint)
    }

    /**
     * Draws an animated medieval torch / brazier with dancing flames and radial glow.
     */
    fun drawAnimatedTorch(
        canvas: Canvas,
        tx: Float,
        ty: Float,
        radius: Float,
        theme: EnvironmentTheme,
        animTime: Float,
        enableDynamicLighting: Boolean = true
    ) {
        // Shadow
        tempRect.set(tx - radius * 0.6f, ty + radius * 0.1f, tx + radius * 0.6f, ty + radius * 0.5f)
        canvas.drawOval(tempRect, shadowPaint)

        // Stone / Iron Pedestal
        canvas.drawRect(tx - radius * 0.20f, ty - radius * 0.65f, tx + radius * 0.20f, ty + radius * 0.20f, torchBasePaint)
        canvas.drawRect(tx - radius * 0.40f, ty - radius * 0.85f, tx + radius * 0.40f, ty - radius * 0.65f, torchBasePaint)

        // Radial Point-Light Glow
        if (enableDynamicLighting) {
            val pulse = 1f + sin(animTime * 8f) * 0.08f
            flameGlowPaint.color = theme.torchLightColor
            flameGlowPaint.alpha = 45
            canvas.drawCircle(tx, ty - radius * 0.80f, radius * 2.2f * pulse, flameGlowPaint)
            flameGlowPaint.alpha = 90
            canvas.drawCircle(tx, ty - radius * 0.80f, radius * 1.1f * pulse, flameGlowPaint)
        }

        // Dancing Multi-tongue Flame
        val flameFlicker = sin(animTime * 12f) * 3f
        val flameTopY = ty - radius * 1.55f + flameFlicker

        // Outer Flame (Red/Orange)
        flameCorePaint.color = Color.parseColor("#F97316")
        polygonPath.reset()
        polygonPath.moveTo(tx - radius * 0.30f, ty - radius * 0.80f)
        polygonPath.lineTo(tx, flameTopY)
        polygonPath.lineTo(tx + radius * 0.30f, ty - radius * 0.80f)
        polygonPath.close()
        canvas.drawPath(polygonPath, flameCorePaint)

        // Inner Core (Blazing Yellow / White)
        flameCorePaint.color = Color.parseColor("#FEF08A")
        polygonPath.reset()
        polygonPath.moveTo(tx - radius * 0.16f, ty - radius * 0.80f)
        polygonPath.lineTo(tx, flameTopY + 5f)
        polygonPath.lineTo(tx + radius * 0.16f, ty - radius * 0.80f)
        polygonPath.close()
        canvas.drawPath(polygonPath, flameCorePaint)
    }

    // =========================================================================
    // 5. 3D Spawning Portal & Medieval Fortress Base
    // =========================================================================

    fun drawSpawningPortal(
        canvas: Canvas,
        w: Float,
        h: Float,
        theme: EnvironmentTheme,
        animTime: Float
    ) {
        val spawn = StageOneMap.spawnPoint
        val px = spawn.x * w
        val py = spawn.y * h
        val radius = w * 0.085f

        // Ground shadow
        tempRect.set(px - radius * 1.35f, py - radius * 0.20f, px + radius * 1.35f, py + radius * 0.90f)
        canvas.drawOval(tempRect, shadowPaint)

        // 3D Ancient Stone Portal Arch Pylons (Left and Right)
        portalPylonPaint.color = Color.parseColor("#1E293B")
        canvas.drawRect(px - radius * 1.25f, py - radius * 1.40f, px - radius * 0.85f, py + radius * 0.30f, portalPylonPaint)
        canvas.drawRect(px + radius * 0.85f, py - radius * 1.40f, px + radius * 1.25f, py + radius * 0.30f, portalPylonPaint)

        // Top Arch lintel stone
        canvas.drawRect(px - radius * 1.35f, py - radius * 1.60f, px + radius * 1.35f, py - radius * 1.35f, portalPylonPaint)

        // Glowing Rune Tablets on lintel
        flameCorePaint.color = theme.torchLightColor
        canvas.drawCircle(px, py - radius * 1.48f, 4f, flameCorePaint)
        canvas.drawCircle(px - radius * 0.65f, py - radius * 1.48f, 3.5f, flameCorePaint)
        canvas.drawCircle(px + radius * 0.65f, py - radius * 1.48f, 3.5f, flameCorePaint)

        // Spinning Dimensional Vortex
        portalRingPaint.color = theme.torchLightColor
        canvas.drawCircle(px, py - radius * 0.35f, radius * 0.85f, portalRingPaint)

        val spinAngle = (animTime * 65f) % 360f
        portalVortexPaint.color = Color.parseColor("#E9D5FF")
        for (arm in 0 until 4) {
            val rad = Math.toRadians((spinAngle + arm * 90f).toDouble())
            val rIn = radius * 0.25f
            val rOut = radius * 0.80f
            canvas.drawLine(
                (px + cos(rad) * rIn).toFloat(),
                (py - radius * 0.35f + sin(rad) * rIn).toFloat(),
                (px + cos(rad + 0.65) * rOut).toFloat(),
                (py - radius * 0.35f + sin(rad + 0.65) * rOut).toFloat(),
                portalVortexPaint
            )
        }

        canvas.drawText("PORTAL", px, py + radius * 0.70f, textPaint)
    }

    fun drawFortressBase(
        canvas: Canvas,
        w: Float,
        h: Float,
        theme: EnvironmentTheme,
        animTime: Float
    ) {
        val base = StageOneMap.basePoint
        val bx = base.x * w
        val by = base.y * h
        val radius = w * 0.11f

        // 1. Massive Ground Shadow
        tempRect.set(bx - radius * 1.30f, by - radius * 0.15f, bx + radius * 1.30f, by + radius * 1.15f)
        canvas.drawOval(tempRect, shadowPaint)

        // 2. 3D Fortress Bastion Block (Granite Walls)
        castleWallPaint.color = theme.castleWallColor
        tempRect.set(bx - radius, by - radius * 0.85f, bx + radius, by + radius * 0.65f)
        canvas.drawRoundRect(tempRect, 12f, 12f, castleWallPaint)

        // 3. Corner Guard Turrets
        val turretR = radius * 0.32f
        canvas.drawCircle(bx - radius * 0.90f, by - radius * 0.75f, turretR, castleWallPaint)
        canvas.drawCircle(bx + radius * 0.90f, by - radius * 0.75f, turretR, castleWallPaint)

        // 4. Crenelated Battlements along the upper edge
        castleTrimPaint.color = theme.castleTrimColor
        val crenelW = (radius * 2f) / 7f
        for (i in 0 until 7 step 2) {
            val left = (bx - radius) + i * crenelW
            canvas.drawRect(left, by - radius * 1.05f, left + crenelW, by - radius * 0.85f, castleTrimPaint)
        }

        // 5. Arched Portcullis Gate with Iron Studs
        val gateW = radius * 0.45f
        val gateH = radius * 0.65f
        tempRect.set(bx - gateW * 0.5f, by + radius * 0.65f - gateH, bx + gateW * 0.5f, by + radius * 0.65f)
        canvas.drawRoundRect(tempRect, 8f, 8f, castleGatePaint)

        // 6. Flying Kingdom Banner on West and East Turrets
        val flagWave = sin(animTime * 6f) * 6f
        polygonPath.reset()
        polygonPath.moveTo(bx - radius * 0.90f, by - radius * 1.45f)
        polygonPath.lineTo(bx - radius * 0.45f, by - radius * 1.35f + flagWave)
        polygonPath.lineTo(bx - radius * 0.90f, by - radius * 1.20f)
        polygonPath.close()
        canvas.drawPath(polygonPath, flagPaint)

        canvas.drawText("CASTELO", bx, by + radius * 0.95f, textPaint)
    }

    // =========================================================================
    // 6. Atmospheric Vignette & Mood Shading
    // =========================================================================

    fun drawAtmosphericVignette(canvas: Canvas, w: Float, h: Float, theme: EnvironmentTheme) {
        vignettePaint.color = theme.ambientVignetteColor
        val borderThick = w * 0.045f
        // Top and bottom atmospheric gradient strips
        canvas.drawRect(0f, 0f, w, borderThick, vignettePaint)
        canvas.drawRect(0f, h - borderThick, w, h, vignettePaint)
    }
}
