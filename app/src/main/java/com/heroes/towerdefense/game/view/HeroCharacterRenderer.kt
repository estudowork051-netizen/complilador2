package com.heroes.towerdefense.game.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.heroes.towerdefense.game.model.DeployedHero
import com.heroes.towerdefense.game.model.HeroAnimState
import com.heroes.towerdefense.model.hero.Hero
import com.heroes.towerdefense.model.hero.HeroRarity
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance, professional 2D Canvas character renderer and animator
 * for all 6 canonical fantasy heroes in Heroes Tower Defense:
 * - Kael (Fire Vanguard / Warrior)
 * - Lyra (Arcane Marksman / Archer)
 * - Brakk (Heavy Mountain Sentinel / Guardian)
 * - Nyra (Twilight Shadow Assassin)
 * - Tiko (Royal Artificer / Engineer)
 * - Vesper (Astral Oracle / Cosmic Mage)
 *
 * Implements articulated skeletal animations (Idle breathing, Windup anticipation,
 * Strike execution with dynamic slash/energy trails, Recovery follow-through,
 * Skill channeling bursts, and Victory celebrations) with zero-allocation draw loops.
 */
object HeroCharacterRenderer {

    // =========================================================================
    // Reusable Paints (Allocated once, zero GC pressure during frame rendering)
    // =========================================================================

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#44000000")
        style = Paint.Style.FILL
    }

    private val auraRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }

    private val auraGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
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

    private val slashArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val effectGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val rangeCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(6f, 6f), 0f)
    }

    private val heroNameBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 18f
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
     * Draws the deployed hero on the active battlefield in StageOneCanvasView.
     */
    fun drawDeployedHero(canvas: Canvas, hero: DeployedHero, w: Float, h: Float) {
        val hx = hero.x * w
        val hy = hero.y * h
        val baseSize = w * 0.052f // Standard hero size unit

        // 1. Hero Range Indicator (Subtle tactical dashed ring)
        rangeCirclePaint.color = hero.hero.rarity.colorInt
        rangeCirclePaint.alpha = 40
        canvas.drawCircle(hx, hy, hero.range * w, rangeCirclePaint)

        // 2. Articulated Character Model & Animation
        drawHero(
            canvas = canvas,
            hero = hero.hero,
            cx = hx,
            cy = hy,
            size = baseSize,
            animTime = hero.animTimer,
            animState = hero.animState,
            stateTimer = hero.stateTimer,
            facingDirection = hero.facingDirection,
            isDeployed = true
        )
    }

    /**
     * Draws any hero character with full visual identity and articulated animations.
     */
    fun drawHero(
        canvas: Canvas,
        hero: Hero,
        cx: Float,
        cy: Float,
        size: Float,
        animTime: Float,
        animState: HeroAnimState = HeroAnimState.IDLE,
        stateTimer: Float = 0f,
        facingDirection: Float = 1f,
        isDeployed: Boolean = false
    ) {
        val s = size / 26f // Normalized scale factor

        // Orientation flip if facing left
        canvas.save()
        canvas.translate(cx, cy)
        if (facingDirection < 0f) {
            canvas.scale(-1f, 1f)
        }

        if (isDeployed) {
            // Tactical rarity ring beneath deployed hero on battlefield
            auraRingPaint.color = hero.rarity.colorInt
            auraRingPaint.alpha = 55
            canvas.drawCircle(0f, 20f * s, 16f * s, auraRingPaint)
        }

        when (hero.id) {
            "hero_kael" -> drawKael(canvas, s, animTime, animState, stateTimer, hero.rarity)
            "hero_lyra" -> drawLyra(canvas, s, animTime, animState, stateTimer, hero.rarity)
            "hero_brakk" -> drawBrakk(canvas, s, animTime, animState, stateTimer, hero.rarity)
            "hero_nyra" -> drawNyra(canvas, s, animTime, animState, stateTimer, hero.rarity)
            "hero_tiko" -> drawTiko(canvas, s, animTime, animState, stateTimer, hero.rarity)
            "hero_vesper" -> drawVesper(canvas, s, animTime, animState, stateTimer, hero.rarity)
            else -> drawKael(canvas, s, animTime, animState, stateTimer, hero.rarity)
        }

        canvas.restore()
    }

    /**
     * Draws a polished circular avatar portrait for hero cards & selection menus.
     */
    fun drawHeroAvatar(
        canvas: Canvas,
        hero: Hero,
        cx: Float,
        cy: Float,
        radius: Float,
        animTime: Float,
        isUnlocked: Boolean = true
    ) {
        // 1. Circular Avatar Frame Background
        fillPaint.color = Color.parseColor("#111827")
        canvas.drawCircle(cx, cy, radius, fillPaint)

        // Rarity ambient backdrop gradient/fill
        auraGlowPaint.color = hero.rarity.colorInt
        auraGlowPaint.alpha = if (isUnlocked) 55 else 25
        canvas.drawCircle(cx, cy, radius * 0.92f, auraGlowPaint)

        // 2. Draw Hero in Idle animation inside avatar
        val heroSize = radius * 0.72f
        drawHero(
            canvas = canvas,
            hero = hero,
            cx = cx,
            cy = cy + (radius * 0.12f),
            size = heroSize,
            animTime = animTime,
            animState = HeroAnimState.IDLE,
            stateTimer = 0f,
            facingDirection = 1f,
            isDeployed = false
        )

        // 3. Ornate Rarity Border Ring
        strokePaint.color = if (isUnlocked) hero.rarity.colorInt else Color.parseColor("#64748B")
        strokePaint.strokeWidth = 3.5f
        canvas.drawCircle(cx, cy, radius - 2f, strokePaint)

        // Golden accent pips on cardinal points
        if (isUnlocked) {
            fillPaint.color = Color.parseColor("#F59E0B")
            val pipR = 2.5f
            canvas.drawCircle(cx, cy - radius + 2f, pipR, fillPaint)
            canvas.drawCircle(cx, cy + radius - 2f, pipR, fillPaint)
            canvas.drawCircle(cx - radius + 2f, cy, pipR, fillPaint)
            canvas.drawCircle(cx + radius - 2f, cy, pipR, fillPaint)
        } else {
            // Locked Overlay & Lock Icon
            fillPaint.color = Color.parseColor("#990B0F19")
            canvas.drawCircle(cx, cy, radius, fillPaint)

            // Stylized Lock symbol
            strokePaint.color = Color.parseColor("#94A3B8")
            strokePaint.strokeWidth = 3f
            reusableRectF.set(cx - 7f, cy - 1f, cx + 7f, cy + 11f)
            canvas.drawRoundRect(reusableRectF, 3f, 3f, strokePaint)
            // Shackle
            reusableRectF.set(cx - 5f, cy - 9f, cx + 5f, cy - 1f)
            canvas.drawArc(reusableRectF, 180f, 180f, false, strokePaint)
        }
    }

    // =========================================================================
    // 1. KAEL — Cavaleiro do Fogo (Warrior / Vanguard)
    // =========================================================================

    private fun drawKael(
        canvas: Canvas,
        s: Float,
        animTime: Float,
        state: HeroAnimState,
        stateTimer: Float,
        rarity: HeroRarity
    ) {
        // Natural breathing motion
        val breath = sin(animTime * 3.5f).toFloat()
        val breathY = breath * 1.5f * s

        // Animation state modulation
        var torsoShiftX = 0f
        var swordAngle = 32f + breath * 4f
        var isSlashing = false
        var slashProgress = 0f
        var isSkillBurst = false

        when (state) {
            HeroAnimState.WINDUP -> {
                val t = (stateTimer / 0.12f).coerceIn(0f, 1f)
                torsoShiftX = -2.5f * s * t
                swordAngle = 32f - (75f * t) // Pulls sword back to -43°
            }
            HeroAnimState.STRIKE -> {
                val t = (stateTimer / 0.18f).coerceIn(0f, 1f)
                torsoShiftX = 3.5f * s * (1f - abs(t - 0.5f) * 2f)
                swordAngle = -43f + (125f * t) // Slashes forward to 82°
                isSlashing = true
                slashProgress = t
            }
            HeroAnimState.RECOVERY -> {
                val t = (stateTimer / 0.15f).coerceIn(0f, 1f)
                torsoShiftX = 1.5f * s * (1f - t)
                swordAngle = 82f - (50f * t) // Eases back to 32°
            }
            HeroAnimState.SKILL_CAST -> {
                torsoShiftX = 0f
                swordAngle = 92f + sin(animTime * 12f).toFloat() * 4f
                isSkillBurst = true
            }
            HeroAnimState.VICTORY -> {
                torsoShiftX = 0f
                swordAngle = 98f + sin(animTime * 8f).toFloat() * 6f
            }
            HeroAnimState.IDLE -> {
                torsoShiftX = 0f
            }
        }

        // 1. Ground Shadow
        reusableRectF.set(-16f * s, 18f * s, 16f * s, 26f * s)
        canvas.drawOval(reusableRectF, shadowPaint)

        // 2. Fiery Ground Aura & Rising Embers
        auraRingPaint.color = Color.parseColor("#F97316")
        auraRingPaint.alpha = (90 + (breath * 25).toInt()).coerceIn(50, 180)
        canvas.drawCircle(0f, 20f * s, 18f * s + breath * s, auraRingPaint)

        // Rising embers
        fillPaint.color = Color.parseColor("#FDE047")
        for (i in 0 until 4) {
            val emberY = (20f * s) - (((animTime * 28f + i * 14f) % 36f) * s)
            val emberX = sin((emberY * 0.15f) + i) * (10f * s)
            canvas.drawCircle(emberX, emberY, 1.4f * s, fillPaint)
        }

        // 3. Flowing Crimson Cape (Behind torso)
        val capeSway = sin(animTime * 4.2f).toFloat() * 4.5f * s
        reusablePath.reset()
        reusablePath.moveTo(-8f * s + torsoShiftX, -4f * s + breathY)
        reusablePath.lineTo(-14f * s + capeSway, 17f * s)
        reusablePath.quadTo(capeSway * 0.6f, 21f * s, 12f * s + capeSway, 18f * s)
        reusablePath.lineTo(8f * s + torsoShiftX, -4f * s + breathY)
        reusablePath.close()

        fillPaint.color = Color.parseColor("#991B1B") // Dark crimson
        canvas.drawPath(reusablePath, fillPaint)
        strokePaint.color = Color.parseColor("#DC2626")
        strokePaint.strokeWidth = 1.5f * s
        canvas.drawPath(reusablePath, strokePaint)

        // 4. Armored Legs & Boots
        fillPaint.color = Color.parseColor("#18181B") // Obsidian greaves
        reusableRectF.set(-9f * s, 8f * s, -3f * s, 20f * s)
        canvas.drawRoundRect(reusableRectF, 2f * s, 2f * s, fillPaint)
        reusableRectF.set(3f * s, 8f * s, 9f * s, 20f * s)
        canvas.drawRoundRect(reusableRectF, 2f * s, 2f * s, fillPaint)

        // Golden knee guards
        fillPaint.color = Color.parseColor("#F59E0B")
        canvas.drawCircle(-6f * s, 11f * s, 2.5f * s, fillPaint)
        canvas.drawCircle(6f * s, 11f * s, 2.5f * s, fillPaint)

        // 5. Heavy Torso & Molten Chestplate
        fillPaint.color = Color.parseColor("#1F2937") // Dark slate plate
        reusableRectF.set(-10f * s + torsoShiftX, -8f * s + breathY, 10f * s + torsoShiftX, 9f * s + breathY)
        canvas.drawRoundRect(reusableRectF, 4f * s, 4f * s, fillPaint)

        // Incandescent molten chest core
        fillPaint.color = Color.parseColor("#F97316")
        canvas.drawCircle(torsoShiftX, 0f + breathY, 4.2f * s, fillPaint)
        fillPaint.color = Color.parseColor("#FEF08A")
        canvas.drawCircle(torsoShiftX, 0f + breathY, 2.2f * s, fillPaint)

        // Golden Pauldrons (Shoulder guards)
        fillPaint.color = Color.parseColor("#F59E0B")
        reusablePath.reset()
        reusablePath.moveTo(-10f * s + torsoShiftX, -7f * s + breathY)
        reusablePath.lineTo(-17f * s + torsoShiftX, -4f * s + breathY)
        reusablePath.lineTo(-12f * s + torsoShiftX, 2f * s + breathY)
        reusablePath.close()
        canvas.drawPath(reusablePath, fillPaint)

        reusablePath.reset()
        reusablePath.moveTo(10f * s + torsoShiftX, -7f * s + breathY)
        reusablePath.lineTo(17f * s + torsoShiftX, -4f * s + breathY)
        reusablePath.lineTo(12f * s + torsoShiftX, 2f * s + breathY)
        reusablePath.close()
        canvas.drawPath(reusablePath, fillPaint)

        // 6. Knight Helmet & Fiery Visor
        fillPaint.color = Color.parseColor("#27272A")
        canvas.drawCircle(torsoShiftX, -14f * s + breathY, 7.5f * s, fillPaint)

        // Golden crest on helm
        fillPaint.color = Color.parseColor("#F59E0B")
        reusablePath.reset()
        reusablePath.moveTo(torsoShiftX - 2.5f * s, -21f * s + breathY)
        reusablePath.lineTo(torsoShiftX, -25f * s + breathY)
        reusablePath.lineTo(torsoShiftX + 2.5f * s, -21f * s + breathY)
        reusablePath.close()
        canvas.drawPath(reusablePath, fillPaint)

        // Fiery glowing eye visor slit
        strokePaint.color = Color.parseColor("#FBBF24")
        strokePaint.strokeWidth = 2f * s
        canvas.drawLine(torsoShiftX - 4.5f * s, -14f * s + breathY, torsoShiftX + 4.5f * s, -14f * s + breathY, strokePaint)

        // 7. Articulated Flaming Greatsword
        val swordPivotX = 10f * s + torsoShiftX
        val swordPivotY = -2f * s + breathY

        canvas.save()
        canvas.translate(swordPivotX, swordPivotY)
        canvas.rotate(swordAngle)

        // Golden hilt and crossguard
        fillPaint.color = Color.parseColor("#D97706")
        canvas.drawRect(-3f * s, -1.5f * s, 3f * s, 1.5f * s, fillPaint)
        fillPaint.color = Color.parseColor("#78350F")
        canvas.drawRect(-1f * s, 1.5f * s, 1f * s, 7f * s, fillPaint) // Handle
        fillPaint.color = Color.parseColor("#F59E0B")
        canvas.drawCircle(0f, 7.5f * s, 1.5f * s, fillPaint) // Pommel

        // Fiery Blade Aura (Outer orange, inner white-yellow blade)
        strokePaint.color = Color.parseColor("#EA580C")
        strokePaint.strokeWidth = 6.5f * s
        canvas.drawLine(0f, -2f * s, 0f, -28f * s, strokePaint)

        strokePaint.color = Color.parseColor("#FDE047")
        strokePaint.strokeWidth = 3.5f * s
        canvas.drawLine(0f, -2f * s, 0f, -27f * s, strokePaint)

        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 1.8f * s
        canvas.drawLine(0f, -2f * s, 0f, -26f * s, strokePaint)

        canvas.restore()

        // 8. Dynamic Slash Arc (Visible during strike execution)
        if (isSlashing) {
            slashArcPaint.color = Color.parseColor("#F97316")
            slashArcPaint.strokeWidth = 5f * s
            reusableRectF.set(-2f * s, -28f * s, 36f * s, 10f * s)
            canvas.drawArc(reusableRectF, -80f, 130f * slashProgress, false, slashArcPaint)

            slashArcPaint.color = Color.parseColor("#FEF08A")
            slashArcPaint.strokeWidth = 2.5f * s
            canvas.drawArc(reusableRectF, -70f, 110f * slashProgress, false, slashArcPaint)
        }

        // 9. Skill Channeling Shockwave (Visible during skill cast)
        if (isSkillBurst) {
            effectGlowPaint.color = Color.parseColor("#F97316")
            effectGlowPaint.alpha = (120 + sin(animTime * 15f) * 60).toInt().coerceIn(40, 200)
            canvas.drawCircle(0f, 0f, 28f * s, effectGlowPaint)

            strokePaint.color = Color.parseColor("#FEF08A")
            strokePaint.strokeWidth = 3f * s
            canvas.drawCircle(0f, 0f, 24f * s + sin(animTime * 10f) * 4f * s, strokePaint)
        }

        // 10. Victory Halo
        if (state == HeroAnimState.VICTORY) {
            auraRingPaint.color = rarity.colorInt
            auraRingPaint.alpha = (90 + sin(animTime * 8f) * 50).toInt().coerceIn(30, 180)
            canvas.drawCircle(0f, 0f, 24f * s, auraRingPaint)
        }
    }

    // =========================================================================
    // 2. LYRA — Patrulheira Arcana (Archer / Arcane Marksman)
    // =========================================================================

    private fun drawLyra(
        canvas: Canvas,
        s: Float,
        animTime: Float,
        state: HeroAnimState,
        stateTimer: Float,
        rarity: HeroRarity
    ) {
        val breath = sin(animTime * 3.8f).toFloat()
        val breathY = breath * 1.2f * s

        var bowArmX = 10f * s
        var bowArmY = -3f * s + breathY
        var bowStringDraw = 0f
        var isArrowFlying = false
        var arrowFlyDist = 0f

        when (state) {
            HeroAnimState.WINDUP -> {
                val t = (stateTimer / 0.12f).coerceIn(0f, 1f)
                bowStringDraw = 12f * s * t // String pulled back
            }
            HeroAnimState.STRIKE -> {
                val t = (stateTimer / 0.18f).coerceIn(0f, 1f)
                bowStringDraw = 0f // Snapped forward
                isArrowFlying = true
                arrowFlyDist = 38f * s * t
            }
            HeroAnimState.RECOVERY -> {
                bowStringDraw = 0f
            }
            HeroAnimState.SKILL_CAST -> {
                bowStringDraw = 10f * s
                isArrowFlying = true
                arrowFlyDist = 20f * s
            }
            HeroAnimState.VICTORY -> {
                bowArmY = -14f * s + breathY
            }
            HeroAnimState.IDLE -> {
                bowStringDraw = 0f
            }
        }

        // 1. Ground Shadow
        reusableRectF.set(-14f * s, 17f * s, 14f * s, 25f * s)
        canvas.drawOval(reusableRectF, shadowPaint)

        // 2. Arcane Cyan Ground Ring
        auraRingPaint.color = Color.parseColor("#06B6D4")
        auraRingPaint.alpha = (90 + (breath * 20).toInt()).coerceIn(40, 160)
        canvas.drawCircle(0f, 19f * s, 16f * s, auraRingPaint)

        // 3. Flowing Elven Cloak & Quiver
        val cloakWave = sin(animTime * 4.8f).toFloat() * 3.5f * s
        reusablePath.reset()
        reusablePath.moveTo(-6f * s, -4f * s + breathY)
        reusablePath.lineTo(-12f * s + cloakWave, 16f * s)
        reusablePath.lineTo(4f * s + cloakWave, 17f * s)
        reusablePath.lineTo(6f * s, -4f * s + breathY)
        reusablePath.close()

        fillPaint.color = Color.parseColor("#1E3A8A") // Deep sapphire blue
        canvas.drawPath(reusablePath, fillPaint)

        // Quiver on back with cyan arrow fletchings
        fillPaint.color = Color.parseColor("#475569")
        reusableRectF.set(-9f * s, -10f * s + breathY, -4f * s, 6f * s)
        canvas.drawRoundRect(reusableRectF, 2f * s, 2f * s, fillPaint)
        strokePaint.color = Color.parseColor("#38BDF8")
        strokePaint.strokeWidth = 1.5f * s
        canvas.drawLine(-6.5f * s, -14f * s + breathY, -6.5f * s, -9f * s + breathY, strokePaint)

        // 4. Agile Legs & Leather Boots
        fillPaint.color = Color.parseColor("#334155")
        reusableRectF.set(-8f * s, 8f * s, -3f * s, 19f * s)
        canvas.drawRoundRect(reusableRectF, 2f * s, 2f * s, fillPaint)
        reusableRectF.set(2f * s, 8f * s, 7f * s, 19f * s)
        canvas.drawRoundRect(reusableRectF, 2f * s, 2f * s, fillPaint)

        // 5. Fitted Arcane Tunic & Silver Filigree
        fillPaint.color = Color.parseColor("#2563EB") // Royal Blue
        reusableRectF.set(-8f * s, -7f * s + breathY, 8f * s, 8f * s + breathY)
        canvas.drawRoundRect(reusableRectF, 3f * s, 3f * s, fillPaint)

        // Silver chest clasp
        fillPaint.color = Color.parseColor("#E2E8F0")
        canvas.drawCircle(0f, 0f + breathY, 2.5f * s, fillPaint)

        // 6. Elven Head, Hood & Glowing Eyes
        fillPaint.color = Color.parseColor("#1D4ED8")
        canvas.drawCircle(0f, -13f * s + breathY, 6.5f * s, fillPaint)

        // Flowing silver-blue hair
        fillPaint.color = Color.parseColor("#93C5FD")
        canvas.drawCircle(-3f * s, -11f * s + breathY, 3f * s, fillPaint)

        // Glowing Cyan Eyes
        fillPaint.color = Color.parseColor("#38BDF8")
        canvas.drawCircle(2f * s, -13f * s + breathY, 1.3f * s, fillPaint)

        // Silver Diadem / Circlet
        strokePaint.color = Color.parseColor("#F1F5F9")
        strokePaint.strokeWidth = 1.5f * s
        canvas.drawLine(-4f * s, -15f * s + breathY, 4f * s, -15f * s + breathY, strokePaint)

        // 7. Arcane Composite Recurve Bow (Left Hand)
        canvas.save()
        canvas.translate(bowArmX, bowArmY)

        // Bow curved stave (Silver/Cyan)
        reusablePath.reset()
        reusablePath.moveTo(0f, -18f * s)
        reusablePath.quadTo(8f * s, 0f, 0f, 18f * s)
        strokePaint.color = Color.parseColor("#CBD5E1")
        strokePaint.strokeWidth = 3f * s
        canvas.drawPath(reusablePath, strokePaint)

        // Arcane glowing gems on tips
        fillPaint.color = Color.parseColor("#06B6D4")
        canvas.drawCircle(0f, -18f * s, 2f * s, fillPaint)
        canvas.drawCircle(0f, 18f * s, 2f * s, fillPaint)

        // Drawn Bowstring of light (V-shape if drawn)
        strokePaint.color = Color.parseColor("#E0F2FE")
        strokePaint.strokeWidth = 1.2f * s
        reusablePath.reset()
        reusablePath.moveTo(0f, -18f * s)
        reusablePath.lineTo(-bowStringDraw, 0f)
        reusablePath.lineTo(0f, 18f * s)
        canvas.drawPath(reusablePath, strokePaint)

        // Glowing crystal arrow resting on string during windup
        if (bowStringDraw > 0f) {
            strokePaint.color = Color.parseColor("#38BDF8")
            strokePaint.strokeWidth = 2.2f * s
            canvas.drawLine(-bowStringDraw, 0f, 12f * s, 0f, strokePaint)
            fillPaint.color = Color.WHITE
            canvas.drawCircle(12f * s, 0f, 2f * s, fillPaint)
        }

        canvas.restore()

        // 8. Fired Arcane Arrow & Speedlines (During strike)
        if (isArrowFlying) {
            strokePaint.color = Color.parseColor("#38BDF8")
            strokePaint.strokeWidth = 2.8f * s
            val arrowStartX = bowArmX + arrowFlyDist - (16f * s)
            val arrowEndX = bowArmX + arrowFlyDist
            canvas.drawLine(arrowStartX, bowArmY, arrowEndX, bowArmY, strokePaint)

            fillPaint.color = Color.WHITE
            canvas.drawCircle(arrowEndX, bowArmY, 2.5f * s, fillPaint)

            // Speedline trail
            strokePaint.color = Color.parseColor("#E0F2FE")
            strokePaint.strokeWidth = 1.2f * s
            canvas.drawLine(arrowStartX - 8f * s, bowArmY - 2f * s, arrowStartX, bowArmY - 2f * s, strokePaint)
            canvas.drawLine(arrowStartX - 8f * s, bowArmY + 2f * s, arrowStartX, bowArmY + 2f * s, strokePaint)
        }

        // Victory Halo
        if (state == HeroAnimState.VICTORY) {
            auraRingPaint.color = rarity.colorInt
            auraRingPaint.alpha = (90 + sin(animTime * 8f) * 50).toInt().coerceIn(30, 180)
            canvas.drawCircle(0f, 0f, 24f * s, auraRingPaint)
        }
    }

    // =========================================================================
    // 3. BRAKK — Sentinela das Montanhas (Heavy Guardian / Tank)
    // =========================================================================

    private fun drawBrakk(
        canvas: Canvas,
        s: Float,
        animTime: Float,
        state: HeroAnimState,
        stateTimer: Float,
        rarity: HeroRarity
    ) {
        val breath = sin(animTime * 2.4f).toFloat() // Slow heavy breathing
        val breathY = breath * 1.8f * s

        var hammerAngle = 20f
        var isSmashing = false
        var smashProgress = 0f
        var shieldX = -10f * s

        when (state) {
            HeroAnimState.WINDUP -> {
                val t = (stateTimer / 0.12f).coerceIn(0f, 1f)
                hammerAngle = 20f - (85f * t) // Raises hammer high behind head (-65°)
                shieldX = -10f * s - (3f * s * t)
            }
            HeroAnimState.STRIKE -> {
                val t = (stateTimer / 0.18f).coerceIn(0f, 1f)
                hammerAngle = -65f + (140f * t) // Slams hammer to ground (+75°)
                isSmashing = true
                smashProgress = t
            }
            HeroAnimState.RECOVERY -> {
                val t = (stateTimer / 0.15f).coerceIn(0f, 1f)
                hammerAngle = 75f - (55f * t)
            }
            HeroAnimState.SKILL_CAST -> {
                hammerAngle = -20f
                shieldX = 0f // Plants shield dead center!
            }
            HeroAnimState.VICTORY -> {
                hammerAngle = -50f + sin(animTime * 6f).toFloat() * 10f
            }
            HeroAnimState.IDLE -> {
                hammerAngle = 20f
            }
        }

        // 1. Extra Wide Ground Shadow
        reusableRectF.set(-22f * s, 18f * s, 22f * s, 27f * s)
        canvas.drawOval(reusableRectF, shadowPaint)

        // 2. Cracked Stone Runic Decal
        auraRingPaint.color = Color.parseColor("#D97706") // Earth Bronze Gold
        auraRingPaint.alpha = (100 + (breath * 25).toInt()).coerceIn(50, 180)
        canvas.drawCircle(0f, 21f * s, 20f * s, auraRingPaint)

        // 3. Imposing Greaves & Ultra-wide Fortress Stance
        fillPaint.color = Color.parseColor("#334155") // Granite plate
        reusableRectF.set(-13f * s, 8f * s, -5f * s, 21f * s)
        canvas.drawRoundRect(reusableRectF, 3f * s, 3f * s, fillPaint)
        reusableRectF.set(5f * s, 8f * s, 13f * s, 21f * s)
        canvas.drawRoundRect(reusableRectF, 3f * s, 3f * s, fillPaint)

        // 4. Granite Cuirass & Moss Green Plating
        fillPaint.color = Color.parseColor("#1E293B")
        reusableRectF.set(-14f * s, -9f * s + breathY, 14f * s, 9f * s + breathY)
        canvas.drawRoundRect(reusableRectF, 4f * s, 4f * s, fillPaint)

        fillPaint.color = Color.parseColor("#047857") // Mountain moss green
        reusableRectF.set(-10f * s, -6f * s + breathY, 10f * s, 6f * s + breathY)
        canvas.drawRoundRect(reusableRectF, 3f * s, 3f * s, fillPaint)

        // Colossal Spiked Boulder Pauldrons
        fillPaint.color = Color.parseColor("#475569")
        canvas.drawCircle(-15f * s, -5f * s + breathY, 5.5f * s, fillPaint)
        canvas.drawCircle(15f * s, -5f * s + breathY, 5.5f * s, fillPaint)
        fillPaint.color = Color.parseColor("#F59E0B")
        canvas.drawCircle(-15f * s, -8f * s + breathY, 2f * s, fillPaint)
        canvas.drawCircle(15f * s, -8f * s + breathY, 2f * s, fillPaint)

        // 5. Great Helm with Golden Horns
        fillPaint.color = Color.parseColor("#1E293B")
        canvas.drawCircle(0f, -16f * s + breathY, 8.5f * s, fillPaint)

        // Large Golden Horns
        fillPaint.color = Color.parseColor("#F59E0B")
        reusablePath.reset()
        reusablePath.moveTo(-6f * s, -20f * s + breathY)
        reusablePath.quadTo(-16f * s, -26f * s + breathY, -14f * s, -14f * s + breathY)
        canvas.drawPath(reusablePath, strokePaint.apply { strokeWidth = 3f * s; color = Color.parseColor("#F59E0B") })

        reusablePath.reset()
        reusablePath.moveTo(6f * s, -20f * s + breathY)
        reusablePath.quadTo(16f * s, -26f * s + breathY, 14f * s, -14f * s + breathY)
        canvas.drawPath(reusablePath, strokePaint)

        // Emerald Glowing Eye Slit
        strokePaint.color = Color.parseColor("#10B981")
        strokePaint.strokeWidth = 2.5f * s
        canvas.drawLine(-5f * s, -16f * s + breathY, 5f * s, -16f * s + breathY, strokePaint)

        // 6. Colossal Stone Warhammer (Right Arm)
        val hammerPivotX = 13f * s
        val hammerPivotY = -2f * s + breathY

        canvas.save()
        canvas.translate(hammerPivotX, hammerPivotY)
        canvas.rotate(hammerAngle)

        // Steel haft
        strokePaint.color = Color.parseColor("#94A3B8")
        strokePaint.strokeWidth = 3.5f * s
        canvas.drawLine(0f, 8f * s, 0f, -22f * s, strokePaint)

        // Heavy Stone Hammer Head
        fillPaint.color = Color.parseColor("#475569")
        reusableRectF.set(-6f * s, -29f * s, 6f * s, -19f * s)
        canvas.drawRoundRect(reusableRectF, 2f * s, 2f * s, fillPaint)

        // Gold reinforced band
        fillPaint.color = Color.parseColor("#F59E0B")
        reusableRectF.set(-6.5f * s, -25f * s, 6.5f * s, -23f * s)
        canvas.drawRect(reusableRectF, fillPaint)

        canvas.restore()

        // 7. Massive Runic Tower Shield (Left Arm / Front Guard)
        canvas.save()
        canvas.translate(shieldX, 0f + breathY)

        reusablePath.reset()
        reusablePath.moveTo(-8f * s, -12f * s)
        reusablePath.lineTo(8f * s, -12f * s)
        reusablePath.lineTo(7f * s, 8f * s)
        reusablePath.lineTo(0f, 18f * s) // Pointed bottom
        reusablePath.lineTo(-7f * s, 8f * s)
        reusablePath.close()

        fillPaint.color = Color.parseColor("#047857")
        canvas.drawPath(reusablePath, fillPaint)

        strokePaint.color = Color.parseColor("#94A3B8")
        strokePaint.strokeWidth = 2.5f * s
        canvas.drawPath(reusablePath, strokePaint)

        // Embossed golden mountain crest
        fillPaint.color = Color.parseColor("#F59E0B")
        canvas.drawCircle(0f, 2f * s, 3.5f * s, fillPaint)

        canvas.restore()

        // 8. Ground Smash Impact Effect
        if (isSmashing) {
            strokePaint.color = Color.parseColor("#F59E0B")
            strokePaint.strokeWidth = 4f * s
            reusableRectF.set(6f * s, 10f * s, 34f * s, 26f * s)
            canvas.drawArc(reusableRectF, 0f, 360f, false, strokePaint)

            // Stone dust particles
            fillPaint.color = Color.parseColor("#CBD5E1")
            for (p in 0 until 5) {
                val dustAngle = p * 0.6f
                val dx = 20f * s + cos(dustAngle) * (14f * s * smashProgress)
                val dy = 18f * s - sin(dustAngle) * (12f * s * smashProgress)
                canvas.drawCircle(dx, dy, 2f * s, fillPaint)
            }
        }

        // Victory Halo
        if (state == HeroAnimState.VICTORY) {
            auraRingPaint.color = rarity.colorInt
            auraRingPaint.alpha = (90 + sin(animTime * 8f) * 50).toInt().coerceIn(30, 180)
            canvas.drawCircle(0f, 0f, 26f * s, auraRingPaint)
        }
    }

    // =========================================================================
    // 4. NYRA — Lâmina do Crepúsculo (Shadow Assassin)
    // =========================================================================

    private fun drawNyra(
        canvas: Canvas,
        s: Float,
        animTime: Float,
        state: HeroAnimState,
        stateTimer: Float,
        rarity: HeroRarity
    ) {
        val breath = sin(animTime * 4.5f).toFloat()
        val breathY = breath * 1.1f * s

        var daggerAngleL = -30f
        var daggerAngleR = 30f
        var isCrossSlashing = false
        var slashT = 0f
        var alphaMod = 255

        when (state) {
            HeroAnimState.WINDUP -> {
                val t = (stateTimer / 0.12f).coerceIn(0f, 1f)
                daggerAngleL = -30f - (45f * t)
                daggerAngleR = 30f + (45f * t)
                alphaMod = (255 - (t * 90)).toInt() // Stealth fading
            }
            HeroAnimState.STRIKE -> {
                val t = (stateTimer / 0.18f).coerceIn(0f, 1f)
                daggerAngleL = -75f + (140f * t) // Scissor cross slash
                daggerAngleR = 75f - (140f * t)
                isCrossSlashing = true
                slashT = t
            }
            HeroAnimState.RECOVERY -> {
                val t = (stateTimer / 0.15f).coerceIn(0f, 1f)
                daggerAngleL = 65f - (95f * t)
                daggerAngleR = -65f + (95f * t)
            }
            HeroAnimState.SKILL_CAST -> {
                daggerAngleL = -80f
                daggerAngleR = 80f
            }
            HeroAnimState.VICTORY -> {
                daggerAngleL = -95f + sin(animTime * 8f).toFloat() * 15f
                daggerAngleR = 95f - sin(animTime * 8f).toFloat() * 15f
            }
            HeroAnimState.IDLE -> {
                daggerAngleL = -30f
                daggerAngleR = 30f
            }
        }

        // 1. Dark Violet Shadow Mist Pool
        auraGlowPaint.color = Color.parseColor("#581C87")
        auraGlowPaint.alpha = 110
        reusableRectF.set(-15f * s, 17f * s, 15f * s, 25f * s)
        canvas.drawOval(reusableRectF, auraGlowPaint)

        // 2. Wispy Ragged Cloak (Behind body)
        val cloakWave = sin(animTime * 5.5f).toFloat() * 4f * s
        reusablePath.reset()
        reusablePath.moveTo(-6f * s, -4f * s + breathY)
        reusablePath.lineTo(-13f * s + cloakWave, 16f * s)
        reusablePath.lineTo(cloakWave * 0.5f, 18f * s)
        reusablePath.lineTo(11f * s + cloakWave, 16f * s)
        reusablePath.lineTo(6f * s, -4f * s + breathY)
        reusablePath.close()

        fillPaint.color = Color.parseColor("#3B0764")
        fillPaint.alpha = alphaMod
        canvas.drawPath(reusablePath, fillPaint)

        // 3. Crouched Agile Legs
        fillPaint.color = Color.parseColor("#18181B")
        reusableRectF.set(-8f * s, 8f * s, -3f * s, 18f * s)
        canvas.drawRoundRect(reusableRectF, 2f * s, 2f * s, fillPaint)
        reusableRectF.set(2f * s, 8f * s, 7f * s, 18f * s)
        canvas.drawRoundRect(reusableRectF, 2f * s, 2f * s, fillPaint)

        // 4. Midnight Corslet & Throwing Straps
        fillPaint.color = Color.parseColor("#09090B")
        reusableRectF.set(-7f * s, -6f * s + breathY, 7f * s, 8f * s + breathY)
        canvas.drawRoundRect(reusableRectF, 3f * s, 3f * s, fillPaint)

        strokePaint.color = Color.parseColor("#A855F7") // Violet trim
        strokePaint.strokeWidth = 1.2f * s
        canvas.drawLine(-5f * s, 0f + breathY, 5f * s, 0f + breathY, strokePaint)

        // 5. Shadow Cowl & Piercing Violet/Magenta Eyes
        fillPaint.color = Color.parseColor("#18181B")
        canvas.drawCircle(0f, -12f * s + breathY, 6f * s, fillPaint)

        // Deep Cowl Peak
        reusablePath.reset()
        reusablePath.moveTo(-5f * s, -14f * s + breathY)
        reusablePath.lineTo(0f, -18f * s + breathY)
        reusablePath.lineTo(5f * s, -14f * s + breathY)
        strokePaint.color = Color.parseColor("#581C87")
        strokePaint.strokeWidth = 1.5f * s
        canvas.drawPath(reusablePath, strokePaint)

        // Piercing Neon Magenta Eyes peering from shadow
        fillPaint.color = Color.parseColor("#F43F5E")
        canvas.drawCircle(-2f * s, -12f * s + breathY, 1.2f * s, fillPaint)
        canvas.drawCircle(2f * s, -12f * s + breathY, 1.2f * s, fillPaint)

        // 6. Twin Shadow Katars / Daggers (Left and Right)
        drawDagger(canvas, s, -9f * s, 0f + breathY, daggerAngleL)
        drawDagger(canvas, s, 9f * s, 0f + breathY, daggerAngleR)

        // 7. Scissor Cross Slash Effect (X Pattern)
        if (isCrossSlashing) {
            slashArcPaint.color = Color.parseColor("#D946EF") // Neon Magenta
            slashArcPaint.strokeWidth = 3.5f * s
            val progressLen = 28f * s * slashT
            // Slash 1: Top-left to bottom-right
            canvas.drawLine(0f, -14f * s, 0f + progressLen, -14f * s + progressLen, slashArcPaint)
            // Slash 2: Top-right to bottom-left
            canvas.drawLine(0f + progressLen, -14f * s, 0f, -14f * s + progressLen, slashArcPaint)
        }

        // Victory Halo
        if (state == HeroAnimState.VICTORY) {
            auraRingPaint.color = rarity.colorInt
            auraRingPaint.alpha = (90 + sin(animTime * 8f) * 50).toInt().coerceIn(30, 180)
            canvas.drawCircle(0f, 0f, 22f * s, auraRingPaint)
        }
    }

    private fun drawDagger(canvas: Canvas, s: Float, x: Float, y: Float, angle: Float) {
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(angle)

        // Hilt
        fillPaint.color = Color.parseColor("#27272A")
        canvas.drawRect(-1f * s, 0f, 1f * s, 5f * s, fillPaint)

        // Glowing shadow katar blade
        strokePaint.color = Color.parseColor("#D946EF") // Glowing magenta
        strokePaint.strokeWidth = 3f * s
        canvas.drawLine(0f, 0f, 0f, -16f * s, strokePaint)

        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 1.2f * s
        canvas.drawLine(0f, 0f, 0f, -14f * s, strokePaint)

        canvas.restore()
    }

    // =========================================================================
    // 5. TIKO — Artífice do Reino (Engineer / Technologist)
    // =========================================================================

    private fun drawTiko(
        canvas: Canvas,
        s: Float,
        animTime: Float,
        state: HeroAnimState,
        stateTimer: Float,
        rarity: HeroRarity
    ) {
        val bounce = abs(sin(animTime * 4.5f)).toFloat()
        val bounceY = -bounce * 1.8f * s

        var wrenchAngle = 25f
        var isWrenchSwinging = false
        var swingProgress = 0f

        when (state) {
            HeroAnimState.WINDUP -> {
                val t = (stateTimer / 0.12f).coerceIn(0f, 1f)
                wrenchAngle = 25f - (75f * t)
            }
            HeroAnimState.STRIKE -> {
                val t = (stateTimer / 0.18f).coerceIn(0f, 1f)
                wrenchAngle = -50f + (130f * t)
                isWrenchSwinging = true
                swingProgress = t
            }
            HeroAnimState.RECOVERY -> {
                val t = (stateTimer / 0.15f).coerceIn(0f, 1f)
                wrenchAngle = 80f - (55f * t)
            }
            HeroAnimState.SKILL_CAST -> {
                wrenchAngle = -45f
            }
            HeroAnimState.VICTORY -> {
                wrenchAngle = -90f + sin(animTime * 10f).toFloat() * 15f
            }
            HeroAnimState.IDLE -> {
                wrenchAngle = 25f
            }
        }

        // 1. Ground Shadow
        reusableRectF.set(-14f * s, 17f * s, 14f * s, 25f * s)
        canvas.drawOval(reusableRectF, shadowPaint)

        // 2. Mechanical Gear Decal on Ground
        auraRingPaint.color = Color.parseColor("#EAB308")
        canvas.drawCircle(0f, 20f * s, 15f * s, auraRingPaint)

        // 3. Steampunk Backpack & Rotating Brass Cog (Behind body)
        val packX = -4f * s
        val packY = -3f * s + bounceY

        fillPaint.color = Color.parseColor("#B45309") // Copper tank
        reusableRectF.set(packX - 7f * s, packY - 7f * s, packX + 7f * s, packY + 7f * s)
        canvas.drawRoundRect(reusableRectF, 2f * s, 2f * s, fillPaint)

        // Rotating Brass Gear on top of backpack
        val cogAngle = (animTime * 180f) % 360f
        canvas.save()
        canvas.translate(packX, packY - 9f * s)
        canvas.rotate(cogAngle)

        fillPaint.color = Color.parseColor("#F59E0B")
        for (tooth in 0 until 6) {
            canvas.rotate(60f)
            canvas.drawRect(-1.5f * s, -6f * s, 1.5f * s, -3f * s, fillPaint)
        }
        canvas.drawCircle(0f, 0f, 4.5f * s, fillPaint)
        fillPaint.color = Color.parseColor("#78350F")
        canvas.drawCircle(0f, 0f, 1.8f * s, fillPaint) // Central axle hole

        canvas.restore()

        // Tiny steam puff from chimney
        fillPaint.color = Color.parseColor("#80E2E8F0")
        val puffY = packY - 14f * s - ((animTime * 15f) % 10f) * s
        canvas.drawCircle(packX + 4f * s, puffY, 2.5f * s, fillPaint)

        // 4. Sturdy Boots with Brass Caps
        fillPaint.color = Color.parseColor("#451A03")
        reusableRectF.set(-7f * s, 9f * s, -2f * s, 19f * s)
        canvas.drawRoundRect(reusableRectF, 2f * s, 2f * s, fillPaint)
        reusableRectF.set(2f * s, 9f * s, 7f * s, 19f * s)
        canvas.drawRoundRect(reusableRectF, 2f * s, 2f * s, fillPaint)

        fillPaint.color = Color.parseColor("#F59E0B")
        canvas.drawCircle(-4.5f * s, 18f * s, 2f * s, fillPaint)
        canvas.drawCircle(4.5f * s, 18f * s, 2f * s, fillPaint)

        // 5. Work Apron, Yellow Tunic & Utility Belt
        fillPaint.color = Color.parseColor("#EAB308") // Artificer yellow
        reusableRectF.set(-7.5f * s, -6f * s + bounceY, 7.5f * s, 9f * s + bounceY)
        canvas.drawRoundRect(reusableRectF, 3f * s, 3f * s, fillPaint)

        fillPaint.color = Color.parseColor("#78350F") // Leather apron
        reusableRectF.set(-5f * s, -2f * s + bounceY, 5f * s, 9f * s + bounceY)
        canvas.drawRect(reusableRectF, fillPaint)

        // 6. Inventor Head & Aviator Goggles
        fillPaint.color = Color.parseColor("#FDE68A") // Skin tone
        canvas.drawCircle(0f, -12f * s + bounceY, 6.5f * s, fillPaint)

        // Spiky Auburn Hair
        fillPaint.color = Color.parseColor("#B45309")
        canvas.drawCircle(-3f * s, -17f * s + bounceY, 2.5f * s, fillPaint)
        canvas.drawCircle(2f * s, -17f * s + bounceY, 3f * s, fillPaint)

        // Big Brass Goggles on forehead
        fillPaint.color = Color.parseColor("#D97706")
        canvas.drawCircle(-3f * s, -14f * s + bounceY, 3.2f * s, fillPaint)
        canvas.drawCircle(3f * s, -14f * s + bounceY, 3.2f * s, fillPaint)
        fillPaint.color = Color.parseColor("#38BDF8") // Cyan lens
        canvas.drawCircle(-3f * s, -14f * s + bounceY, 2.2f * s, fillPaint)
        canvas.drawCircle(3f * s, -14f * s + bounceY, 2.2f * s, fillPaint)

        // Cheerful grin
        strokePaint.color = Color.parseColor("#451A03")
        strokePaint.strokeWidth = 1.2f * s
        canvas.drawLine(-2.5f * s, -9.5f * s + bounceY, 2.5f * s, -9.5f * s + bounceY, strokePaint)

        // 7. Oversized Kinetic Multi-tool Wrench (Right Hand)
        val wrenchPivotX = 9f * s
        val wrenchPivotY = 0f + bounceY

        canvas.save()
        canvas.translate(wrenchPivotX, wrenchPivotY)
        canvas.rotate(wrenchAngle)

        // Handle
        strokePaint.color = Color.parseColor("#64748B")
        strokePaint.strokeWidth = 3f * s
        canvas.drawLine(0f, 6f * s, 0f, -16f * s, strokePaint)

        // Brass spanner jaw
        fillPaint.color = Color.parseColor("#F59E0B")
        reusableRectF.set(-5f * s, -22f * s, 5f * s, -14f * s)
        canvas.drawRoundRect(reusableRectF, 2f * s, 2f * s, fillPaint)
        fillPaint.color = Color.WHITE
        canvas.drawCircle(0f, -18f * s, 2f * s, fillPaint)

        canvas.restore()

        // 8. Electrical Sparks upon Impact
        if (isWrenchSwinging) {
            strokePaint.color = Color.parseColor("#FDE047")
            strokePaint.strokeWidth = 2f * s
            for (arc in 0 until 4) {
                val sparkAngle = arc * 1.5f
                val dist = 14f * s * swingProgress
                val sx = 14f * s + cos(sparkAngle) * dist
                val sy = 6f * s + sin(sparkAngle) * dist
                canvas.drawCircle(sx, sy, 1.8f * s, fillPaint.apply { color = Color.parseColor("#FDE047") })
            }
        }

        // Victory Halo
        if (state == HeroAnimState.VICTORY) {
            auraRingPaint.color = rarity.colorInt
            auraRingPaint.alpha = (90 + sin(animTime * 8f) * 50).toInt().coerceIn(30, 180)
            canvas.drawCircle(0f, 0f, 24f * s, auraRingPaint)
        }
    }

    // =========================================================================
    // 6. VESPER — Oráculo das Estrelas (Astral Mage / Cosmic Weaver)
    // =========================================================================

    private fun drawVesper(
        canvas: Canvas,
        s: Float,
        animTime: Float,
        state: HeroAnimState,
        stateTimer: Float,
        rarity: HeroRarity
    ) {
        // Zero-gravity smooth levitation
        val hoverY = sin(animTime * 2.4f).toFloat() * 5f * s

        var staffAngle = 18f
        var isBursting = false
        var burstT = 0f

        when (state) {
            HeroAnimState.WINDUP -> {
                val t = (stateTimer / 0.12f).coerceIn(0f, 1f)
                staffAngle = 18f - (45f * t)
            }
            HeroAnimState.STRIKE -> {
                val t = (stateTimer / 0.18f).coerceIn(0f, 1f)
                staffAngle = -27f + (60f * t)
                isBursting = true
                burstT = t
            }
            HeroAnimState.RECOVERY -> {
                val t = (stateTimer / 0.15f).coerceIn(0f, 1f)
                staffAngle = 33f - (15f * t)
            }
            HeroAnimState.SKILL_CAST -> {
                staffAngle = -60f
                isBursting = true
                burstT = 0.8f
            }
            HeroAnimState.VICTORY -> {
                staffAngle = -85f + sin(animTime * 6f).toFloat() * 8f
            }
            HeroAnimState.IDLE -> {
                staffAngle = 18f
            }
        }

        // 1. Soft Cosmic Ground Shadow (Shrinks as hero levitates higher)
        val shadowScale = (1f - (hoverY / (20f * s))).coerceIn(0.6f, 1.1f)
        reusableRectF.set(-14f * s * shadowScale, 20f * s, 14f * s * shadowScale, 26f * s)
        canvas.drawOval(reusableRectF, shadowPaint)

        // 2. Starlight Constellation Ring on ground
        auraRingPaint.color = Color.parseColor("#38BDF8")
        auraRingPaint.alpha = 110
        canvas.drawCircle(0f, 22f * s, 18f * s, auraRingPaint)

        // 3. Orbiting Celestial Orbs — Back Layer (Drawn behind robes)
        drawOrbitingCelestialOrbs(canvas, s, animTime, hoverY, isFrontPass = false)

        // 4. Undulating Celestial Midnight Robes
        val robeWave = sin(animTime * 3.8f).toFloat() * 3f * s
        reusablePath.reset()
        reusablePath.moveTo(-8f * s, -6f * s + hoverY)
        reusablePath.lineTo(-12f * s + robeWave, 16f * s + hoverY)
        reusablePath.lineTo(12f * s - robeWave, 16f * s + hoverY)
        reusablePath.lineTo(8f * s, -6f * s + hoverY)
        reusablePath.close()

        fillPaint.color = Color.parseColor("#0F172A") // Cosmic void navy
        canvas.drawPath(reusablePath, fillPaint)

        strokePaint.color = Color.parseColor("#818CF8") // Stardust constellation trim
        strokePaint.strokeWidth = 1.8f * s
        canvas.drawPath(reusablePath, strokePaint)

        // Little starlight dots embroidered on robes
        fillPaint.color = Color.WHITE
        canvas.drawCircle(-4f * s, 6f * s + hoverY, 1f * s, fillPaint)
        canvas.drawCircle(3f * s, 10f * s + hoverY, 1.2f * s, fillPaint)
        canvas.drawCircle(0f, 2f * s + hoverY, 1f * s, fillPaint)

        // 5. Starry Cowl, Serene Face & Starlight Eyes
        fillPaint.color = Color.parseColor("#1E1B4B")
        canvas.drawCircle(0f, -13f * s + hoverY, 7f * s, fillPaint)

        // Floating Diamond Starlight Crown / Halo
        fillPaint.color = Color.parseColor("#38BDF8")
        canvas.drawCircle(0f, -22f * s + hoverY, 2.5f * s, fillPaint)
        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 1.2f * s
        canvas.drawCircle(0f, -22f * s + hoverY, 4.5f * s, strokePaint)

        // Pure Starlight Eyes
        fillPaint.color = Color.WHITE
        canvas.drawCircle(-2.2f * s, -13f * s + hoverY, 1.4f * s, fillPaint)
        canvas.drawCircle(2.2f * s, -13f * s + hoverY, 1.4f * s, fillPaint)

        // 6. Crystalline Astral Staff (Right Hand)
        val staffPivotX = 11f * s
        val staffPivotY = -2f * s + hoverY

        canvas.save()
        canvas.translate(staffPivotX, staffPivotY)
        canvas.rotate(staffAngle)

        // Silver-white shaft
        strokePaint.color = Color.parseColor("#F8FAFC")
        strokePaint.strokeWidth = 2.5f * s
        canvas.drawLine(0f, 10f * s, 0f, -24f * s, strokePaint)

        // Armillary Crystalline Sphere Top
        strokePaint.color = Color.parseColor("#38BDF8")
        strokePaint.strokeWidth = 1.8f * s
        canvas.drawCircle(0f, -27f * s, 5f * s, strokePaint)

        // Spinning Mini Galaxy Core
        fillPaint.color = Color.parseColor("#FDE047")
        canvas.drawCircle(0f, -27f * s, 2.2f * s, fillPaint)

        canvas.restore()

        // 7. Orbiting Celestial Orbs — Front Layer (Drawn over robes)
        drawOrbitingCelestialOrbs(canvas, s, animTime, hoverY, isFrontPass = true)

        // 8. Radiant Cosmic Starburst Beam (During strike or skill)
        if (isBursting) {
            effectGlowPaint.color = Color.parseColor("#38BDF8")
            effectGlowPaint.alpha = (140 + (burstT * 100)).toInt().coerceIn(60, 240)
            canvas.drawCircle(18f * s + (20f * s * burstT), -10f * s + hoverY, 8f * s, effectGlowPaint)

            strokePaint.color = Color.WHITE
            strokePaint.strokeWidth = 2f * s
            canvas.drawCircle(18f * s + (20f * s * burstT), -10f * s + hoverY, 5f * s, strokePaint)
        }

        // Victory Halo
        if (state == HeroAnimState.VICTORY) {
            auraRingPaint.color = rarity.colorInt
            auraRingPaint.alpha = (90 + sin(animTime * 8f) * 50).toInt().coerceIn(30, 180)
            canvas.drawCircle(0f, hoverY, 26f * s, auraRingPaint)
        }
    }

    private fun drawOrbitingCelestialOrbs(
        canvas: Canvas,
        s: Float,
        animTime: Float,
        hoverY: Float,
        isFrontPass: Boolean
    ) {
        val orbColors = intArrayOf(
            Color.parseColor("#38BDF8"), // Arcane Cyan
            Color.parseColor("#C084FC"), // Cosmic Violet
            Color.parseColor("#FDE047")  // Radiant Starlight Gold
        )

        val radiusX = 24f * s
        val radiusY = 9f * s

        for (i in 0 until 3) {
            val angle = animTime * 3.2f + i * (2f * PI.toFloat() / 3f)
            val isFront = sin(angle) > 0f

            if (isFront == isFrontPass) {
                val ox = cos(angle) * radiusX
                val oy = -4f * s + hoverY + sin(angle) * radiusY

                // Outer aura glow
                effectGlowPaint.color = orbColors[i]
                effectGlowPaint.alpha = 110
                canvas.drawCircle(ox, oy, 3.5f * s, effectGlowPaint)

                // Core orb
                fillPaint.color = orbColors[i]
                canvas.drawCircle(ox, oy, 2.2f * s, fillPaint)
                fillPaint.color = Color.WHITE
                canvas.drawCircle(ox, oy, 1.1f * s, fillPaint)
            }
        }
    }
}
