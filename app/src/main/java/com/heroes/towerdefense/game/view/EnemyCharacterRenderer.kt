package com.heroes.towerdefense.game.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.heroes.towerdefense.game.boss.ActiveBoss
import com.heroes.towerdefense.game.boss.BossType
import com.heroes.towerdefense.game.model.ActiveEnemy
import com.heroes.towerdefense.game.model.EnemyAnimState
import com.heroes.towerdefense.game.model.EnemyVisualType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance, professional 2D Canvas character renderer and animator
 * for all common enemies (Goblin, Skeleton, Orc, Dark Mage) and epic bosses
 * (Obsidian Golem, Shadow Queen, Astral Dragon) in Heroes Tower Defense
 * (Visual Upgrade Part 2).
 *
 * Implements articulated skeletal/procedural animations (running, marching, heavy stomping,
 * hovering levitation, weapon strikes, staff channeling, hit flinches, death collapses,
 * multi-phase boss transformations, and cinematic entrance sequences) with zero-allocation draw loops.
 */
object EnemyCharacterRenderer {

    // =========================================================================
    // Reusable Paints (Allocated once, zero GC overhead during frame rendering)
    // =========================================================================

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#55000000")
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

    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val telegraphWarningPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#44EF4444")
        style = Paint.Style.FILL
    }

    private val telegraphRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EF4444")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(10f, 6f), 0f)
    }

    private val protectionShieldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#998B5CF6")
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val vulnerabilityAuraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F59E0B")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(8f, 5f), 0f)
    }

    private val healthBarBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7F1D1D") // Dark crimson
        style = Paint.Style.FILL
    }

    private val healthBarFgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#22C55E") // Bright green
        style = Paint.Style.FILL
    }

    private val healthBarBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0F172A")
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
    }

    private val nameBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 18f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val titleBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FCD34D")
        textSize = 14f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    // Reusable Paths & Rects
    private val reusablePath = Path()
    private val reusablePath2 = Path()
    private val reusableRectF = RectF()

    // =========================================================================
    // Public Entry Points
    // =========================================================================

    /**
     * Draws an active common enemy on the battlefield with articulated animations,
     * movement facing direction, hit reactions, and health bar.
     */
    fun drawEnemy(canvas: Canvas, enemy: ActiveEnemy, cx: Float, cy: Float, w: Float, h: Float) {
        val baseSize = minOf(w, h) * 0.038f
        val animTime = enemy.animTimer
        val animState = enemy.animState
        val facing = enemy.facingDirection
        val isDying = enemy.isDying || !enemy.isAlive || animState == EnemyAnimState.DEATH

        // Save canvas state for character local coordinate transform
        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(facing, 1f) // Flip horizontally according to movement direction

        // Hit recoil shake & flinch (suppressed during death animation)
        if (enemy.hitReactionTimer > 0f && !isDying) {
            val flinchOffset = sin(enemy.hitReactionTimer * 40f) * (baseSize * 0.15f)
            canvas.translate(-flinchOffset, 0f)
        }

        // Dying animation transformation: sink down and subtle collapse
        if (isDying) {
            val deathProgress = (enemy.deathAnimTimer / enemy.deathDuration.coerceAtLeast(0.01f)).coerceIn(0f, 1f)
            canvas.translate(0f, deathProgress * (baseSize * 0.35f))
            canvas.scale(1f - (deathProgress * 0.25f), 1f - (deathProgress * 0.35f))
        }

        // Draw specific enemy body according to visualType
        when (enemy.visualType) {
            EnemyVisualType.GOBLIN -> drawGoblin(canvas, baseSize, animTime, animState, enemy.hitReactionTimer > 0f, isDying)
            EnemyVisualType.SKELETON -> drawSkeleton(canvas, baseSize, animTime, animState, enemy.hitReactionTimer > 0f, isDying)
            EnemyVisualType.ORC -> drawOrc(canvas, baseSize * 1.25f, animTime, animState, enemy.hitReactionTimer > 0f, isDying)
            EnemyVisualType.DARK_MAGE -> drawDarkMage(canvas, baseSize * 1.1f, animTime, animState, enemy.hitReactionTimer > 0f, isDying)
            else -> drawGoblin(canvas, baseSize, animTime, animState, enemy.hitReactionTimer > 0f, isDying)
        }

        canvas.restore()

        // Floating Health Bar (Rendered in screen space without horizontal flip; hidden when dying)
        if (!isDying) {
            drawFloatingHealthBar(canvas, cx, cy - (baseSize * 1.6f), baseSize * 2.2f, 6f, enemy.healthFraction, isBoss = false)
        }
    }

    /**
     * Draws an active epic boss on the battlefield with cinematic presence,
     * telegraphed attack danger zones, protection shields, vulnerability auras,
     * multi-phase visual transformations, and dedicated boss health bar.
     */
    fun drawBoss(canvas: Canvas, boss: ActiveBoss, bx: Float, by: Float, w: Float, h: Float) {
        val bossRadius = minOf(w, h) * 0.082f
        val animTime = boss.animTimer
        val facing = boss.facingDirection
        val isDying = boss.isDying || !boss.isAlive || boss.animState == EnemyAnimState.DEATH

        // 1. Draw Telegraphed Danger Zone if charging an attack (hidden when dying)
        if (!isDying && boss.isTelegraphing && boss.activeTelegraphAttack != null) {
            val attack = boss.activeTelegraphAttack!!
            val attackRadius = attack.range * w
            canvas.drawCircle(bx, by, attackRadius, telegraphWarningPaint)
            canvas.drawCircle(bx, by, attackRadius, telegraphRingPaint)
        }

        // 2. Draw Protection Shield (e.g. Shadow Veil; hidden when dying)
        if (!isDying && boss.isProtected) {
            val shieldPulse = 1f + sin(animTime * 8f) * 0.05f
            canvas.drawCircle(bx, by, bossRadius * 1.45f * shieldPulse, protectionShieldPaint)
        }

        // 3. Draw Vulnerability fractured aura (hidden when dying)
        if (!isDying && boss.isVulnerable) {
            val vulnPulse = 1f + cos(animTime * 10f) * 0.06f
            canvas.drawCircle(bx, by, bossRadius * 1.35f * vulnPulse, vulnerabilityAuraPaint)
        }

        // 4. Transform canvas to boss local space with facing direction
        canvas.save()
        canvas.translate(bx, by)
        canvas.scale(facing, 1f)

        // Hit flinch (suppressed during death animation)
        if (boss.hitReactionTimer > 0f && !isDying) {
            val flinch = sin(boss.hitReactionTimer * 30f) * (bossRadius * 0.10f)
            canvas.translate(-flinch, 0f)
        }

        // Dying boss visual progression
        if (isDying) {
            val deathProgress = (boss.deathAnimTimer / boss.deathDuration.coerceAtLeast(0.01f)).coerceIn(0f, 1f)
            canvas.translate(0f, deathProgress * (bossRadius * 0.25f))
            canvas.scale(1f - (deathProgress * 0.20f), 1f - (deathProgress * 0.25f))
        }

        // Draw Boss Character Model
        when (boss.bossConfig.type) {
            BossType.OBSIDIAN_GOLEM -> drawObsidianGolem(
                canvas = canvas,
                size = bossRadius,
                animTime = animTime,
                isVulnerable = boss.isVulnerable,
                isTelegraphing = boss.isTelegraphing,
                isHit = boss.hitReactionTimer > 0f,
                isDying = isDying
            )
            BossType.SHADOW_QUEEN -> drawShadowQueen(
                canvas = canvas,
                size = bossRadius,
                animTime = animTime,
                isProtected = boss.isProtected,
                isVulnerable = boss.isVulnerable,
                isTelegraphing = boss.isTelegraphing,
                isHit = boss.hitReactionTimer > 0f,
                isDying = isDying
            )
            BossType.ASTRAL_DRAGON -> drawAstralDragon(
                canvas = canvas,
                size = bossRadius * 1.35f,
                animTime = animTime,
                phase = boss.currentPhase,
                isTelegraphing = boss.isTelegraphing,
                isHit = boss.hitReactionTimer > 0f,
                isDying = isDying
            )
        }

        canvas.restore()

        // 5. Boss Dedicated Floating Health Bar with Title (hidden when dying)
        if (!isDying) {
            drawFloatingHealthBar(
                canvas = canvas,
                cx = bx,
                cy = by - (bossRadius * 1.7f),
                barWidth = bossRadius * 3.4f,
                barHeight = 10f,
                fraction = boss.healthFraction,
                isBoss = true,
                bossName = boss.name,
                phase = boss.currentPhase
            )
        }
    }

    /**
     * Draws an animated monster portrait avatar inside circular UI frames
     * for codex screens, dialogs, stage preparation, and cinematic overlays.
     */
    fun drawEnemyAvatar(
        canvas: Canvas,
        visualType: EnemyVisualType,
        cx: Float,
        cy: Float,
        radius: Float,
        animTime: Float,
        isDying: Boolean = false
    ) {
        // 1. Dark fantasy circular backing
        fillPaint.color = Color.parseColor("#0B0F19")
        canvas.drawCircle(cx, cy, radius, fillPaint)

        // 2. Elemental inner glow
        glowPaint.color = visualType.primaryColorInt
        glowPaint.alpha = 50
        canvas.drawCircle(cx, cy, radius * 0.88f, glowPaint)

        // 3. Clip circular bounds for character bust
        canvas.save()
        reusablePath.reset()
        reusablePath.addCircle(cx, cy, radius * 0.94f, Path.Direction.CW)
        canvas.clipPath(reusablePath)

        // Translate to portrait center and render bust
        canvas.save()
        canvas.translate(cx, cy + (radius * 0.15f))
        val bustScale = radius * 0.85f

        when (visualType) {
            EnemyVisualType.GOBLIN -> drawGoblin(
                canvas = canvas,
                size = bustScale,
                time = animTime,
                state = if (isDying) EnemyAnimState.DEATH else EnemyAnimState.IDLE,
                isHit = false,
                isDying = isDying
            )
            EnemyVisualType.SKELETON -> drawSkeleton(
                canvas = canvas,
                size = bustScale,
                time = animTime,
                state = if (isDying) EnemyAnimState.DEATH else EnemyAnimState.IDLE,
                isHit = false,
                isDying = isDying
            )
            EnemyVisualType.ORC -> drawOrc(
                canvas = canvas,
                size = bustScale,
                time = animTime,
                state = if (isDying) EnemyAnimState.DEATH else EnemyAnimState.IDLE,
                isHit = false,
                isDying = isDying
            )
            EnemyVisualType.DARK_MAGE -> drawDarkMage(
                canvas = canvas,
                size = bustScale,
                time = animTime,
                state = if (isDying) EnemyAnimState.DEATH else EnemyAnimState.IDLE,
                isHit = false,
                isDying = isDying
            )
            EnemyVisualType.OBSIDIAN_GOLEM -> drawObsidianGolem(
                canvas = canvas,
                size = bustScale,
                animTime = animTime,
                isVulnerable = false,
                isTelegraphing = false,
                isHit = false,
                isDying = isDying
            )
            EnemyVisualType.SHADOW_QUEEN -> drawShadowQueen(
                canvas = canvas,
                size = bustScale,
                animTime = animTime,
                isProtected = false,
                isVulnerable = false,
                isTelegraphing = false,
                isHit = false,
                isDying = isDying
            )
            EnemyVisualType.ASTRAL_DRAGON -> drawAstralDragon(
                canvas = canvas,
                size = bustScale * 1.1f,
                animTime = animTime,
                phase = 1,
                isTelegraphing = false,
                isHit = false,
                isDying = isDying
            )
        }
        canvas.restore()
        canvas.restore()

        // 4. Ornate circular rim
        strokePaint.color = visualType.accentColorInt
        strokePaint.strokeWidth = 4f
        canvas.drawCircle(cx, cy, radius - 2f, strokePaint)

        strokePaint.color = Color.parseColor("#1E293B")
        strokePaint.strokeWidth = 2f
        canvas.drawCircle(cx, cy, radius * 0.95f, strokePaint)
    }

    // =========================================================================
    // 1. Goblin — Saqueador Selvagem
    // =========================================================================

    private fun drawGoblin(
        canvas: Canvas,
        size: Float,
        time: Float,
        state: EnemyAnimState,
        isHit: Boolean,
        isDying: Boolean = false
    ) {
        val isDefeated = isDying || state.isDying
        val runCycle = sin(time * 14f)
        val legSwing = if (state.isMoving && !isDefeated) runCycle * (size * 0.35f) else 0f
        val bobbing = if (state.isMoving && !isDefeated) abs(cos(time * 14f)) * (size * 0.12f) else sin(time * 3f) * (size * 0.05f)

        // Drop shadow
        canvas.drawOval(
            -size * 0.8f, size * 0.85f,
            size * 0.8f, size * 1.15f,
            shadowPaint
        )

        // Skin & Armor Colors
        val skinColor = if (isHit && !isDefeated) Color.parseColor("#EF4444") else if (isDefeated) Color.parseColor("#4B5563") else Color.parseColor("#16A34A")
        val skinShadow = if (isHit && !isDefeated) Color.parseColor("#B91C1C") else if (isDefeated) Color.parseColor("#374151") else Color.parseColor("#15803D")
        val leatherColor = if (isDefeated) Color.parseColor("#44403C") else Color.parseColor("#78350F")
        val ironColor = if (isDefeated) Color.parseColor("#64748B") else Color.parseColor("#94A3B8")

        // 1. Legs (Running articulation)
        strokePaint.color = skinShadow
        strokePaint.strokeWidth = size * 0.22f
        // Back leg
        canvas.drawLine(-size * 0.25f, size * 0.3f - bobbing, -size * 0.25f - legSwing, size * 0.9f, strokePaint)
        // Front leg
        strokePaint.color = skinColor
        canvas.drawLine(size * 0.25f, size * 0.3f - bobbing, size * 0.25f + legSwing, size * 0.9f, strokePaint)

        // 2. Leather Torso
        fillPaint.color = leatherColor
        reusableRectF.set(-size * 0.45f, -size * 0.2f - bobbing, size * 0.45f, size * 0.45f - bobbing)
        canvas.drawRoundRect(reusableRectF, size * 0.15f, size * 0.15f, fillPaint)

        // Leather cross-belt
        strokePaint.color = Color.parseColor("#451A03")
        strokePaint.strokeWidth = size * 0.10f
        canvas.drawLine(-size * 0.35f, -size * 0.15f - bobbing, size * 0.35f, size * 0.35f - bobbing, strokePaint)

        // 3. Spiked Shoulder Pad (Left arm)
        fillPaint.color = ironColor
        canvas.drawCircle(-size * 0.45f, -size * 0.10f - bobbing, size * 0.20f, fillPaint)
        // Iron spike
        strokePaint.color = Color.parseColor("#CBD5E1")
        strokePaint.strokeWidth = 3f
        canvas.drawLine(-size * 0.45f, -size * 0.10f - bobbing, -size * 0.65f, -size * 0.25f - bobbing, strokePaint)

        // 4. Goblin Head
        fillPaint.color = skinColor
        canvas.drawCircle(0f, -size * 0.45f - bobbing, size * 0.48f, fillPaint)

        // Pointed Ears (flapping during movement)
        val earWiggle = sin(time * 14f) * 6f
        // Right ear
        reusablePath.reset()
        reusablePath.moveTo(size * 0.35f, -size * 0.45f - bobbing)
        reusablePath.lineTo(size * 1.05f, -size * 0.70f - bobbing + earWiggle)
        reusablePath.lineTo(size * 0.40f, -size * 0.20f - bobbing)
        reusablePath.close()
        fillPaint.color = skinColor
        canvas.drawPath(reusablePath, fillPaint)
        // Inner ear pinkish
        fillPaint.color = Color.parseColor("#86EFAC")
        canvas.drawCircle(size * 0.55f, -size * 0.42f - bobbing, size * 0.12f, fillPaint)

        // Left ear
        reusablePath.reset()
        reusablePath.moveTo(-size * 0.35f, -size * 0.45f - bobbing)
        reusablePath.lineTo(-size * 1.05f, -size * 0.70f - bobbing - earWiggle)
        reusablePath.lineTo(-size * 0.40f, -size * 0.20f - bobbing)
        reusablePath.close()
        fillPaint.color = skinShadow
        canvas.drawPath(reusablePath, fillPaint)

        // 5. Menacing Eyes (Fiery yellow with black slit)
        fillPaint.color = Color.parseColor("#FACC15")
        canvas.drawCircle(size * 0.18f, -size * 0.48f - bobbing, size * 0.14f, fillPaint)
        canvas.drawCircle(-size * 0.18f, -size * 0.48f - bobbing, size * 0.14f, fillPaint)

        fillPaint.color = Color.parseColor("#18181B")
        canvas.drawOval(size * 0.16f, -size * 0.55f - bobbing, size * 0.20f, -size * 0.41f - bobbing, fillPaint)
        canvas.drawOval(-size * 0.20f, -size * 0.55f - bobbing, -size * 0.16f, -size * 0.41f - bobbing, fillPaint)

        // Sharp Lower Tusks
        fillPaint.color = Color.WHITE
        canvas.drawCircle(size * 0.15f, -size * 0.28f - bobbing, size * 0.08f, fillPaint)
        canvas.drawCircle(-size * 0.15f, -size * 0.28f - bobbing, size * 0.08f, fillPaint)

        // 6. Right Arm & Jagged Dagger (Thrusting forward)
        val armThrust = if (state.isAttacking) size * 0.45f else sin(time * 14f) * (size * 0.20f)
        val handX = size * 0.45f + armThrust
        val handY = size * 0.05f - bobbing

        // Arm
        strokePaint.color = skinColor
        strokePaint.strokeWidth = size * 0.16f
        canvas.drawLine(size * 0.35f, -size * 0.1f - bobbing, handX, handY, strokePaint)

        // Jagged Dagger Blade
        fillPaint.color = ironColor
        reusablePath.reset()
        reusablePath.moveTo(handX, handY - size * 0.08f)
        reusablePath.lineTo(handX + size * 0.65f, handY - size * 0.18f)
        reusablePath.lineTo(handX + size * 0.50f, handY + size * 0.05f)
        reusablePath.lineTo(handX + size * 0.30f, handY + size * 0.02f)
        reusablePath.close()
        canvas.drawPath(reusablePath, fillPaint)

        // Dagger guard & hilt
        strokePaint.color = Color.parseColor("#78350F")
        strokePaint.strokeWidth = size * 0.08f
        canvas.drawLine(handX - size * 0.10f, handY + size * 0.10f, handX + size * 0.05f, handY - size * 0.15f, strokePaint)
    }

    // =========================================================================
    // 2. Skeleton — Guerreiro Amaldiçoado
    // =========================================================================

    private fun drawSkeleton(
        canvas: Canvas,
        size: Float,
        time: Float,
        state: EnemyAnimState,
        isHit: Boolean,
        isDying: Boolean = false
    ) {
        val isDefeated = isDying || state.isDying
        val walkCycle = sin(time * 7f)
        val legStride = if (state.isMoving && !isDefeated) walkCycle * (size * 0.30f) else 0f
        val bob = if (state.isMoving && !isDefeated) abs(cos(time * 7f)) * (size * 0.08f) else sin(time * 2.5f) * (size * 0.04f)

        // Drop shadow
        canvas.drawOval(-size * 0.75f, size * 0.85f, size * 0.75f, size * 1.15f, shadowPaint)

        val boneColor = if (isHit && !isDefeated) Color.parseColor("#FCA5A5") else if (isDefeated) Color.parseColor("#64748B") else Color.parseColor("#E2E8F0")
        val boneShadow = if (isHit && !isDefeated) Color.parseColor("#EF4444") else if (isDefeated) Color.parseColor("#475569") else Color.parseColor("#94A3B8")
        val ironColor = if (isDefeated) Color.parseColor("#334155") else Color.parseColor("#475569")
        val cursedCyan = if (isDefeated) Color.parseColor("#475569") else Color.parseColor("#06B6D4")

        // 1. Articulated Bony Legs
        strokePaint.color = boneShadow
        strokePaint.strokeWidth = size * 0.12f
        // Left leg
        canvas.drawLine(-size * 0.22f, size * 0.35f - bob, -size * 0.22f - legStride, size * 0.90f, strokePaint)
        // Right leg
        strokePaint.color = boneColor
        canvas.drawLine(size * 0.22f, size * 0.35f - bob, size * 0.22f + legStride, size * 0.90f, strokePaint)

        // 2. Spine & Ribcage
        fillPaint.color = Color.parseColor("#0F172A") // Dark hollow interior
        canvas.drawRect(-size * 0.30f, -size * 0.15f - bob, size * 0.30f, size * 0.32f - bob, fillPaint)

        // Ribs
        strokePaint.color = boneColor
        strokePaint.strokeWidth = size * 0.09f
        canvas.drawLine(-size * 0.35f, -size * 0.10f - bob, size * 0.35f, -size * 0.10f - bob, strokePaint)
        canvas.drawLine(-size * 0.32f, size * 0.04f - bob, size * 0.32f, size * 0.04f - bob, strokePaint)
        canvas.drawLine(-size * 0.28f, size * 0.18f - bob, size * 0.28f, size * 0.18f - bob, strokePaint)

        // Rusted Iron Breastplate Strap
        strokePaint.color = ironColor
        strokePaint.strokeWidth = size * 0.08f
        canvas.drawLine(-size * 0.32f, -size * 0.15f - bob, size * 0.30f, size * 0.25f - bob, strokePaint)

        // 3. Ancient Round Shield (Left Arm)
        val shieldX = -size * 0.45f
        val shieldY = size * 0.05f - bob
        val shieldR = size * 0.38f

        fillPaint.color = Color.parseColor("#78350F") // Dark wood
        canvas.drawCircle(shieldX, shieldY, shieldR, fillPaint)
        strokePaint.color = ironColor // Iron rim
        strokePaint.strokeWidth = size * 0.09f
        canvas.drawCircle(shieldX, shieldY, shieldR, strokePaint)
        // Center iron boss
        fillPaint.color = Color.parseColor("#94A3B8")
        canvas.drawCircle(shieldX, shieldY, size * 0.12f, fillPaint)

        // 4. Skeleton Skull
        fillPaint.color = boneColor
        canvas.drawCircle(0f, -size * 0.45f - bob, size * 0.42f, fillPaint)

        // Jawbone
        fillPaint.color = boneShadow
        reusableRectF.set(-size * 0.22f, -size * 0.24f - bob, size * 0.22f, -size * 0.10f - bob)
        canvas.drawRoundRect(reusableRectF, size * 0.08f, size * 0.08f, fillPaint)

        // Teeth lines
        strokePaint.color = Color.parseColor("#0F172A")
        strokePaint.strokeWidth = 2f
        canvas.drawLine(-size * 0.12f, -size * 0.22f - bob, -size * 0.12f, -size * 0.12f - bob, strokePaint)
        canvas.drawLine(0f, -size * 0.22f - bob, 0f, -size * 0.12f - bob, strokePaint)
        canvas.drawLine(size * 0.12f, -size * 0.22f - bob, size * 0.12f, -size * 0.12f - bob, strokePaint)

        // Hollow Eye Sockets
        fillPaint.color = Color.parseColor("#020617")
        canvas.drawCircle(-size * 0.16f, -size * 0.46f - bob, size * 0.14f, fillPaint)
        canvas.drawCircle(size * 0.16f, -size * 0.46f - bob, size * 0.14f, fillPaint)

        // Flickering Cursed Soul Flame Eyes
        val flicker = 1f + sin(time * 12f) * 0.2f
        glowPaint.color = cursedCyan
        canvas.drawCircle(-size * 0.16f, -size * 0.46f - bob, size * 0.08f * flicker, glowPaint)
        canvas.drawCircle(size * 0.16f, -size * 0.46f - bob, size * 0.08f * flicker, glowPaint)

        // 5. Right Arm & Chipped Ancient Broadsword
        val swordSwing = if (state.isAttacking) size * 0.55f else sin(time * 7f) * (size * 0.15f)
        val sHandX = size * 0.40f + swordSwing
        val sHandY = size * 0.05f - bob

        // Bony arm
        strokePaint.color = boneColor
        strokePaint.strokeWidth = size * 0.11f
        canvas.drawLine(size * 0.28f, -size * 0.10f - bob, sHandX, sHandY, strokePaint)

        // Chipped Iron Sword Blade
        strokePaint.color = Color.parseColor("#CBD5E1")
        strokePaint.strokeWidth = size * 0.14f
        canvas.drawLine(sHandX, sHandY, sHandX + size * 0.65f, sHandY - size * 0.45f, strokePaint)

        // Crossguard
        strokePaint.color = Color.parseColor("#B45309")
        strokePaint.strokeWidth = size * 0.08f
        canvas.drawLine(sHandX - size * 0.08f, sHandY - size * 0.10f, sHandX + size * 0.12f, sHandY + size * 0.10f, strokePaint)

        // Attack trail
        if (state.isAttacking) {
            trailPaint.color = cursedCyan
            trailPaint.strokeWidth = 3f
            trailPaint.alpha = 180
            canvas.drawArc(
                sHandX - size * 0.4f, sHandY - size * 0.8f,
                sHandX + size * 1.0f, sHandY + size * 0.3f,
                -60f, 90f, false, trailPaint
            )
        }
    }

    // =========================================================================
    // 3. Orc — Brutamontes de Guerra
    // =========================================================================

    private fun drawOrc(
        canvas: Canvas,
        size: Float,
        time: Float,
        state: EnemyAnimState,
        isHit: Boolean,
        isDying: Boolean = false
    ) {
        val isDefeated = isDying || state.isDying
        val stompCycle = sin(time * 5f)
        val stompStride = if (state.isMoving && !isDefeated) stompCycle * (size * 0.25f) else 0f
        val heavyBob = if (state.isMoving && !isDefeated) abs(cos(time * 5f)) * (size * 0.10f) else sin(time * 2f) * (size * 0.05f)

        // Drop shadow
        canvas.drawOval(-size * 0.95f, size * 0.85f, size * 0.95f, size * 1.25f, shadowPaint)

        val orcSkin = if (isHit && !isDefeated) Color.parseColor("#EF4444") else if (isDefeated) Color.parseColor("#475569") else Color.parseColor("#15803D")
        val orcShadow = if (isHit && !isDefeated) Color.parseColor("#991B1B") else if (isDefeated) Color.parseColor("#334155") else Color.parseColor("#14532D")
        val heavyIron = if (isDefeated) Color.parseColor("#334155") else Color.parseColor("#1E293B")
        val warPaintRed = if (isDefeated) Color.parseColor("#7F1D1D") else Color.parseColor("#DC2626")

        // 1. Massive Armored Legs
        strokePaint.color = orcShadow
        strokePaint.strokeWidth = size * 0.32f
        // Left leg
        canvas.drawLine(-size * 0.35f, size * 0.25f - heavyBob, -size * 0.35f - stompStride, size * 0.88f, strokePaint)
        // Right leg
        strokePaint.color = orcSkin
        canvas.drawLine(size * 0.35f, size * 0.25f - heavyBob, size * 0.35f + stompStride, size * 0.88f, strokePaint)

        // Iron knee guards
        fillPaint.color = heavyIron
        canvas.drawCircle(-size * 0.35f - (stompStride * 0.5f), size * 0.55f - heavyBob, size * 0.18f, fillPaint)
        canvas.drawCircle(size * 0.35f + (stompStride * 0.5f), size * 0.55f - heavyBob, size * 0.18f, fillPaint)

        // 2. Hulking Muscular Torso
        fillPaint.color = orcSkin
        reusableRectF.set(-size * 0.65f, -size * 0.25f - heavyBob, size * 0.65f, size * 0.40f - heavyBob)
        canvas.drawRoundRect(reusableRectF, size * 0.20f, size * 0.20f, fillPaint)

        // Studded Iron Belt
        fillPaint.color = heavyIron
        canvas.drawRect(-size * 0.60f, size * 0.20f - heavyBob, size * 0.60f, size * 0.38f - heavyBob, fillPaint)
        fillPaint.color = Color.parseColor("#F59E0B") // Gold skull buckle
        canvas.drawCircle(0f, size * 0.29f - heavyBob, size * 0.14f, fillPaint)

        // 3. Spiked Iron Pauldrons (Shoulders)
        fillPaint.color = heavyIron
        canvas.drawCircle(-size * 0.68f, -size * 0.18f - heavyBob, size * 0.30f, fillPaint)
        canvas.drawCircle(size * 0.68f, -size * 0.18f - heavyBob, size * 0.30f, fillPaint)

        // Pauldron Spikes
        strokePaint.color = Color.parseColor("#94A3B8")
        strokePaint.strokeWidth = 4f
        canvas.drawLine(-size * 0.68f, -size * 0.25f - heavyBob, -size * 0.95f, -size * 0.45f - heavyBob, strokePaint)
        canvas.drawLine(size * 0.68f, -size * 0.25f - heavyBob, size * 0.95f, -size * 0.45f - heavyBob, strokePaint)

        // 4. Orc Head & Jaw
        fillPaint.color = orcSkin
        canvas.drawCircle(0f, -size * 0.48f - heavyBob, size * 0.50f, fillPaint)

        // Massive Square Lower Jaw
        fillPaint.color = orcShadow
        reusableRectF.set(-size * 0.38f, -size * 0.32f - heavyBob, size * 0.38f, -size * 0.08f - heavyBob)
        canvas.drawRoundRect(reusableRectF, size * 0.12f, size * 0.12f, fillPaint)

        // Huge Upward Lower Tusks
        fillPaint.color = Color.parseColor("#F8FAFC")
        reusablePath.reset()
        reusablePath.moveTo(size * 0.20f, -size * 0.12f - heavyBob)
        reusablePath.lineTo(size * 0.32f, -size * 0.38f - heavyBob)
        reusablePath.lineTo(size * 0.12f, -size * 0.22f - heavyBob)
        reusablePath.close()
        canvas.drawPath(reusablePath, fillPaint)

        reusablePath.reset()
        reusablePath.moveTo(-size * 0.20f, -size * 0.12f - heavyBob)
        reusablePath.lineTo(-size * 0.32f, -size * 0.38f - heavyBob)
        reusablePath.lineTo(-size * 0.12f, -size * 0.22f - heavyBob)
        reusablePath.close()
        canvas.drawPath(reusablePath, fillPaint)

        // Crimson War Paint Chevron across brow
        strokePaint.color = warPaintRed
        strokePaint.strokeWidth = size * 0.12f
        canvas.drawLine(-size * 0.35f, -size * 0.55f - heavyBob, 0f, -size * 0.42f - heavyBob, strokePaint)
        canvas.drawLine(0f, -size * 0.42f - heavyBob, size * 0.35f, -size * 0.55f - heavyBob, strokePaint)

        // Piercing red eyes
        fillPaint.color = Color.parseColor("#EF4444")
        canvas.drawCircle(size * 0.18f, -size * 0.46f - heavyBob, size * 0.10f, fillPaint)
        canvas.drawCircle(-size * 0.18f, -size * 0.46f - heavyBob, size * 0.10f, fillPaint)

        // 5. Giant Double-Headed Executioner's Battleaxe
        val axeChop = if (state.isAttacking) size * 0.60f else sin(time * 5f) * (size * 0.18f)
        val axX = size * 0.55f + axeChop
        val axY = -size * 0.10f - heavyBob

        // Long shaft
        strokePaint.color = Color.parseColor("#78350F")
        strokePaint.strokeWidth = size * 0.14f
        canvas.drawLine(axX - size * 0.25f, axY + size * 0.70f, axX + size * 0.35f, axY - size * 0.70f, strokePaint)

        // Double Axe Blade (Iron & Spikes)
        fillPaint.color = heavyIron
        reusablePath.reset()
        // Top blade
        reusablePath.moveTo(axX + size * 0.20f, axY - size * 0.45f)
        reusablePath.lineTo(axX + size * 0.85f, axY - size * 0.75f)
        reusablePath.lineTo(axX + size * 0.75f, axY - size * 0.25f)
        reusablePath.close()
        canvas.drawPath(reusablePath, fillPaint)

        // Bottom blade
        reusablePath.reset()
        reusablePath.moveTo(axX + size * 0.20f, axY - size * 0.45f)
        reusablePath.lineTo(axX - size * 0.45f, axY - size * 0.75f)
        reusablePath.lineTo(axX - size * 0.35f, axY - size * 0.25f)
        reusablePath.close()
        canvas.drawPath(reusablePath, fillPaint)

        // Sharp cutting edge highlights
        strokePaint.color = Color.parseColor("#E2E8F0")
        strokePaint.strokeWidth = 3f
        canvas.drawLine(axX + size * 0.85f, axY - size * 0.75f, axX + size * 0.75f, axY - size * 0.25f, strokePaint)
        canvas.drawLine(axX - size * 0.45f, axY - size * 0.75f, axX - size * 0.35f, axY - size * 0.25f, strokePaint)
    }

    // =========================================================================
    // 4. Dark Mage — Conjurador das Trevas
    // =========================================================================

    private fun drawDarkMage(
        canvas: Canvas,
        size: Float,
        time: Float,
        state: EnemyAnimState,
        isHit: Boolean,
        isDying: Boolean = false
    ) {
        val isDefeated = isDying || state.isDying
        val hover = if (isDefeated) 0f else sin(time * 3f) * (size * 0.14f)
        val robeWave = if (isDefeated) 0f else sin(time * 6f) * (size * 0.12f)

        // Floating shadow pool below
        canvas.drawOval(-size * 0.70f, size * 0.80f, size * 0.70f, size * 1.10f, shadowPaint)

        val robeColor = if (isHit && !isDefeated) Color.parseColor("#EF4444") else if (isDefeated) Color.parseColor("#334155") else Color.parseColor("#3B0764")
        val robeDark = if (isHit && !isDefeated) Color.parseColor("#991B1B") else if (isDefeated) Color.parseColor("#1E293B") else Color.parseColor("#1E1B4B")
        val arcaneViolet = if (isDefeated) Color.parseColor("#64748B") else Color.parseColor("#C084FC")
        val goldTrim = if (isDefeated) Color.parseColor("#78350F") else Color.parseColor("#F59E0B")

        // 1. Billowing Layered Robes
        fillPaint.color = robeDark
        reusablePath.reset()
        reusablePath.moveTo(-size * 0.45f, -size * 0.15f - hover)
        reusablePath.lineTo(size * 0.45f, -size * 0.15f - hover)
        reusablePath.lineTo(size * 0.65f + robeWave, size * 0.75f - hover)
        reusablePath.lineTo(-size * 0.65f - robeWave, size * 0.75f - hover)
        reusablePath.close()
        canvas.drawPath(reusablePath, fillPaint)

        // Front robe fold
        fillPaint.color = robeColor
        reusablePath.reset()
        reusablePath.moveTo(-size * 0.30f, -size * 0.10f - hover)
        reusablePath.lineTo(size * 0.30f, -size * 0.10f - hover)
        reusablePath.lineTo(size * 0.40f, size * 0.72f - hover)
        reusablePath.lineTo(-size * 0.40f, size * 0.72f - hover)
        reusablePath.close()
        canvas.drawPath(reusablePath, fillPaint)

        // Gold Runic Hem
        strokePaint.color = goldTrim
        strokePaint.strokeWidth = size * 0.08f
        canvas.drawLine(-size * 0.42f, size * 0.70f - hover, size * 0.42f, size * 0.70f - hover, strokePaint)

        // 2. Pointed Dark Cowl / Hood
        fillPaint.color = robeColor
        reusablePath.reset()
        reusablePath.moveTo(0f, -size * 0.95f - hover) // Tall pointed tip
        reusablePath.lineTo(size * 0.48f, -size * 0.35f - hover)
        reusablePath.lineTo(-size * 0.48f, -size * 0.35f - hover)
        reusablePath.close()
        canvas.drawPath(reusablePath, fillPaint)

        // Shadow inside hood
        fillPaint.color = Color.parseColor("#020617") // Deepest black void
        canvas.drawCircle(0f, -size * 0.42f - hover, size * 0.32f, fillPaint)

        // Glowing Arcane Violet Eyes
        val eyePulse = 1f + sin(time * 8f) * 0.2f
        glowPaint.color = arcaneViolet
        canvas.drawCircle(-size * 0.14f, -size * 0.42f - hover, size * 0.09f * eyePulse, glowPaint)
        canvas.drawCircle(size * 0.14f, -size * 0.42f - hover, size * 0.09f * eyePulse, glowPaint)

        // 3. Darkwood Staff with Levitating Crystal
        val staffRise = if (state.isAttacking) size * 0.30f else sin(time * 3f) * (size * 0.10f)
        val stX = size * 0.55f
        val stY = -hover - staffRise

        // Gnarled Staff
        strokePaint.color = Color.parseColor("#451A03")
        strokePaint.strokeWidth = size * 0.12f
        canvas.drawLine(stX, stY + size * 0.85f, stX, stY - size * 0.45f, strokePaint)

        // Staff Crescent Topper
        strokePaint.color = Color.parseColor("#78350F")
        strokePaint.strokeWidth = size * 0.08f
        canvas.drawArc(
            stX - size * 0.28f, stY - size * 0.75f,
            stX + size * 0.28f, stY - size * 0.25f,
            -150f, 180f, false, strokePaint
        )

        // Levitating Floating Amethyst Crystal
        val crystalBob = sin(time * 8f) * (size * 0.08f)
        fillPaint.color = Color.parseColor("#A855F7")
        canvas.drawCircle(stX, stY - size * 0.52f + crystalBob, size * 0.16f, fillPaint)

        // Inner glowing core
        glowPaint.color = Color.parseColor("#E9D5FF")
        canvas.drawCircle(stX, stY - size * 0.52f + crystalBob, size * 0.08f, glowPaint)

        // Spinning arcane orbit rings
        if (state.isAttacking) {
            strokePaint.color = arcaneViolet
            strokePaint.strokeWidth = 2.5f
            val ringW = size * 0.45f
            val ringH = size * 0.15f
            canvas.drawOval(
                stX - ringW, stY - size * 0.52f + crystalBob - ringH,
                stX + ringW, stY - size * 0.52f + crystalBob + ringH,
                strokePaint
            )
        }
    }

    // =========================================================================
    // 5. Boss 1 — Obsidian Golem (Colosso Vulcânico)
    // =========================================================================

    private fun drawObsidianGolem(
        canvas: Canvas,
        size: Float,
        animTime: Float,
        isVulnerable: Boolean,
        isTelegraphing: Boolean,
        isHit: Boolean,
        isDying: Boolean = false
    ) {
        // Drop shadow
        canvas.drawOval(-size * 1.1f, size * 0.85f, size * 1.1f, size * 1.35f, shadowPaint)

        val isDefeated = isDying
        val rockColor = if (isHit && !isDefeated) Color.parseColor("#EF4444") else if (isDefeated) Color.parseColor("#475569") else Color.parseColor("#1E293B") // Basalt rock
        val rockShadow = if (isDefeated) Color.parseColor("#1E293B") else Color.parseColor("#0F172A")
        val magmaColor = if (isDefeated) Color.parseColor("#64748B") else if (isVulnerable) Color.parseColor("#FDE047") else Color.parseColor("#EA580C")
        val magmaCore = if (isDefeated) Color.parseColor("#94A3B8") else if (isVulnerable) Color.parseColor("#FFFFFF") else Color.parseColor("#F97316")

        val pulse = 1f + sin(animTime * 6f) * 0.08f
        val slamLift = if (isTelegraphing) -size * 0.45f else 0f

        // 1. Massive Colossal Rocky Legs
        fillPaint.color = rockShadow
        canvas.drawRect(-size * 0.65f, size * 0.35f, -size * 0.20f, size * 0.95f, fillPaint)
        canvas.drawRect(size * 0.20f, size * 0.35f, size * 0.65f, size * 0.95f, fillPaint)

        // 2. Basalt Torso Plates
        fillPaint.color = rockColor
        canvas.drawCircle(0f, 0f, size * 0.70f, fillPaint)

        // Magma Veins & Fissures
        strokePaint.color = magmaColor
        strokePaint.strokeWidth = size * 0.10f * pulse
        canvas.drawLine(-size * 0.45f, -size * 0.35f, 0f, 0f, strokePaint)
        canvas.drawLine(size * 0.45f, -size * 0.35f, 0f, 0f, strokePaint)
        canvas.drawLine(0f, 0f, 0f, size * 0.45f, strokePaint)

        // 3. Molten Lava Heart Core (Exposed and blazing if vulnerable)
        val coreSize = if (isVulnerable) size * 0.42f else size * 0.25f
        fillPaint.color = magmaColor
        canvas.drawCircle(0f, 0f, coreSize * pulse, fillPaint)
        fillPaint.color = magmaCore
        canvas.drawCircle(0f, 0f, (coreSize * 0.55f) * pulse, fillPaint)

        // 4. Rocky Spiked Shoulders & Raised Fists
        fillPaint.color = rockColor
        canvas.drawCircle(-size * 0.85f, -size * 0.25f + slamLift, size * 0.38f, fillPaint)
        canvas.drawCircle(size * 0.85f, -size * 0.25f + slamLift, size * 0.38f, fillPaint)

        // Boulder Fists
        fillPaint.color = rockShadow
        canvas.drawCircle(-size * 0.95f, size * 0.25f + slamLift, size * 0.32f, fillPaint)
        canvas.drawCircle(size * 0.95f, size * 0.25f + slamLift, size * 0.32f, fillPaint)

        // 5. Basalt Crown & Head
        fillPaint.color = rockColor
        canvas.drawCircle(0f, -size * 0.65f, size * 0.42f, fillPaint)

        // Fiery Molten Eyes
        fillPaint.color = Color.parseColor("#FEF08A")
        canvas.drawCircle(-size * 0.16f, -size * 0.65f, size * 0.11f, fillPaint)
        canvas.drawCircle(size * 0.16f, -size * 0.65f, size * 0.11f, fillPaint)

        // Rocky Spikes on Head
        fillPaint.color = rockShadow
        canvas.drawCircle(-size * 0.30f, -size * 0.90f, size * 0.14f, fillPaint)
        canvas.drawCircle(0f, -size * 0.98f, size * 0.16f, fillPaint)
        canvas.drawCircle(size * 0.30f, -size * 0.90f, size * 0.14f, fillPaint)
    }

    // =========================================================================
    // 6. Boss 2 — Shadow Queen (Soberana das Sombras)
    // =========================================================================

    private fun drawShadowQueen(
        canvas: Canvas,
        size: Float,
        animTime: Float,
        isProtected: Boolean,
        isVulnerable: Boolean,
        isTelegraphing: Boolean,
        isHit: Boolean,
        isDying: Boolean = false
    ) {
        val isDefeated = isDying
        val hover = if (isDefeated) 0f else sin(animTime * 3.5f) * (size * 0.12f)
        val capeWave = if (isDefeated) 0f else sin(animTime * 6f) * (size * 0.18f)

        // Drop shadow
        canvas.drawOval(-size * 0.85f, size * 0.85f, size * 0.85f, size * 1.25f, shadowPaint)

        val armorColor = if (isHit && !isDefeated) Color.parseColor("#EF4444") else if (isDefeated) Color.parseColor("#1E293B") else Color.parseColor("#0F172A")
        val voidViolet = if (isDefeated) Color.parseColor("#3B0764") else if (isProtected) Color.parseColor("#7E22CE") else Color.parseColor("#6B21A8")
        val glowViolet = if (isDefeated) Color.parseColor("#64748B")
            else if (isVulnerable) Color.parseColor("#FDE047")
            else if (isProtected) Color.parseColor("#F0ABFC")
            else Color.parseColor("#C084FC")
        val goldCrown = if (isDefeated) Color.parseColor("#78350F")
            else if (isVulnerable) Color.parseColor("#FEF08A")
            else Color.parseColor("#F59E0B")

        // 1. Ethereal Shadow Cape (Large flowing wings of darkness)
        fillPaint.color = voidViolet
        reusablePath.reset()
        reusablePath.moveTo(0f, -size * 0.40f - hover)
        reusablePath.lineTo(-size * 1.15f - capeWave, size * 0.70f - hover)
        reusablePath.lineTo(-size * 0.50f, size * 0.95f - hover)
        reusablePath.lineTo(0f, size * 0.75f - hover)
        reusablePath.lineTo(size * 0.50f, size * 0.95f - hover)
        reusablePath.lineTo(size * 1.15f + capeWave, size * 0.70f - hover)
        reusablePath.close()
        canvas.drawPath(reusablePath, fillPaint)

        // 2. Gothic Slender Plate Torso
        fillPaint.color = armorColor
        reusableRectF.set(-size * 0.38f, -size * 0.35f - hover, size * 0.38f, size * 0.45f - hover)
        canvas.drawRoundRect(reusableRectF, size * 0.16f, size * 0.16f, fillPaint)

        // Violet energy corset line
        strokePaint.color = glowViolet
        strokePaint.strokeWidth = size * 0.08f
        canvas.drawLine(0f, -size * 0.30f - hover, 0f, size * 0.35f - hover, strokePaint)

        // Protective Shadow Veil Ward (if protected)
        if (isProtected && !isDefeated) {
            val shieldPulse = 1f + sin(animTime * 8f) * 0.06f
            strokePaint.color = Color.parseColor("#E879F9")
            strokePaint.strokeWidth = 3f
            canvas.drawCircle(0f, -size * 0.30f - hover, size * 0.90f * shieldPulse, strokePaint)
            glowPaint.color = Color.parseColor("#A855F7")
            glowPaint.alpha = 45
            canvas.drawCircle(0f, -size * 0.30f - hover, size * 0.85f * shieldPulse, glowPaint)
        }

        // Vulnerable Fractured Core & Weak Point Exposure (if vulnerable)
        if (isVulnerable && !isDefeated) {
            val vulnPulse = 1f + cos(animTime * 10f) * 0.08f
            // Luminous golden-yellow fissure lines across dark armor
            strokePaint.color = Color.parseColor("#FDE047")
            strokePaint.strokeWidth = 2.5f
            canvas.drawLine(-size * 0.28f, -size * 0.25f - hover, size * 0.22f, size * 0.15f - hover, strokePaint)
            canvas.drawLine(size * 0.22f, -size * 0.20f - hover, -size * 0.16f, size * 0.28f - hover, strokePaint)
            // Blazing exposed heart crystal
            glowPaint.color = Color.WHITE
            glowPaint.alpha = 90
            canvas.drawCircle(0f, -size * 0.05f - hover, size * 0.18f * vulnPulse, glowPaint)
        }

        // 3. Void Blade / Scepter
        val bladeRaise = if (isTelegraphing) -size * 0.35f else 0f
        strokePaint.color = glowViolet
        strokePaint.strokeWidth = size * 0.10f
        canvas.drawLine(size * 0.45f, -size * 0.20f - hover + bladeRaise, size * 0.85f, -size * 0.85f - hover + bladeRaise, strokePaint)
        // Blade glow
        glowPaint.color = Color.WHITE
        canvas.drawCircle(size * 0.85f, -size * 0.85f - hover + bladeRaise, size * 0.10f, glowPaint)

        // 4. Queen's Head & Face
        fillPaint.color = Color.parseColor("#E2E8F0") // Pale porcelain skin
        canvas.drawCircle(0f, -size * 0.52f - hover, size * 0.34f, fillPaint)

        // Piercing Glowing Violet Eyes
        fillPaint.color = glowViolet
        canvas.drawCircle(-size * 0.12f, -size * 0.54f - hover, size * 0.08f, fillPaint)
        canvas.drawCircle(size * 0.12f, -size * 0.54f - hover, size * 0.08f, fillPaint)

        // 5. Tall Jagged Shadow Crown
        fillPaint.color = goldCrown
        reusablePath.reset()
        reusablePath.moveTo(-size * 0.35f, -size * 0.65f - hover)
        reusablePath.lineTo(-size * 0.28f, -size * 1.05f - hover)
        reusablePath.lineTo(-size * 0.10f, -size * 0.72f - hover)
        reusablePath.lineTo(0f, -size * 1.15f - hover) // Center tallest spike
        reusablePath.lineTo(size * 0.10f, -size * 0.72f - hover)
        reusablePath.lineTo(size * 0.28f, -size * 1.05f - hover)
        reusablePath.lineTo(size * 0.35f, -size * 0.65f - hover)
        reusablePath.close()
        canvas.drawPath(reusablePath, fillPaint)

        // Crown Violet Gem
        fillPaint.color = glowViolet
        canvas.drawCircle(0f, -size * 0.80f - hover, size * 0.10f, fillPaint)
    }

    // =========================================================================
    // 7. Boss 3 — Astral Dragon (Dragão Astral Ancestral)
    // =========================================================================

    private fun drawAstralDragon(
        canvas: Canvas,
        size: Float,
        animTime: Float,
        phase: Int,
        isTelegraphing: Boolean,
        isHit: Boolean,
        isDying: Boolean = false
    ) {
        val isDefeated = isDying
        val wingFlap = if (isDefeated) 0f else sin(animTime * 4.5f) * (size * 0.40f)
        val bodySine = if (isDefeated) 0f else sin(animTime * 2.5f) * (size * 0.08f)

        // Drop shadow
        canvas.drawOval(-size * 1.2f, size * 0.90f, size * 1.2f, size * 1.40f, shadowPaint)

        val isPhase2 = phase == 2
        val scaleColor = if (isHit && !isDefeated) Color.parseColor("#EF4444") else if (isDefeated) Color.parseColor("#1E293B") else if (isPhase2) Color.parseColor("#9F1239") else Color.parseColor("#0B132B")
        val wingColor = if (isDefeated) Color.parseColor("#334155") else if (isPhase2) Color.parseColor("#E11D48") else Color.parseColor("#0369A1")
        val starlight = if (isDefeated) Color.parseColor("#64748B") else if (isPhase2) Color.parseColor("#FDA4AF") else Color.parseColor("#38BDF8")
        val novaGlow = if (isDefeated) Color.parseColor("#94A3B8") else if (isPhase2) Color.parseColor("#FFE4E6") else Color.parseColor("#F0F9FF")

        // 1. Serpentine Draconic Tail
        strokePaint.color = scaleColor
        strokePaint.strokeWidth = size * 0.20f
        reusablePath.reset()
        reusablePath.moveTo(0f, size * 0.45f + bodySine)
        reusablePath.quadTo(-size * 0.65f, size * 0.75f, -size * 0.95f, size * 1.15f)
        canvas.drawPath(reusablePath, strokePaint)

        // Starlight tail tip
        glowPaint.color = starlight
        canvas.drawCircle(-size * 0.95f, size * 1.15f, size * 0.12f, glowPaint)

        // 2. Colossal Articulated Wings with Constellations
        // Left Wing
        fillPaint.color = wingColor
        fillPaint.alpha = 220
        reusablePath.reset()
        reusablePath.moveTo(-size * 0.20f, -size * 0.15f + bodySine)
        reusablePath.lineTo(-size * 1.45f, -size * 0.90f + wingFlap)
        reusablePath.lineTo(-size * 1.25f, size * 0.15f + (wingFlap * 0.5f))
        reusablePath.lineTo(-size * 0.40f, size * 0.35f + bodySine)
        reusablePath.close()
        canvas.drawPath(reusablePath, fillPaint)

        // Right Wing
        reusablePath.reset()
        reusablePath.moveTo(size * 0.20f, -size * 0.15f + bodySine)
        reusablePath.lineTo(size * 1.45f, -size * 0.90f - wingFlap)
        reusablePath.lineTo(size * 1.25f, size * 0.15f - (wingFlap * 0.5f))
        reusablePath.lineTo(size * 0.40f, size * 0.35f + bodySine)
        reusablePath.close()
        canvas.drawPath(reusablePath, fillPaint)
        fillPaint.alpha = 255

        // Wing bone struts
        strokePaint.color = scaleColor
        strokePaint.strokeWidth = size * 0.10f
        canvas.drawLine(0f, -size * 0.15f + bodySine, -size * 1.45f, -size * 0.90f + wingFlap, strokePaint)
        canvas.drawLine(0f, -size * 0.15f + bodySine, size * 1.45f, -size * 0.90f - wingFlap, strokePaint)

        // Constellation starlight stars on wings
        fillPaint.color = novaGlow
        canvas.drawCircle(-size * 0.85f, -size * 0.40f + (wingFlap * 0.6f), size * 0.08f, fillPaint)
        canvas.drawCircle(-size * 1.15f, -size * 0.65f + (wingFlap * 0.8f), size * 0.06f, fillPaint)
        canvas.drawCircle(size * 0.85f, -size * 0.40f - (wingFlap * 0.6f), size * 0.08f, fillPaint)
        canvas.drawCircle(size * 1.15f, -size * 0.65f - (wingFlap * 0.8f), size * 0.06f, fillPaint)

        // 3. Dragon Muscular Body & Neck
        fillPaint.color = scaleColor
        canvas.drawCircle(0f, size * 0.10f + bodySine, size * 0.45f, fillPaint)
        canvas.drawCircle(0f, -size * 0.30f + bodySine, size * 0.35f, fillPaint)

        // 4. Fierce Dragon Head & Jaws
        val headRear = if (isTelegraphing) -size * 0.25f else 0f
        fillPaint.color = scaleColor
        reusableRectF.set(-size * 0.32f, -size * 0.85f + headRear, size * 0.32f, -size * 0.30f + headRear)
        canvas.drawRoundRect(reusableRectF, size * 0.15f, size * 0.15f, fillPaint)

        // Celestial / Solar Horns
        strokePaint.color = starlight
        strokePaint.strokeWidth = size * 0.12f
        // Left horn
        reusablePath.reset()
        reusablePath.moveTo(-size * 0.20f, -size * 0.70f + headRear)
        reusablePath.quadTo(-size * 0.65f, -size * 1.05f + headRear, -size * 0.55f, -size * 1.35f + headRear)
        canvas.drawPath(reusablePath, strokePaint)
        // Right horn
        reusablePath.reset()
        reusablePath.moveTo(size * 0.20f, -size * 0.70f + headRear)
        reusablePath.quadTo(size * 0.65f, -size * 1.05f + headRear, size * 0.55f, -size * 1.35f + headRear)
        canvas.drawPath(reusablePath, strokePaint)

        // Supernova Eyes
        fillPaint.color = novaGlow
        canvas.drawCircle(-size * 0.16f, -size * 0.62f + headRear, size * 0.12f, fillPaint)
        canvas.drawCircle(size * 0.16f, -size * 0.62f + headRear, size * 0.12f, fillPaint)

        // Concentrating Astral Breath energy orb if telegraphing
        if (isTelegraphing) {
            val orbPulse = 1f + sin(animTime * 15f) * 0.3f
            glowPaint.color = starlight
            canvas.drawCircle(0f, -size * 0.95f + headRear, size * 0.25f * orbPulse, glowPaint)
            glowPaint.color = Color.WHITE
            canvas.drawCircle(0f, -size * 0.95f + headRear, size * 0.12f * orbPulse, glowPaint)
        }
    }

    // =========================================================================
    // Floating Health Bar Renderer
    // =========================================================================

    private fun drawFloatingHealthBar(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        barWidth: Float,
        barHeight: Float,
        fraction: Float,
        isBoss: Boolean,
        bossName: String? = null,
        phase: Int = 1
    ) {
        val barLeft = cx - (barWidth / 2f)
        val barTop = cy - (barHeight / 2f)
        val barRight = barLeft + barWidth
        val barBottom = barTop + barHeight

        // Background
        reusableRectF.set(barLeft, barTop, barRight, barBottom)
        canvas.drawRoundRect(reusableRectF, barHeight / 2f, barHeight / 2f, healthBarBgPaint)

        // Foreground Fill
        val fillW = barWidth * fraction
        if (fillW > 0f) {
            reusableRectF.set(barLeft, barTop, barLeft + fillW, barBottom)
            // Color gradient shift: green above 50%, yellow between 25-50%, red below 25%
            healthBarFgPaint.color = when {
                fraction > 0.50f -> Color.parseColor("#22C55E")
                fraction > 0.25f -> Color.parseColor("#F59E0B")
                else -> Color.parseColor("#EF4444")
            }
            canvas.drawRoundRect(reusableRectF, barHeight / 2f, barHeight / 2f, healthBarFgPaint)
        }

        // Frame Border
        reusableRectF.set(barLeft, barTop, barRight, barBottom)
        canvas.drawRoundRect(reusableRectF, barHeight / 2f, barHeight / 2f, healthBarBorderPaint)

        // Boss Text Overlays
        if (isBoss && bossName != null) {
            val phaseLabel = if (phase > 1) " (FASE $phase)" else ""
            canvas.drawText("$bossName$phaseLabel", cx, barTop - 8f, nameBadgePaint)
        }
    }
}
