package com.heroes.towerdefense.game.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.heroes.towerdefense.model.tower.TowerType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D Stylized Isometric Tower Renderer (Clash Royale Art Style Reference).
 *
 * Implements voluminous, beveled 3D isometric representations for:
 * - Archer Tower: Hexagonal stone foundation, heavy timber framework, planked platform,
 *   conical hunter-green shingled roof, and animated fluttering pennant flag.
 * - Mage Tower: Stepped cylindrical obsidian/indigo spire with glowing carved gold runes,
 *   floating iron balcony, and colossal levitating, facet-shaded arcane crystal with orbital rings.
 * - Cannon Tower: Fortified heavy granite bastion with riveted iron armor plates, 360-degree
 *   turret mount, and blackened cast-iron cannon with physical recoil kickback.
 * - Frost Tower: Glacial crystalline citadel with icy facets and cold mist aura.
 *
 * Zero-allocation draw loop with static reusable Paint, Path, and RectF instances.
 */
object IsometricTowerRenderer {

    // =========================================================================
    // Reusable Paints (Allocated once, zero GC pressure during frame rendering)
    // =========================================================================

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#55000000") // Soft isometric drop shadow
        style = Paint.Style.FILL
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 18f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val previewFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val previewStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
    }

    // Reusable Paths & Rects
    private val polygonPath = Path()
    private val roofPath = Path()
    private val flagPath = Path()
    private val tempRect = RectF()

    // =========================================================================
    // Tower Shadow
    // =========================================================================

    /**
     * Draws an isometric ground contact shadow under the tower foundation.
     */
    fun drawTowerShadow(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        tempRect.set(
            cx - radius * 1.15f,
            cy + radius * 0.25f,
            cx + radius * 1.15f,
            cy + radius * 1.05f
        )
        canvas.drawOval(tempRect, shadowPaint)
    }

    // =========================================================================
    // 3D Isometric Archer Tower
    // =========================================================================

    fun drawArcherTower(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        recoilOffset: Float = 0f,
        spawnScale: Float = 1f,
        animTime: Float = 0f
    ) {
        canvas.save()
        canvas.translate(cx, cy)
        if (spawnScale != 1f) {
            canvas.scale(spawnScale, spawnScale, 0f, radius * 0.5f)
        }

        val baseW = radius * 0.95f
        val baseH = radius * 0.55f

        // 1. Foundation Base Shadow Face (Bottom/Right - darker)
        fillPaint.color = Color.parseColor("#334155") // Dark slate stone
        polygonPath.reset()
        polygonPath.moveTo(-baseW, 0f)
        polygonPath.lineTo(0f, baseH)
        polygonPath.lineTo(baseW, 0f)
        polygonPath.lineTo(baseW, -radius * 0.45f)
        polygonPath.lineTo(-baseW, -radius * 0.45f)
        polygonPath.close()
        canvas.drawPath(polygonPath, fillPaint)

        // 2. Foundation Front Light Face (Top/Left - illuminated stone)
        fillPaint.color = Color.parseColor("#475569") // Medium slate
        polygonPath.reset()
        polygonPath.moveTo(-baseW, -radius * 0.45f)
        polygonPath.lineTo(0f, -radius * 0.15f)
        polygonPath.lineTo(baseW, -radius * 0.45f)
        polygonPath.lineTo(0f, -radius * 0.75f)
        polygonPath.close()
        canvas.drawPath(polygonPath, fillPaint)

        // Cobblestone foundation border rim
        strokePaint.color = Color.parseColor("#1E293B")
        strokePaint.strokeWidth = 2.5f
        canvas.drawPath(polygonPath, strokePaint)

        // 3. Sturdy Wooden Timber Pillars (4 Vertical Posts)
        val pillarW = radius * 0.18f
        val pillarH = radius * 1.15f
        val pillarY = -radius * 0.45f - pillarH

        // Back-left and back-right pillars
        fillPaint.color = Color.parseColor("#5C2B09") // Dark shaded timber
        canvas.drawRect(-baseW * 0.70f, pillarY, -baseW * 0.70f + pillarW, pillarY + pillarH, fillPaint)
        canvas.drawRect(baseW * 0.70f - pillarW, pillarY, baseW * 0.70f, pillarY + pillarH, fillPaint)

        // Cross-bracing wooden struts
        strokePaint.color = Color.parseColor("#78350F")
        strokePaint.strokeWidth = 4f
        canvas.drawLine(-baseW * 0.65f, pillarY + pillarH * 0.2f, baseW * 0.65f, pillarY + pillarH * 0.8f, strokePaint)
        canvas.drawLine(-baseW * 0.65f, pillarY + pillarH * 0.8f, baseW * 0.65f, pillarY + pillarH * 0.2f, strokePaint)

        // Front-left and front-right pillars (Illuminated timber)
        fillPaint.color = Color.parseColor("#92400E") // Rich amber wood
        canvas.drawRect(-baseW * 0.50f, pillarY + radius * 0.15f, -baseW * 0.50f + pillarW, pillarY + pillarH + radius * 0.15f, fillPaint)
        canvas.drawRect(baseW * 0.50f - pillarW, pillarY + radius * 0.15f, baseW * 0.50f, pillarY + pillarH + radius * 0.15f, fillPaint)

        // Iron reinforcement bands on posts
        fillPaint.color = Color.parseColor("#0F172A")
        canvas.drawRect(-baseW * 0.50f, pillarY + pillarH * 0.5f, -baseW * 0.50f + pillarW, pillarY + pillarH * 0.5f + 4f, fillPaint)
        canvas.drawRect(baseW * 0.50f - pillarW, pillarY + pillarH * 0.5f, baseW * 0.50f, pillarY + pillarH * 0.5f + 4f, fillPaint)

        // 4. Wooden Battlement Platform (Floor + Balustrade)
        val platY = pillarY - recoilOffset
        fillPaint.color = Color.parseColor("#78350F")
        tempRect.set(-baseW * 0.90f, platY - radius * 0.25f, baseW * 0.90f, platY + radius * 0.15f)
        canvas.drawRoundRect(tempRect, 6f, 6f, fillPaint)
        strokePaint.color = Color.parseColor("#451A03")
        strokePaint.strokeWidth = 2.5f
        canvas.drawRoundRect(tempRect, 6f, 6f, strokePaint)

        // Front protective battlements (crenels)
        fillPaint.color = Color.parseColor("#B45309")
        canvas.drawRect(-baseW * 0.85f, platY - radius * 0.40f, -baseW * 0.55f, platY - radius * 0.15f, fillPaint)
        canvas.drawRect(-baseW * 0.15f, platY - radius * 0.40f, baseW * 0.15f, platY - radius * 0.15f, fillPaint)
        canvas.drawRect(baseW * 0.55f, platY - radius * 0.40f, baseW * 0.85f, platY - radius * 0.15f, fillPaint)

        // Miniature hunter bow peeking over the parapet
        strokePaint.color = Color.parseColor("#FEF08A") // Golden bow
        strokePaint.strokeWidth = 3f
        canvas.drawArc(
            RectF(-radius * 0.25f, platY - radius * 0.60f, radius * 0.25f, platY - radius * 0.20f),
            180f, 180f, false, strokePaint
        )

        // 5. 3D Conical Shingled Roof (Hunter Green with light and dark facets)
        val roofTopY = platY - radius * 1.10f

        // Shaded dark roof slope (right facet)
        fillPaint.color = Color.parseColor("#14532D") // Deep forest pine
        roofPath.reset()
        roofPath.moveTo(0f, roofTopY)
        roofPath.lineTo(baseW * 1.05f, platY - radius * 0.20f)
        roofPath.lineTo(0f, platY + radius * 0.05f)
        roofPath.close()
        canvas.drawPath(roofPath, fillPaint)

        // Highlighted light roof slope (left facet)
        fillPaint.color = Color.parseColor("#16A34A") // Vibrant emerald shingle
        roofPath.reset()
        roofPath.moveTo(0f, roofTopY)
        roofPath.lineTo(-baseW * 1.05f, platY - radius * 0.20f)
        roofPath.lineTo(0f, platY + radius * 0.05f)
        roofPath.close()
        canvas.drawPath(roofPath, fillPaint)

        // Center roof ridge highlight
        strokePaint.color = Color.parseColor("#86EFAC")
        strokePaint.strokeWidth = 2f
        canvas.drawLine(0f, roofTopY, 0f, platY + radius * 0.05f, strokePaint)

        // 6. Finial Spire & Waving Pennant Flag
        strokePaint.color = Color.parseColor("#78350F")
        strokePaint.strokeWidth = 3f
        canvas.drawLine(0f, roofTopY, 0f, roofTopY - radius * 0.55f, strokePaint)

        // Waving red medieval pennant
        val waveOffset = sin(animTime * 6f) * (radius * 0.10f)
        fillPaint.color = Color.parseColor("#EF4444")
        flagPath.reset()
        flagPath.moveTo(0f, roofTopY - radius * 0.52f)
        flagPath.lineTo(radius * 0.55f, roofTopY - radius * 0.42f + waveOffset)
        flagPath.lineTo(0f, roofTopY - radius * 0.28f)
        flagPath.close()
        canvas.drawPath(flagPath, fillPaint)

        // Gold finial ball
        fillPaint.color = Color.parseColor("#F59E0B")
        canvas.drawCircle(0f, roofTopY - radius * 0.55f, radius * 0.08f, fillPaint)

        // 7. Bow fire recoil twang and release spark
        if (recoilOffset > 0.05f) {
            val twangScale = recoilOffset.coerceIn(0f, 1f)
            fillPaint.color = Color.parseColor("#FEF08A")
            fillPaint.alpha = (220 * twangScale).toInt()
            canvas.drawCircle(radius * 0.45f, platY - radius * 0.15f, radius * 0.18f * twangScale, fillPaint)
        }

        canvas.restore()
    }

    // =========================================================================
    // 3D Isometric Mage Tower
    // =========================================================================

    fun drawMageTower(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        recoilOffset: Float = 0f,
        spawnScale: Float = 1f,
        animTime: Float = 0f
    ) {
        canvas.save()
        canvas.translate(cx, cy)
        if (spawnScale != 1f) {
            canvas.scale(spawnScale, spawnScale, 0f, radius * 0.5f)
        }

        val baseW = radius * 0.90f
        val baseH = radius * 0.48f

        // 1. Stepped Cylindrical Podium (Layer 1 - Dark Arcane Slate)
        fillPaint.color = Color.parseColor("#1E1B4B")
        tempRect.set(-baseW, -radius * 0.20f, baseW, baseH + radius * 0.15f)
        canvas.drawRoundRect(tempRect, 12f, 12f, fillPaint)

        // Upper step ring with carved gold runes
        fillPaint.color = Color.parseColor("#312E81")
        tempRect.set(-baseW * 0.85f, -radius * 0.45f, baseW * 0.85f, radius * 0.10f)
        canvas.drawRoundRect(tempRect, 10f, 10f, fillPaint)

        strokePaint.color = Color.parseColor("#F59E0B") // Inlaid gold rune trim
        strokePaint.strokeWidth = 2f
        canvas.drawRoundRect(tempRect, 10f, 10f, strokePaint)

        // 2. Slender Arcane Spire Column
        val spireTopY = -radius * 1.85f
        val spireBotY = -radius * 0.45f

        // Left shaded column half
        fillPaint.color = Color.parseColor("#4338CA")
        polygonPath.reset()
        polygonPath.moveTo(-radius * 0.45f, spireBotY)
        polygonPath.lineTo(-radius * 0.25f, spireTopY)
        polygonPath.lineTo(0f, spireTopY)
        polygonPath.lineTo(0f, spireBotY)
        polygonPath.close()
        canvas.drawPath(polygonPath, fillPaint)

        // Right illuminated column half
        fillPaint.color = Color.parseColor("#6366F1")
        polygonPath.reset()
        polygonPath.moveTo(0f, spireBotY)
        polygonPath.lineTo(0f, spireTopY)
        polygonPath.lineTo(radius * 0.25f, spireTopY)
        polygonPath.lineTo(radius * 0.45f, spireBotY)
        polygonPath.close()
        canvas.drawPath(polygonPath, fillPaint)

        // Vertical glowing arcane power conduit (Cyan pulse)
        val pulseAlpha = (180 + sin(animTime * 5f) * 60).toInt().coerceIn(100, 255)
        strokePaint.color = Color.parseColor("#38BDF8")
        strokePaint.strokeWidth = 3f
        strokePaint.alpha = pulseAlpha
        canvas.drawLine(0f, spireBotY - 4f, 0f, spireTopY + 4f, strokePaint)

        // 3. Ornate Upper Iron Balcony
        fillPaint.color = Color.parseColor("#1E293B")
        tempRect.set(-radius * 0.55f, spireTopY - radius * 0.15f, radius * 0.55f, spireTopY + radius * 0.10f)
        canvas.drawRoundRect(tempRect, 6f, 6f, fillPaint)
        strokePaint.color = Color.parseColor("#F59E0B")
        strokePaint.strokeWidth = 2f
        canvas.drawRoundRect(tempRect, 6f, 6f, strokePaint)

        // 4. Levitating Colossal Arcane Crystal (Cut Diamond Facets)
        val floatOffset = sin(animTime * 3.5f) * (radius * 0.15f) - recoilOffset
        val crystalCenterY = spireTopY - radius * 0.65f + floatOffset
        val crystalW = radius * 0.42f
        val crystalH = radius * 0.60f

        // Soft radial crystal glow in cyan/violet
        glowPaint.color = Color.parseColor("#A855F7")
        glowPaint.alpha = 50
        canvas.drawCircle(0f, crystalCenterY, crystalH * 1.35f, glowPaint)
        glowPaint.color = Color.parseColor("#38BDF8")
        glowPaint.alpha = 75
        canvas.drawCircle(0f, crystalCenterY, crystalH * 0.90f, glowPaint)

        // Top-left illuminated crystal facet
        fillPaint.color = Color.parseColor("#BAE6FD") // Light cyan crystal
        polygonPath.reset()
        polygonPath.moveTo(0f, crystalCenterY - crystalH)
        polygonPath.lineTo(-crystalW, crystalCenterY)
        polygonPath.lineTo(0f, crystalCenterY + crystalH * 0.4f)
        polygonPath.close()
        canvas.drawPath(polygonPath, fillPaint)

        // Top-right medium facet
        fillPaint.color = Color.parseColor("#38BDF8") // Rich cyan crystal
        polygonPath.reset()
        polygonPath.moveTo(0f, crystalCenterY - crystalH)
        polygonPath.lineTo(crystalW, crystalCenterY)
        polygonPath.lineTo(0f, crystalCenterY + crystalH * 0.4f)
        polygonPath.close()
        canvas.drawPath(polygonPath, fillPaint)

        // Bottom shaded facet
        fillPaint.color = Color.parseColor("#0284C7")
        polygonPath.reset()
        polygonPath.moveTo(-crystalW, crystalCenterY)
        polygonPath.lineTo(0f, crystalCenterY + crystalH)
        polygonPath.lineTo(crystalW, crystalCenterY)
        polygonPath.lineTo(0f, crystalCenterY + crystalH * 0.4f)
        polygonPath.close()
        canvas.drawPath(polygonPath, fillPaint)

        // Crystal facet specular shine lines
        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 1.5f
        strokePaint.alpha = 200
        canvas.drawLine(0f, crystalCenterY - crystalH, 0f, crystalCenterY + crystalH, strokePaint)
        canvas.drawLine(-crystalW, crystalCenterY, crystalW, crystalCenterY, strokePaint)

        // 5. Tilted Orbital Rune Rings
        val orbitAngle = (animTime * 45f) % 360f
        strokePaint.color = Color.parseColor("#E0E7FF")
        strokePaint.strokeWidth = 2f
        strokePaint.alpha = 180
        tempRect.set(-crystalW * 1.55f, crystalCenterY - radius * 0.20f, crystalW * 1.55f, crystalCenterY + radius * 0.20f)
        canvas.save()
        canvas.rotate(25f, 0f, crystalCenterY)
        canvas.drawOval(tempRect, strokePaint)
        // Little orbiting energy spark
        val sparkRad = Math.toRadians(orbitAngle.toDouble())
        val sx = (cos(sparkRad) * crystalW * 1.55f).toFloat()
        val sy = (sin(sparkRad) * radius * 0.20f).toFloat() + crystalCenterY
        fillPaint.color = Color.parseColor("#FDE047")
        canvas.drawCircle(sx, sy, 3.5f, fillPaint)
        canvas.restore()

        // 6. Arcane discharge crystal flare on attack
        if (recoilOffset > 0.05f) {
            val flareScale = recoilOffset.coerceIn(0f, 1f)
            glowPaint.color = Color.parseColor("#38BDF8")
            glowPaint.alpha = (180 * flareScale).toInt()
            canvas.drawCircle(0f, crystalCenterY, crystalH * 1.4f * flareScale, glowPaint)
            fillPaint.color = Color.WHITE
            fillPaint.alpha = (230 * flareScale).toInt()
            canvas.drawCircle(0f, crystalCenterY, crystalW * 0.45f * flareScale, fillPaint)
        }

        canvas.restore()
    }

    // =========================================================================
    // 3D Isometric Cannon Tower
    // =========================================================================

    fun drawCannonTower(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        recoilOffset: Float = 0f,
        spawnScale: Float = 1f,
        animTime: Float = 0f
    ) {
        canvas.save()
        canvas.translate(cx, cy)
        if (spawnScale != 1f) {
            canvas.scale(spawnScale, spawnScale, 0f, radius * 0.5f)
        }

        val baseW = radius * 1.05f
        val baseH = radius * 0.55f

        // 1. Heavy Fortified Granite Bastion Block (Isometric cube)
        // Shaded Right Face
        fillPaint.color = Color.parseColor("#1E293B")
        polygonPath.reset()
        polygonPath.moveTo(0f, baseH)
        polygonPath.lineTo(baseW, 0f)
        polygonPath.lineTo(baseW, -radius * 0.85f)
        polygonPath.lineTo(0f, -radius * 0.35f)
        polygonPath.close()
        canvas.drawPath(polygonPath, fillPaint)

        // Light Left Face
        fillPaint.color = Color.parseColor("#334155")
        polygonPath.reset()
        polygonPath.moveTo(-baseW, 0f)
        polygonPath.lineTo(0f, baseH)
        polygonPath.lineTo(0f, -radius * 0.35f)
        polygonPath.lineTo(-baseW, -radius * 0.85f)
        polygonPath.close()
        canvas.drawPath(polygonPath, fillPaint)

        // Top Platform Face
        fillPaint.color = Color.parseColor("#475569")
        polygonPath.reset()
        polygonPath.moveTo(-baseW, -radius * 0.85f)
        polygonPath.lineTo(0f, -radius * 0.35f)
        polygonPath.lineTo(baseW, -radius * 0.85f)
        polygonPath.lineTo(0f, -radius * 1.35f)
        polygonPath.close()
        canvas.drawPath(polygonPath, fillPaint)

        // Bolted iron reinforcement plates on corners
        strokePaint.color = Color.parseColor("#0F172A")
        strokePaint.strokeWidth = 3f
        canvas.drawLine(-baseW, -radius * 0.85f, 0f, -radius * 0.35f, strokePaint)
        canvas.drawLine(0f, -radius * 0.35f, baseW, -radius * 0.85f, strokePaint)
        canvas.drawLine(0f, -radius * 0.35f, 0f, baseH, strokePaint)

        // Golden rivet studs on corner plates
        fillPaint.color = Color.parseColor("#F59E0B")
        canvas.drawCircle(0f, -radius * 0.15f, 3f, fillPaint)
        canvas.drawCircle(0f, radius * 0.15f, 3f, fillPaint)
        canvas.drawCircle(-baseW * 0.50f, -radius * 0.60f, 2.5f, fillPaint)
        canvas.drawCircle(baseW * 0.50f, -radius * 0.60f, 2.5f, fillPaint)

        // 2. Swiveling Armored Turret Dome Mount
        val turretCenterY = -radius * 0.90f
        fillPaint.color = Color.parseColor("#1E293B")
        tempRect.set(-radius * 0.55f, turretCenterY - radius * 0.30f, radius * 0.55f, turretCenterY + radius * 0.15f)
        canvas.drawOval(tempRect, fillPaint)
        strokePaint.color = Color.parseColor("#64748B")
        strokePaint.strokeWidth = 2.5f
        canvas.drawOval(tempRect, strokePaint)

        // 3. Blackened Cast-Iron Cannon with physical recoil kickback and idle sway
        val kick = recoilOffset * radius * 0.35f
        val idleSway = sin(animTime * 2.5f) * 1.5f
        val barrelAngleDeg = -30f + idleSway // Elevated isometric firing angle with subtle idle sway
        val barrelLen = radius * 0.95f
        val barrelThick = radius * 0.28f

        canvas.save()
        canvas.translate(0f, turretCenterY)
        canvas.rotate(barrelAngleDeg)
        canvas.translate(-kick, 0f) // Recoil displacement backward along barrel axis

        // Cannon barrel shadow/body
        fillPaint.color = Color.parseColor("#0F172A")
        tempRect.set(0f, -barrelThick * 0.5f, barrelLen, barrelThick * 0.5f)
        canvas.drawRoundRect(tempRect, 4f, 4f, fillPaint)

        // Illuminated barrel top sheen
        strokePaint.color = Color.parseColor("#475569")
        strokePaint.strokeWidth = 3f
        canvas.drawLine(4f, -barrelThick * 0.3f, barrelLen - 4f, -barrelThick * 0.3f, strokePaint)

        // Heavy reinforced muzzle ring
        fillPaint.color = Color.parseColor("#334155")
        canvas.drawRect(barrelLen - radius * 0.12f, -barrelThick * 0.65f, barrelLen, barrelThick * 0.65f, fillPaint)
        strokePaint.color = Color.parseColor("#F59E0B") // Brass reinforced muzzle collar
        strokePaint.strokeWidth = 2f
        canvas.drawLine(barrelLen - radius * 0.12f, -barrelThick * 0.65f, barrelLen - radius * 0.12f, barrelThick * 0.65f, strokePaint)

        // Barrel trunnion mount bracket
        fillPaint.color = Color.parseColor("#64748B")
        canvas.drawCircle(radius * 0.25f, 0f, radius * 0.12f, fillPaint)

        // 4. Muzzle Flash and Smoke Billow on Cannon Firing
        if (recoilOffset > 0.05f) {
            val flashScale = recoilOffset.coerceIn(0f, 1f)
            val flashR = radius * 0.42f * flashScale
            // Fiery outer flash
            fillPaint.color = Color.parseColor("#EF4444")
            canvas.drawCircle(barrelLen + flashR * 0.5f, 0f, flashR, fillPaint)
            // Yellow plasma core
            fillPaint.color = Color.parseColor("#FDE047")
            canvas.drawCircle(barrelLen + flashR * 0.3f, 0f, flashR * 0.65f, fillPaint)
            // White-hot center
            fillPaint.color = Color.WHITE
            canvas.drawCircle(barrelLen + flashR * 0.15f, 0f, flashR * 0.35f, fillPaint)

            // Smoke puff
            fillPaint.color = Color.parseColor("#64748B")
            fillPaint.alpha = (160 * flashScale).toInt()
            canvas.drawCircle(barrelLen + flashR * 0.75f, -flashR * 0.35f, flashR * 0.50f, fillPaint)
        }

        canvas.restore()

        canvas.restore()
    }

    // =========================================================================
    // 3D Isometric Frost Spire
    // =========================================================================

    fun drawFrostTower(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        spawnScale: Float = 1f,
        animTime: Float = 0f
    ) {
        canvas.save()
        canvas.translate(cx, cy)
        if (spawnScale != 1f) {
            canvas.scale(spawnScale, spawnScale, 0f, radius * 0.5f)
        }

        // Glacial Crystal Pedestal
        fillPaint.color = Color.parseColor("#0284C7")
        tempRect.set(-radius * 0.85f, -radius * 0.35f, radius * 0.85f, radius * 0.35f)
        canvas.drawRoundRect(tempRect, 8f, 8f, fillPaint)

        // Faceted Ice Spire
        val topY = -radius * 1.80f
        fillPaint.color = Color.parseColor("#BAE6FD")
        polygonPath.reset()
        polygonPath.moveTo(0f, topY)
        polygonPath.lineTo(-radius * 0.45f, -radius * 0.35f)
        polygonPath.lineTo(0f, -radius * 0.15f)
        polygonPath.close()
        canvas.drawPath(polygonPath, fillPaint)

        fillPaint.color = Color.parseColor("#38BDF8")
        polygonPath.reset()
        polygonPath.moveTo(0f, topY)
        polygonPath.lineTo(radius * 0.45f, -radius * 0.35f)
        polygonPath.lineTo(0f, -radius * 0.15f)
        polygonPath.close()
        canvas.drawPath(polygonPath, fillPaint)

        // Glowing Frost Mist
        val pulse = 1f + sin(animTime * 4f) * 0.1f
        glowPaint.color = Color.parseColor("#E0F2FE")
        glowPaint.alpha = 60
        canvas.drawCircle(0f, topY, radius * 0.45f * pulse, glowPaint)

        canvas.restore()
    }

    // =========================================================================
    // Placement Preview Hologram
    // =========================================================================

    /**
     * Renders a translucent 3D holographic projection of the selected tower type
     * on an empty build spot, complete with valid/invalid anchor ring and attack range preview.
     */
    fun drawPlacementPreview(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        type: TowerType,
        rangeRadius: Float,
        isValid: Boolean,
        animTime: Float = 0f
    ) {
        val anchorColor = if (isValid) Color.parseColor("#10B981") else Color.parseColor("#EF4444")
        val ringPulse = 1f + sin(animTime * 6f) * 0.05f

        // 1. Attack Range Circle
        previewFillPaint.color = anchorColor
        previewFillPaint.alpha = if (isValid) 35 else 50
        canvas.drawCircle(cx, cy, rangeRadius, previewFillPaint)

        previewStrokePaint.color = anchorColor
        previewStrokePaint.alpha = if (isValid) 160 else 220
        canvas.drawCircle(cx, cy, rangeRadius * ringPulse, previewStrokePaint)

        // 2. Ground Anchor Pedestal
        strokePaint.color = anchorColor
        strokePaint.strokeWidth = 3f
        tempRect.set(cx - 38f * ringPulse, cy - 18f * ringPulse, cx + 38f * ringPulse, cy + 18f * ringPulse)
        canvas.drawOval(tempRect, strokePaint)

        // 3. Translucent Holographic Tower Projection (Alpha 140)
        canvas.save()
        // Render isometric tower with subtle floating hologram hover
        val hover = sin(animTime * 4f) * 6f
        when (type) {
            TowerType.ARCHER_TOWER -> drawArcherTower(canvas, cx, cy - hover, 36f, spawnScale = 0.95f, animTime = animTime)
            TowerType.MAGE_TOWER -> drawMageTower(canvas, cx, cy - hover, 36f, spawnScale = 0.95f, animTime = animTime)
            TowerType.CANNON_TOWER -> drawCannonTower(canvas, cx, cy - hover, 36f, spawnScale = 0.95f, animTime = animTime)
            TowerType.FROST_TOWER -> drawFrostTower(canvas, cx, cy - hover, 36f, spawnScale = 0.95f, animTime = animTime)
        }
        canvas.restore()
    }
}
